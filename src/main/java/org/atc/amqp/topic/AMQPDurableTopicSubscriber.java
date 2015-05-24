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

package org.atc.amqp.topic;

import org.atc.ATCException;
import org.atc.ATCMessage;
import org.atc.SimpleConsumer;
import org.atc.config.SubscriberConfig;
import org.atc.amqp.MessageUtils;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class AMQPDurableTopicSubscriber implements SimpleConsumer {

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
    public ATCMessage receive() throws ATCException {
        try {
            Message m = topicSubscriber.receive();
            return MessageUtils.fromJMSToATC(m);
        } catch (JMSException e) {
            throw new ATCException("Error occurred while processing received message. Subscriber id: " +
                    config.getId(), e);
        }
    }

    @Override
    public void close() throws ATCException {
        try {
            topicSubscriber.close();
            topicSession.close();
            topicConnection.close();
        } catch (JMSException e) {
            throw new ATCException("Exception occurred while closing subscriber " + config.getId(), e);
        }
    }

    @Override
    public void unsubscribe() throws ATCException {
        try {
            topicSession.unsubscribe(subscriptionId);
        } catch (JMSException e) {
            throw new ATCException("Exception occurred while un-subscribing subscriber " + config.getId(), e);
        }
    }

    @Override
    public MessageConsumer subscribe(SubscriberConfig conf) throws NamingException, ATCException {

        try {
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
        } catch (JMSException e) {
            throw new ATCException("Subscriber initialisation failed. Subscriber id " + config.getId(), e);
        }
    }
}