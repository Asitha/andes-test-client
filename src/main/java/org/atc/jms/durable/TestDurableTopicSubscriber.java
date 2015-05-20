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

package org.atc.jms.durable;

import org.atc.jms.SimpleJMSConsumer;
import org.atc.config.SubscriberConfig;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class TestDurableTopicSubscriber implements SimpleJMSConsumer {

    private String subscriptionId;
    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private TopicSubscriber topicSubscriber;
    private SubscriberConfig config;

    @Override
    public SubscriberConfig getConfigs() {
        return config;
    }

    @Override
    public Message receive() throws JMSException {
        return topicSubscriber.receive();
    }

    @Override
    public void close() throws JMSException {
        topicSubscriber.close();
        topicSession.close();
        topicConnection.close();
    }

    @Override
    public void unsubscribe() throws JMSException {
        topicSession.unsubscribe(subscriptionId);
    }

    @Override
    public MessageConsumer subscribe(SubscriberConfig conf) throws NamingException, JMSException {

        String topicName = conf.getQueueName();
        subscriptionId = conf.getSubscriptionID();
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, conf.getInitialContextFactory());
        properties.put(conf.getConnectionFactoryPrefix() + "." + conf.getConnectionFactoryName(), conf.getTCPConnectionURL());
        properties.put("topic." + topicName, topicName);
        InitialContext ctx = new InitialContext(properties);
        // Lookup connection factory
        TopicConnectionFactory connFactory = (TopicConnectionFactory) ctx.lookup(conf.getConnectionFactoryName());
        topicConnection = connFactory.createTopicConnection();
        topicConnection.start();
        topicSession = topicConnection.createTopicSession(false, QueueSession.AUTO_ACKNOWLEDGE);

        // create durable subscriber with subscription ID
        Topic topic = (Topic) ctx.lookup(topicName);
        topicSubscriber = topicSession.createDurableSubscriber(topic, subscriptionId);
        config = conf;
        return topicSubscriber;
    }
}
