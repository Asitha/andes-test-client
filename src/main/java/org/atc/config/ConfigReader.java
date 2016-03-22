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

import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class ConfigReader {

    private ConfigReader() {
    }

    public static final int RESEND_WAIT_INTERVAL_MILLISECONDS = 1000;

    public static TestConfiguration parseConfig(final String filePath) throws FileNotFoundException, NoSuchFieldException, IllegalAccessException {

        InputStream input = new FileInputStream(new File(filePath));
        Yaml yaml = new Yaml();
        yaml.setBeanAccess(BeanAccess.FIELD);
        TestConfiguration testConfiguration = yaml.loadAs(input, TestConfiguration.class);

        preProcessTopicPublishers(testConfiguration);
        preProcessQueuePublishers(testConfiguration);
        preProcessTopicSubscribers(testConfiguration);
        preProcessQueueSubscribers(testConfiguration);
        preProcessDurableTopicSubscribers(testConfiguration);

        return testConfiguration;
    }

    private static void addGlobalConfigerationsIfAbsent(TestConfiguration tc, List pubsubList) {
        for(Object obj: pubsubList) {
            PubSubConfig pubSubConfig = (PubSubConfig) obj ;
            pubSubConfig.addGlobalConfigurationsIfAbsent(tc);
        }
    }

    private static void preProcessDurableTopicSubscribers(TestConfiguration tc) throws NoSuchFieldException, IllegalAccessException {
        addGlobalConfigerationsIfAbsent(tc, tc.getDurableTopicSubscribers());
        createDuplicateSubscribersIfNeeded(tc.getDurableTopicSubscribers());
    }

    private static void preProcessQueueSubscribers(TestConfiguration tc) throws NoSuchFieldException, IllegalAccessException {
        addGlobalConfigerationsIfAbsent(tc, tc.getQueueSubscribers());
        createDuplicateSubscribersIfNeeded(tc.getQueueSubscribers());
    }

    private static void preProcessTopicSubscribers(TestConfiguration tc) throws NoSuchFieldException, IllegalAccessException {
        addGlobalConfigerationsIfAbsent(tc, tc.getTopicSubscribers());
        createDuplicateSubscribersIfNeeded(tc.getTopicSubscribers());
    }

    private static void preProcessQueuePublishers(TestConfiguration tc) throws NoSuchFieldException, IllegalAccessException {
        addGlobalConfigerationsIfAbsent(tc, tc.getQueuePublishers());
        createDuplicatePublishersIfNeeded(tc.getQueuePublishers());
    }

    private static void preProcessTopicPublishers(TestConfiguration tc) throws NoSuchFieldException, IllegalAccessException {
        addGlobalConfigerationsIfAbsent(tc, tc.getTopicPublishers());
        createDuplicatePublishersIfNeeded(tc.getTopicPublishers());
    }

    private static void createDuplicateSubscribersIfNeeded(List<SubscriberConfig> subscriberConfigList) throws NoSuchFieldException, IllegalAccessException {
        int startPos = subscriberConfigList.size() - 1;
        for (int i = startPos; i > -1; i--) {
            SubscriberConfig sc = subscriberConfigList.get(i);
            int copyCount = sc.getParallelThreads() - 1; // minus the current copy
            for (int j = 0; j < copyCount; j++) {
                SubscriberConfig copy = sc.copy();
                copy.setId(sc.getId() + "__" + (j+2));
                if(StringUtils.isNotBlank(sc.getSubscriptionID())) {
                    copy.setSubscriptionID(sc.getSubscriptionID() + "__" + (j+2));
                }
                subscriberConfigList.add(copy);
            }
        }
    }

    private static void createDuplicatePublishersIfNeeded(List<PublisherConfig> publisherList) throws NoSuchFieldException, IllegalAccessException {
        int startPos = publisherList.size() - 1;
        for (int i = startPos; i > -1; i--) {
            PublisherConfig sc = publisherList.get(i);
            int copyCount = sc.getParallelThreads() - 1; // minus the current copy
            for (int j = 0; j < copyCount; j++) {
                PublisherConfig copy = sc.copy();
                copy.setId(sc.getId() + "__" + (j+2));
                publisherList.add(copy);
            }
        }
    }
}
