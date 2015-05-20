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

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.atc.Main.CONFIG_FILE_PATH;

public class ConfigReader {

    public static final String HOSTNAME = "hostname";
    public static final String PORT = "port";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    public static final String ICF = "initial_context_factory";
    public static final String CF_NAME_PREFIX = "connection_factory_name_prefix";
    public static final String CF_NAME = "connection_factory_name";
    public static final String CLIENT_ID = "client_id";
    public static final String VIRTUALHOST_NAME = "virtualhost_name";

    public static final String TRANSACTIONAL_CLIENT = "transaction_enable";
    public static final String TRANSACTION_BATCH_SIZE = "transaction_batch_size";

    public static final String TOPIC_PUBLISHERS = "topic_publishers";
    public static final String MESSAGE_COUNT = "message_count";
    public static final String PARALLEL_THREADS = "parallel_threads";
    public static final String SUBSCRIPTION_ID = "sub_id";
    public static final String PRINT_PER_MESSAGES = "print_per_messages";
    
    public static final String QUEUE_NAME = "queue_name";

    public static final String TOPIC_SUBSCRIBERS = "topic_subscribers";
    
    public static final String QUEUE_SUBSCRIBERS = "queue_subscribers";
    
    public static final String QUEUE_PUBLISHERS = "queue_publishers";
    
    public static final String DURABLE_SUBSCRIPTION = "durable_subscribers";

    public static final String FAILOVER_PARAMS = "failover_params";

    public static final String PUB_SUB_ID = "id";

    public static final String UNSUB_ON_FINISH = "unsubscribe_on_finish";

    public static final String DELAY_BETWEEN_MESSAGES = "delay_between_messages";

    public static final String CONSOLE_REPORT = "console_report_enable";

    public static final String CONSOLE_REPORT_UPDATE_INTERVAL = "console_report_update_interval_seconds";

    public static final String JMX_REPORT = "jmx_report_enable";
    
    public static final String CSV_REPORT = "csv_report_enable";
    
    public static final String CSV_UPDATE_INTERVAL = "csv_report_update_interval_seconds";

    public static final String CSV_GAUGE_UPDATE_INTERVAL = "csv_gauges_update_interval_milis";

    public static TestConfiguration parseConfig(final String filePath) throws FileNotFoundException, CloneNotSupportedException {

        InputStream input = new FileInputStream(new File(filePath));
        Yaml yaml = new Yaml();
        Map map = (Map) yaml.load(input);

        GlobalConfig globalConfig = new GlobalConfig(map.get(USERNAME), 
                map.get(PASSWORD),
                map.get(HOSTNAME), 
                map.get(PORT),
                map.get(ICF),
                map.get(CF_NAME_PREFIX),
                map.get(CF_NAME),
                map.get(CLIENT_ID),
                map.get(VIRTUALHOST_NAME),
                map.get(PRINT_PER_MESSAGES),
                map.get(CONSOLE_REPORT),
                map.get(CONSOLE_REPORT_UPDATE_INTERVAL),
                map.get(JMX_REPORT),
                map.get(CSV_REPORT),
                map.get(CSV_UPDATE_INTERVAL),
                map.get(CSV_GAUGE_UPDATE_INTERVAL)
        );

        TestConfiguration testConfiguration = new TestConfiguration(globalConfig);

        // configure publishers
        List<PublisherConfig> publisherConfigList;
        Object section = map.get(TOPIC_PUBLISHERS);
        publisherConfigList = populatePublisherConfigs(section, globalConfig);
        testConfiguration.addTopicPublisherList(publisherConfigList);
        
        section = map.get(QUEUE_PUBLISHERS);
        publisherConfigList = populatePublisherConfigs(section, globalConfig);
        testConfiguration.addQueuePublisherConfigList(publisherConfigList);
        
        // configure subscribers
        List<SubscriberConfig> subscriberConfigList;        
        section = map.get(TOPIC_SUBSCRIBERS);
        subscriberConfigList = populateSubscriberConfigs(section,globalConfig);
        testConfiguration.addTopicSubscriberConfigList(subscriberConfigList);
        
        section = map.get(QUEUE_SUBSCRIBERS);
        subscriberConfigList = populateSubscriberConfigs(section,globalConfig);
        testConfiguration.addQueueSubscriberConfigList(subscriberConfigList);
        
        section = map.get(DURABLE_SUBSCRIPTION);
        subscriberConfigList = populateSubscriberConfigs(section, globalConfig);
        testConfiguration.addDurableSubscriberConfigList(subscriberConfigList);

        return testConfiguration;
    }
    
    private static List<SubscriberConfig> populateSubscriberConfigs(Object section, GlobalConfig globalConfig) throws CloneNotSupportedException {

        List<SubscriberConfig> subscriberConfigsList;
        if (null != section) {
            List subscriberList = (List) section;
            subscriberConfigsList = new ArrayList<SubscriberConfig>(subscriberList.size());
            for (Object subscriber : subscriberList) {
                Map subscriberInfo = (Map) subscriber;
                SubscriberConfig subscriberConfig = new SubscriberConfig(globalConfig);

                updateGlobalConfigs(subscriberInfo, subscriberConfig);
                populatePubSubConfigs(subscriberInfo, subscriberConfig);
                
                Object val = subscriberInfo.get(SUBSCRIPTION_ID);
                if(null != val) {
                    String subID;
                    if (val instanceof Integer) {
                        subID = val.toString();
                    } else if (val instanceof Long) {
                        subID = val.toString();
                    } else {
                        subID = val.toString();
                    }
                    subscriberConfig.setSubscriptionID(subID);
                }

                val = subscriberInfo.get(UNSUB_ON_FINISH);
                if(null != val) {
                    if (val instanceof Boolean) {
                        subscriberConfig.setUnsubOnFinish((Boolean)val);
                    } else {
                        throw new IllegalArgumentException("Boolean value expected for " + UNSUB_ON_FINISH +
                                "found " + val + "[ " + val.getClass() + " ]");
                    }
                }
                
                subscriberConfigsList.add(subscriberConfig);

                for (int i = 1; i < subscriberConfig.getParallelPublishers(); i++) {
                    SubscriberConfig s = subscriberConfig.newSubscriberConfig(
                            subscriberConfig.getId() + "-" + Integer.toString(i));
                    subscriberConfigsList.add(s);
                }
            }
        } else {
            subscriberConfigsList = new ArrayList<SubscriberConfig>(0);
        }
        return subscriberConfigsList;
    }
    
