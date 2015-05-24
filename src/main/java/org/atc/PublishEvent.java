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

import com.lmax.disruptor.EventFactory;

/**
 * Used by {@link com.lmax.disruptor.dsl.Disruptor} to handle transactional publishing. This is
 * the container for the publish events
 */
public class PublishEvent {

    enum EventType {

        /**
         * Message publishing event
         */
        MessageEvent,

        /**
         * Close publisher event
         */
        CLOSE_PUB;
    }

    private ATCMessage atcMessage;
    private EventType type;

    public ATCMessage getAtcMessage() {
        return atcMessage;
    }

    public void setAtcMessage(ATCMessage atcMessage) {
        this.atcMessage = atcMessage;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public static class PublishEventFactory implements EventFactory<PublishEvent> {

        @Override
        public PublishEvent newInstance() {
            return new PublishEvent();
        }
    }

    public static EventFactory<PublishEvent> getFactory() {
        return new PublishEventFactory();
    }

    public void clear() {
        atcMessage = null;
        type = null;
    }

}
