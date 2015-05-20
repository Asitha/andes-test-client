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

public class PublisherConfig extends PubSubConfig {

    private int publisherMaxThroughput;
    private String messageContent;

    
    public PublisherConfig(GlobalConfig globalConfig) {
        super(globalConfig);
    }

    public PublisherConfig(PublisherConfig config) {
        super(config);
        messageContent = config.getMessageContent();
        publisherMaxThroughput = config.getPublisherMaxThroughput();
    }

    public int getPublisherMaxThroughput() {
        return publisherMaxThroughput;
    }

    public void setPublisherMaxThroughput(int publisherMaxThroughput) {
        this.publisherMaxThroughput = publisherMaxThroughput;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    public PublisherConfig newPublisherConfig(String id) {
        PublisherConfig p = new PublisherConfig(this);
        p.setId(id);
        return p;
    }
}
