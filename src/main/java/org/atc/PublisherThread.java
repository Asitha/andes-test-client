/*
 * Copyright 2015 Asitha Nanayakkara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.atc;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Meter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atc.config.PublisherConfig;

import javax.jms.JMSException;
import javax.jms.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.codahale.metrics.MetricRegistry.name;

public class PublisherThread implements Runnable {

    private static Log log = LogFactory.getLog(PublisherThread.class);
    private final Meter publishRate;

    private SimplePublisher publisher;
    private AtomicInteger sentCount;

    public PublisherThread(SimplePublisher publisher) {
        this.publisher = publisher;
        sentCount = new AtomicInteger(0);
        publishRate = Main.metrics.meter(name(
                        "publisher", publisher.getConfigs().getQueueName(), "publisher id " + publisher.getConfigs().getId(), "meter")
        );

        // Per given period how many messages were sent is taken through this gauge
        Main.gauges.register(
                name("Publisher", publisher.getConfigs().getQueueName(),
                        "publisher id " + this.publisher.getConfigs().getId(), "gauge"),
                new Gauge<Integer>() {

                    /**
                     * number of messages sent since last call to this method is returned
                     * @return Integer
                     */
                    @Override
                    public Integer getValue() {
                        int val = sentCount.get();
                        sentCount.addAndGet(-val);
                        return val;
                    }
                });
    }

    @Override
    public void run() {
        if(publisher.getConfigs().isTransactional()) {
            transactionalPublish();
        } else {
            publish();
        }
    }

    private void publish() {
        long messageCount = publisher.getConfigs().getMessageCount();
        String publisherID = publisher.getConfigs().getId();
        PublisherConfig config = publisher.getConfigs();
        log.info("Starting publisher to send " + messageCount + " messages to ." +
                config.getQueueName() +
                "  Publisher ID: " + publisherID);
        Message message = null;

        try {
            for (int i = 1; i <= messageCount; i++) {

                message = publisher.createTextMessage(i + " Publisher: " + publisherID);
                message.setJMSMessageID(Integer.toString(i));
                publisher.send(message);

                if (log.isTraceEnabled()) {
                    log.trace("message published: " + message);
                }
                sentCount.incrementAndGet();
                publishRate.mark();

                if(config.getDelayBetweenMsgs() > 0) {
                    Thread.sleep(publisher.getConfigs().getDelayBetweenMsgs());
                }
            }

            log.info("Stopping publisher for " +
                    publisher.getConfigs().getQueueName() +
                    " [ Publisher ID: " + publisher.getConfigs().getId() + "  ]");

            publisher.close();
        } catch (JMSException e) {
            log.error("Exception occurred while publishing. " +
                    "\n\tPublisher ID: " + publisherID +
                    "\n\tMessage: " + message, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Stopped publisher for " +
                publisher.getConfigs().getQueueName() +
                " [ Publisher ID: " + publisher.getConfigs().getId() + "  ]");
    }

    private void transactionalPublish() {
        long messageCount = publisher.getConfigs().getMessageCount();
        String publisherID = publisher.getConfigs().getId();
        PublisherConfig config = publisher.getConfigs();

        log.info("Starting transactional publisher to send " + messageCount + " messages to " +
                publisher.getConfigs().getQueueName() +
                ". Publisher ID: " + publisherID);
        Message message;
        int batchSize = publisher.getConfigs().getTransactionBatchSize();
        List<Message> currentBatch = new ArrayList<Message>(batchSize);

        for (int i = 1; i <= messageCount; i++) {
            try {
                message = publisher.createTextMessage(i + " Publisher: " + publisherID);
                message.setJMSMessageID(Integer.toString(i));
                publisher.send(message);
                currentBatch.add(message);

                if (log.isTraceEnabled()) {
                    log.trace("message enqueued for transaction: " + message);
                }

                if(config.getDelayBetweenMsgs() > 0) {
                    Thread.sleep(publisher.getConfigs().getDelayBetweenMsgs());
                }

                if ((currentBatch.size() == batchSize) || (i == messageCount)) {

                    publisher.commit();
                    sentCount.addAndGet(currentBatch.size());
                    publishRate.mark(currentBatch.size());
                    currentBatch.clear();
                }
            } catch (JMSException e) {
                log.error("Exception occurred while transactional publishing", e);
                resend(currentBatch);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        log.info("Stopping transactional publisher. [ Publisher ID: " + publisher.getConfigs().getId() + "  ]");
        try {
            publisher.close();
        } catch (JMSException e) {
            log.error("Exception occurred while closing transactional publisher " + publisherID, e);
        }

        log.info("Stopped publisher. [ Publisher ID: " + publisher.getConfigs().getId() + "  ]");
    }

    private void resend(List<Message> currentBatch) {
        try {
            publisher.rollback();
            for (Message message : currentBatch) {
                publisher.send(message);
            }
            publisher.commit();
            sentCount.addAndGet(currentBatch.size());
            publishRate.mark(currentBatch.size());
            currentBatch.clear();
        } catch (JMSException e) {
            try {
                publisher.rollback();
                resend(currentBatch);
            } catch (JMSException e1) {
                log.error("Roll back failed on resend", e);
                resend(currentBatch);
            }
        }
    }
}
