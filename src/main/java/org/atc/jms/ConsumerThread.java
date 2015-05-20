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

package org.atc.jms;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atc.Main;

import javax.jms.JMSException;
import javax.jms.Message;

import java.util.concurrent.atomic.AtomicInteger;

import static com.codahale.metrics.MetricRegistry.name;

public class ConsumerThread implements Runnable {

    SimpleJMSConsumer jmsConsumer;
    private static Log log = LogFactory.getLog(ConsumerThread.class);
    private final Histogram latencyHist;
    private final Meter consumerRate;
    private final AtomicInteger receivedCount;

    private final Histogram globalLatencyHist;
    private final Meter globalConsumerRate;

    public ConsumerThread(SimpleJMSConsumer consumer, Histogram globalLatency, Meter globalConsumerRate) {
        jmsConsumer = consumer;
        receivedCount = new AtomicInteger(0);
        latencyHist = Main.metrics.histogram(
                name(ConsumerThread.class, "consumer", jmsConsumer.getConfigs().getId(), "latency")
        );
        consumerRate = Main.metrics.meter(
                name(ConsumerThread.class, "consumer", jmsConsumer.getConfigs().getId(), "rate"));

        // Per given period how many messages were sent is taken through this gauge
        Main.gauges.register(
                name(ConsumerThread.class, jmsConsumer.getConfigs().getId(), "receiving-stats"),
                new Gauge<Integer>() {

            @Override
            public Integer getValue() {
                int val = receivedCount.get();
                receivedCount.addAndGet(-val);
                return val;
            }
        });

        this.globalConsumerRate = globalConsumerRate;
        this.globalLatencyHist = globalLatency;
    }


    @Override
    public void run() {

        long messageCount = jmsConsumer.getConfigs().getMessageCount();
        String consumerID = jmsConsumer.getConfigs().getId();

        log.info("Starting consumer to receive " + messageCount + " messages. Consumer ID: " + consumerID);
        Message message = null;

        try {
            long latency;
            for (int i = 1; i <= messageCount; i++) {

                message = jmsConsumer.receive();

                latency = System.currentTimeMillis() - message.getJMSTimestamp();
                latencyHist.update(latency);
                globalLatencyHist.update(latency);
                receivedCount.incrementAndGet();

                consumerRate.mark();
                globalConsumerRate.mark();
                if(log.isTraceEnabled()) {
                    log.trace("message received: " + message);
                }
            }

            log.info("Stopping consumer. [ Consumer ID: " + consumerID + "  ]");
            if(jmsConsumer.getConfigs().isUnsubOnFinish()) {
                jmsConsumer.unsubscribe();
                log.info("Un-subscribing consumer [ Consumer ID: " + consumerID + " ]");
            } else {
                jmsConsumer.close();
                log.info("Consumer disconnected [ Consumer ID: " + consumerID + " ]");
            }
        } catch (JMSException e) {
            log.error("Exception occurred while consuming. " +
                    "\n\tconsumer ID: " + consumerID +
                    "\n\tMessage: " + message, e);
        }

        log.info("Stopped consumer. [ Consumer ID: " + consumerID + "  ]");
    }
}
