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

import com.codahale.metrics.Meter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is used to transactional message publishing
 */
public class DisruptorBasedPublisher {

    private static Log log = LogFactory.getLog(DisruptorBasedPublisher.class);

    private Disruptor<PublishEvent> disruptor;
    ExecutorService executorPool;
    private static final int EXECUTOR_POOL_SHUTDOWN_WAIT_TIME = 10;

    DisruptorBasedPublisher(int batchSize, SimplePublisher publisher, AtomicInteger sentCount, Meter publishRate) {

        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("Disruptor Publisher Thread. Publisher " +
                        publisher.getConfigs().getId()).build();
        executorPool = Executors.newCachedThreadPool(namedThreadFactory);

        int bufferSize = 4096;

        disruptor = new Disruptor<PublishEvent>(
                PublishEvent.getFactory(),
                bufferSize,
                executorPool,
                ProducerType.SINGLE,
                new BlockingWaitStrategy());

        disruptor.handleEventsWith(new TxPublishHandler(batchSize, publisher, sentCount, publishRate));
        disruptor.start();
    }

    /**
     * Publish a message to disruptor (Eventually this will be published to broker)
     * @param ATCMessage message to be published to disruptor
     */
    void publish(ATCMessage ATCMessage) {

        RingBuffer<PublishEvent> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        PublishEvent evt = ringBuffer.get(sequence);
        evt.setAtcMessage(ATCMessage);
        evt.setType(PublishEvent.EventType.MessageEvent);
        ringBuffer.publish(sequence);

        if (log.isDebugEnabled()) {
            log.debug("[ sequence: " + sequence + " ] Transaction message published to disruptor. ");
        }
    }

    /**
     * Close publisher event is published to disruptor
     */
    void closePublisher() {
        RingBuffer<PublishEvent> ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();
        PublishEvent evt = ringBuffer.get(sequence);
        evt.setType(PublishEvent.EventType.CLOSE_PUB);
        ringBuffer.publish(sequence);

        if (log.isDebugEnabled()) {
            log.debug("[ sequence: " + sequence + " ] Publisher close event published to disruptor. ");
        }
    }

    /**
     * Shuts down disruptor after processing all pending events. If new events were published disruptor will
     * wait for them to finish as well.
     */
    public void shutdown() {
        disruptor.shutdown();
        executorPool.shutdown();
        try {
            executorPool.awaitTermination(EXECUTOR_POOL_SHUTDOWN_WAIT_TIME, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
