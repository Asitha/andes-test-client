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

package org.atc.jms.topic;

import org.atc.SimpleConsumer;
import org.atc.config.SubscriberConfig;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.QueueSession;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class TestTopicSubscriber implements SimpleConsumer {

    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private TopicSubscriber topicSubscriber;
    private SubscriberConfig config;
    long messageCount;

    @Override
    public MessageConsumer subscribe(SubscriberConfig config) throws NamingException, JMSException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, config.getInitialContextFactory());
        properties.put(config.getConnectionFactoryPrefix() + "." + config.getConnectionFactoryName(), config.getTCPConnectionURL());
        InitialContext ctx = new InitialContext(properties);
        
        // Lookup connection factory
        TopicConnectionFactory connFactory = (TopicConnectionFactory) ctx.lookup(config.getConnectionFactoryName());
        topicConnection = connFactory.createTopicConnection();
        topicConnection.start();
        topicSession =
                topicConnection.createTopicSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        // Send message
        Topic topic = topicSession.createTopic(config.getQueueName());
        javax.jms.TopicSubscriber topicSubscriber = topicSession.createSubscriber(topic);

        messageCount = config.getMessageCount();
        this.topicSubscriber = topicSubscriber;
        this.config = config;
        return topicSubscriber;
    }

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
        topicConnection.stop();
    }

    @Override
    public void unsubscribe() {
    }

}
