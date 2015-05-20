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

import java.util.ArrayList;
import java.util.List;

public class TestConfiguration {
    
    private GlobalConfig globalConfig;
    
    private List<PublisherConfig> topicPublisherConfigList;
    
    private List<PublisherConfig> queuePublisherConfigList;
    
    private List<SubscriberConfig> queueSubscriberConfigList;
    
    private List<SubscriberConfig> topicSubscriberConfigList;
    
    private List<SubscriberConfig> durableSubscriberConfigList;
    
    public TestConfiguration(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
        topicPublisherConfigList = new ArrayList<PublisherConfig>();
        topicSubscriberConfigList = new ArrayList<SubscriberConfig>();
        queuePublisherConfigList = new ArrayList<PublisherConfig>();
        queueSubscriberConfigList = new ArrayList<SubscriberConfig>();
        durableSubscriberConfigList = new ArrayList<SubscriberConfig>();
    }
    
    public void addTopicPublisherList(List<PublisherConfig> publisherConfigList) {
        topicPublisherConfigList.addAll(publisherConfigList);
    }
    
    public List<PublisherConfig> getTopicPublisherList() {
        return topicPublisherConfigList;
    }
    
    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    public List<SubscriberConfig> getTopicSubscriberConfigList() {
        return topicSubscriberConfigList;
    }

    public void addTopicSubscriberConfigList(List<SubscriberConfig> subscriberConfigList) {
        topicSubscriberConfigList.addAll(subscriberConfigList);
    }

    public List<PublisherConfig> getQueuePublisherConfigList() {
        return queuePublisherConfigList;
    }

    public void addQueuePublisherConfigList(List<PublisherConfig> queuePublisherConfigList) {
        this.queuePublisherConfigList.addAll(queuePublisherConfigList);
    }

    public List<SubscriberConfig> getQueueSubscriberConfigList() {
        return queueSubscriberConfigList;
    }

    public void addQueueSubscriberConfigList(List<SubscriberConfig> queueSubscriberConfigList) {
        this.queueSubscriberConfigList.addAll(queueSubscriberConfigList);
    }

    public List<SubscriberConfig> getDurableSubscriberConfigList() {
        return durableSubscriberConfigList;
    }

    public void addDurableSubscriberConfigList(List<SubscriberConfig> durableSubscriberConfigList) {
        this.durableSubscriberConfigList.addAll(durableSubscriberConfigList);
    }
}