    private static List<PublisherConfig> populatePublisherConfigs(Object section, GlobalConfig globalConfig) {
        List<PublisherConfig> publisherConfigList;
        if (null != section) {
            List publisherList = (List) section;
            publisherConfigList = new ArrayList<PublisherConfig>(publisherList.size());
            for (Object publisher : publisherList) {
                Map publisherInfo = (Map) publisher;
                PublisherConfig publisherConfig = new PublisherConfig(globalConfig);

                updateGlobalConfigs(publisherInfo, publisherConfig);
                populatePubSubConfigs(publisherInfo, publisherConfig);
                publisherConfigList.add(publisherConfig);

                for (int i = 1; i < publisherConfig.getParallelPublishers(); i++) {
                    PublisherConfig p = publisherConfig.newPublisherConfig(publisherConfig.getId() +
                            "-" + Integer.toString(i));
                    publisherConfigList.add(p);
                }
            }
        } else {
            publisherConfigList = new ArrayList<PublisherConfig>(0);
        }
        
        return publisherConfigList;
    }

    private static void populatePubSubConfigs(Map yamlSection, PubSubConfig pubSubConfig) {

        Object value = yamlSection.get(MESSAGE_COUNT);

        if (null == value) {
            throw new IllegalArgumentException(MESSAGE_COUNT + " should be non empty in configuration file " + CONFIG_FILE_PATH);
        }

        if (value instanceof Integer) {
            pubSubConfig.setMessageCount((Integer) value);
        } else if (value instanceof Long) {
            pubSubConfig.setMessageCount((Long) value);
        } else {
            throw new IllegalArgumentException(MESSAGE_COUNT + " type " + value.getClass()
                    + " not valid in configuration file " + CONFIG_FILE_PATH);
        }

        value = yamlSection.get(QUEUE_NAME);
        if(value == null) {
            throw new IllegalArgumentException(QUEUE_NAME + " should be set for publisher in configuration " + CONFIG_FILE_PATH);
        } else {
            pubSubConfig.setQueueName(value.toString());
        }
        
        value = yamlSection.get(PARALLEL_THREADS);
        if (null != value) {
            pubSubConfig.setParallelPublishers((Integer) value);
        } else {
            pubSubConfig.setParallelPublishers(1); // defaults to 1 publisher
        }

        value= yamlSection.get(PUB_SUB_ID);
        if(null != value) {
            pubSubConfig.setId(value.toString());
        } else {
            pubSubConfig.setId(UUID.randomUUID().toString());
        }

        value = yamlSection.get(FAILOVER_PARAMS);
        if (null != value) {
            pubSubConfig.setFailoverParams(value.toString());
        }

        value = yamlSection.get(TRANSACTIONAL_CLIENT);
        if(null != value) {
            pubSubConfig.setTransactional((Boolean)value);
        } else {
            pubSubConfig.setTransactional(false);
        }

        value = yamlSection.get(TRANSACTION_BATCH_SIZE);
        if(null != value) {
            pubSubConfig.setTransactionBatchSize((Integer)value);
        } else {
            pubSubConfig.setTransactionBatchSize(1); // default
        }

        value = yamlSection.get(DELAY_BETWEEN_MESSAGES);
        if (null != value) {
            if(value instanceof Integer ) {
                pubSubConfig.setDelayBetweenMsgs((Integer)value);
            } else {
                throw new IllegalArgumentException("Expects an type Integer for " + DELAY_BETWEEN_MESSAGES +
                        ". Found " + value + " [ " + value.getClass() +" ]" );
            }
        }
    }

    private static void updateGlobalConfigs(Map yamlSection, GlobalConfig config) {

        Object value = yamlSection.get(HOSTNAME);
        if (null != value) {
            config.setHostname(value);
        }
        value = yamlSection.get(PORT);
        if (null != value) {
            config.setPort(value);
        }
        value = yamlSection.get(USERNAME);
        if (null != value) {
            config.setUsername(value);
        }

        value = yamlSection.get(PASSWORD);
        if (null != value) {
            config.setPassword(value);
        }
        
        value = yamlSection.get(ICF);
        if (null != value) {
            config.setInitialContextFactory(value);
        }
        value = yamlSection.get(CF_NAME_PREFIX);
        if (null != value) {
            config.setConnectionFactoryPrefix(value);
        }
        value = yamlSection.get(CF_NAME);
        if (null != value) {
            config.setConnectionFactoryName(value);
        }

        value = yamlSection.get(CLIENT_ID);
        if (null != value) {
            config.setClientID(value);
        }

        value = yamlSection.get(VIRTUALHOST_NAME);
        if (null != value) {
            config.setVirtualHostName(value);
        }

        value = yamlSection.get(PRINT_PER_MESSAGES);
        if(null != value) {
            if( value instanceof Integer) {
                config.setPrintPerMessages(value);
            } else {
                throw new IllegalArgumentException("Value for " + PRINT_PER_MESSAGES + " require type Integer. Found " +
                        " type " + value.getClass());
            }
        }
    }

}
