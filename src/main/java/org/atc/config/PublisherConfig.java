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

package org.atc.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
public class PublisherConfig extends PubSubConfig {

    @XmlAttribute
    private int publisherMaxThroughput;
    @XmlAttribute
    private String messageContent;

    public final int getPublisherMaxThroughput() {
        return publisherMaxThroughput;
    }

    final void setPublisherMaxThroughput(int publisherMaxThroughput) {
        this.publisherMaxThroughput = publisherMaxThroughput;
    }

    public final String getMessageContent() {
        return messageContent;
    }

    final void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    final PublisherConfig copy() throws NoSuchFieldException, IllegalAccessException {
        PublisherConfig copy = new PublisherConfig();
        copyMembers(this, copy);
        return copy;
    }
}
