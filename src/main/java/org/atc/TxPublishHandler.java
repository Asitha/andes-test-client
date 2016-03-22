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
import com.lmax.disruptor.EventHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atc.config.ConfigReader;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Transactional publish handler dor {@link org.atc.DisruptorBasedPublisher}
 */
class TxPublishHandler implements EventHandler<PublishEvent> {

    private static Log log = LogFactory.getLog(TxPublishHandler.class);

    private final List<ATCMessage> messagesList;
    private final int batchSize;
    private final SimplePublisher publisher;
    private final AtomicInteger sentCount;
    private final Meter publishRate;

    /**
     * Creates the transactional publish handler
     * @param batchSize Transaction batch size
     * @param publisher SimplePublisher
     * @param sentCount Sent message within a time period tracker
     * @param publishRate Metrics publish rate calculating meter
     */
    TxPublishHandler(int batchSize, SimplePublisher publisher, AtomicInteger sentCount, Meter publishRate) {
        messagesList = new ArrayList<>(batchSize);
        this.batchSize = batchSize;
        this.publisher = publisher;
        this.sentCount = sentCount;
        this.publishRate = publishRate;
    }

    public void onEvent(PublishEvent event, long sequence, boolean endOfBatch) throws ATCException {

        try {
            if(event.getType() != PublishEvent.EventType.CLOSE_PUB ) {
                messagesList.add(event.getAtcMessage());
                publisher.send(event.getAtcMessage());
                if(log.isDebugEnabled()) {
                    log.debug("[ sequence: " + sequence + " ] publish event. Message " + event.getAtcMessage());
                }
            }

            if (endOfBatch || (messagesList.size() == batchSize)) {
                publisher.commit();
                sentCount.addAndGet(messagesList.size());
                publishRate.mark(messagesList.size());

                if (log.isDebugEnabled()) {
                    log.debug("Messages committed. Batch size " + messagesList.size());
                }
                messagesList.clear();
            }

            if (event.getType() == PublishEvent.EventType.CLOSE_PUB) {
                publisher.close();
            }
        } catch (ATCException e) {
            log.error("Publish failed for publisher " + publisher.getConfigs().getId(), e);
            resend();
        } finally {
            event.clear();
        }
    }

    /**
     * Try to resend failed messages
     */
    private void resend() {

        try {
            Thread.sleep(ConfigReader.RESEND_WAIT_INTERVAL_MILLISECONDS);
            publisher.rollback();
            for (ATCMessage atcMessage : messagesList) {
                publisher.send(atcMessage);
            }

            publisher.commit();
            sentCount.addAndGet(messagesList.size());
            publishRate.mark(messagesList.size());
            messagesList.clear();
        } catch (ATCException e) {
            log.error("Failed to re-publish. Publisher id " + publisher.getConfigs().getId(), e);
            resend();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
