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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atc.config.ConfigReader;
import org.atc.config.PublisherConfig;

import java.util.concurrent.atomic.AtomicInteger;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * This class publishes messages of a single {@link org.atc.SimplePublisher} to the broker.
 * Can be used to publish in a separate thread.
 */
public class PublisherThread implements Runnable {

    private static Log log = LogFactory.getLog(PublisherThread.class);
    private static final String DEFAULT_CONTENT = "Test Message";

    private final Meter publishRate;
    private SimplePublisher publisher;

    private AtomicInteger sentCount;

    public PublisherThread(SimplePublisher publisher) {
        this.publisher = publisher;
        sentCount = new AtomicInteger(0);
        publishRate = Main.METRICS.meter(name(
                "publisher", publisher.getConfigs().getQueueName(),
                "publisher id " + publisher.getConfigs().getId(),
                "meter")
        );

        // Messages sent for a given time period is collected through this gauge
        Main.GAUGES.register(
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

    public final void run() {
        if (publisher.getConfigs().isTransactional()) {
            transactionalPublish();
        } else {
            publish();
        }
    }

    private void publish() {
        long messageCount = publisher.getConfigs().getMessageCount();
        String publisherID = publisher.getConfigs().getId();
        PublisherConfig config = publisher.getConfigs();
        log.info("Starting publisher to send " + messageCount + " messages to ." + config.getQueueName() +
                "  Publisher ID: " + publisherID);
        ATCMessage atcMessage = null;
        String messageContent = config.getMessageContent();
        if(StringUtils.isEmpty(messageContent)) {
            messageContent = DEFAULT_CONTENT;
        }

        try {
            for (int i = 1; i <= messageCount; i++) {
                atcMessage = publisher.createTextMessage(messageContent);
                atcMessage.setMessageID(publisherID + "-" + i);
                publisher.send(atcMessage);

                if (log.isDebugEnabled()) {
                    log.debug("Message published: " + atcMessage);
                }
                sentCount.incrementAndGet();
                publishRate.mark();

                if (config.getDelayBetweenMsgs() > 0) {
                    Thread.sleep(publisher.getConfigs().getDelayBetweenMsgs());
                }
            }

            log.info("Stopping publisher for " + publisher.getConfigs().getQueueName() +
                    " [ Publisher ID: " + publisher.getConfigs().getId() + "  ]");

            publisher.close();
        } catch (ATCException e) {
            log.error("Exception occurred while publishing.\n\tPublisher ID: " + publisherID + "\n\tMessage: "
                    + atcMessage, e);
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

        log.info("Starting transactional publisher to send " + messageCount + " messages to " +
                publisher.getConfigs().getQueueName() + ". Publisher ID: " + publisherID);
        ATCMessage atcMessage;
        int batchSize = publisher.getConfigs().getTransactionBatchSize();

        DisruptorBasedPublisher disruptorPublisher =
                new DisruptorBasedPublisher(batchSize, publisher, sentCount, publishRate);

        for (int i = 1; i <= messageCount; i++) {
            try {
                atcMessage = publisher.createTextMessage(i + " Publisher: " + publisherID);
                atcMessage.setMessageID(Integer.toString(i));
                disruptorPublisher.publish(atcMessage);
            } catch (ATCException e) {
                log.error("Exception occurred while creating message for publisher " + publisherID, e);
                i--; // resend
                try {
                    Thread.sleep(ConfigReader.RESEND_WAIT_INTERVAL_MILLISECONDS); // wait an send again
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt(); // on interrupt exception throw it.
                }
            }
        }

        log.info("Stopping transactional publisher. [ Publisher ID: " + publisher.getConfigs().getId() + "  ]");
        disruptorPublisher.closePublisher();
        disruptorPublisher.shutdown();
        log.info("Stopped publisher. [ Publisher ID: " + publisher.getConfigs().getId() + "  ]");
    }
}
