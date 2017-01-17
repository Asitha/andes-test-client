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
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atc.config.SubscriberConfig;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * This thread handles a single {@link org.atc.SimpleConsumer} and logs all the stats
 */
public class ConsumerThread implements Runnable {

    private SimpleConsumer consumer;
    private static Log log = LogFactory.getLog(ConsumerThread.class);
    private final Histogram latencyHist;
    private final Meter consumerRate;
    private final AtomicInteger receivedCount;

    private final Histogram globalLatencyHist;
    private final Meter globalConsumerRate;

    /**
     * Creates a new consumer thread for a given consumer
     * @param consumer Reference to the {@link org.atc.SimpleConsumer} implementation that need to run
     * @param globalLatency Metrics {@link com.codahale.metrics.Histogram} that calculates global latency
     * @param globalConsumerRate Metrics {@link com.codahale.metrics.Meter} that calculates global consumer rate
     */
    public ConsumerThread(SimpleConsumer consumer, Histogram globalLatency, Meter globalConsumerRate) {
        this.consumer = consumer;
        receivedCount = new AtomicInteger(0);
        latencyHist = Main.METRICS.histogram(
                name("consumer", consumer.getConfigs().getQueueName(),
                        "consumer id " + this.consumer.getConfigs().getId(), "latency")
        );
        consumerRate = Main.METRICS.meter(
                name("consumer", consumer.getConfigs().getQueueName(),
                        "consumer id " + this.consumer.getConfigs().getId(), "rate"));

        // Per given period how many messages were sent is taken through this gauge
        Main.GAUGES.register(
                name(ConsumerThread.class, this.consumer.getConfigs().getId(), "receiving-stats"),
                new Gauge<Integer>() {

            public Integer getValue() {
                int val = receivedCount.get();
                receivedCount.addAndGet(-val);
                return val;
            }
        });

        this.globalConsumerRate = globalConsumerRate;
        this.globalLatencyHist = globalLatency;
    }

    public final void run() {

        long messageCount = consumer.getConfigs().getMessageCount();
        String consumerID = consumer.getConfigs().getId();
        SubscriberConfig config = consumer.getConfigs();
        log.info("Starting consumer to receive " + messageCount + " messages from " + config.getQueueName() +
                " Consumer ID: " + consumerID);
        ATCMessage message = null;
        RateLimiter rateLimiter = null;
        if (config.getMessagesPerSecond() != 0) {
            rateLimiter = RateLimiter.create(config.getMessagesPerSecond());
        }
        try {
            long latency;
            for (int i = 1; i <= messageCount; i++) {

                if (null != rateLimiter) {
                    rateLimiter.acquire(); // wait for a permit to publish or block
                }
                message = consumer.receive();

                if (config.getReceiveWaitTimeMillis() > 0) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(config.getReceiveWaitTimeMillis());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                latency = System.currentTimeMillis() - message.getTimeStamp();
                latencyHist.update(latency);
                globalLatencyHist.update(latency);
                receivedCount.incrementAndGet();

                consumerRate.mark();
                globalConsumerRate.mark();
                if(log.isDebugEnabled()) {
                    log.debug("Message received: " + message);
                }

                if(config.getDelayBetweenMsgs() > 0) {
                    Thread.sleep(config.getDelayBetweenMsgs());
                }
            }

            log.info("Stopping consumer. [ Consumer ID: " + consumerID + "  ]");
            if(consumer.getConfigs().isUnsubscribeOnFinish()) {
                consumer.unsubscribe();
                consumer.close();
                log.info("Un-subscribing consumer for " + config.getQueueName() +
                        " [ Consumer ID:9 " + consumerID + " ]");
            } else {
                consumer.close();
                log.info("Consumer disconnected [ Consumer ID: " + consumerID + " ]");
            }
        } catch (ATCException e) {
            log.error("Exception occurred while consuming. " +
                    "\n\tconsumer ID: " + consumerID +
                    "\n\tMessage: " + message, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Stopped consumer. [ Consumer ID: " + consumerID + "  ]");
    }
}
