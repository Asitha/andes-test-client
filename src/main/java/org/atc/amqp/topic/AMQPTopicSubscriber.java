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
import org.atc.amqp.MessageUtils;
import org.atc.config.SubscriberConfig;

import java.util.Properties;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class AMQPTopicSubscriber implements SimpleConsumer {

    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private TopicSubscriber topicSubscriber;
    private SubscriberConfig config;

    public final MessageConsumer subscribe(SubscriberConfig config) throws NamingException, ATCException {

        this.config = config;
        try {
            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, config.getInitialContextFactory());
            properties.put(config.getConnectionFactoryPrefix() + "." + config.getConnectionFactoryName(), config.getTCPConnectionURL());
            InitialContext ctx = new InitialContext(properties);

            // Lookup connection factory
            TopicConnectionFactory connFactory = (TopicConnectionFactory) ctx.lookup(config.getConnectionFactoryName());
            topicConnection = connFactory.createTopicConnection();
            topicConnection.start();
            if (config.isEnableClientAcknowledgment()) {
                topicSession = topicConnection.createTopicSession(false, TopicSession.CLIENT_ACKNOWLEDGE);
            } else if (config.isTransactional()) {
                topicSession = topicConnection.createTopicSession(true, TopicSession.AUTO_ACKNOWLEDGE);
            } else {
                topicSession = topicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
            }
            // Send message
            Topic topic = topicSession.createTopic(config.getQueueName());
            this.topicSubscriber = topicSession.createSubscriber(topic);

            return topicSubscriber;
        } catch (JMSException jmse) {
            throw new ATCException("Subscriber initialisation failed. Subscriber id " + config.getId(), jmse);
        }
    }

    public final SubscriberConfig getConfigs() {
        return config;
    }

    public final ATCMessage receive() throws ATCException {
        try {
            Message message = topicSubscriber.receive();
            if (config.isEnableClientAcknowledgment()) {
                message.acknowledge();
            }
            return MessageUtils.fromJMSToATC(message);
        } catch (JMSException e) {
            throw new ATCException("Error occurred while processing received message. Subscriber id: " +
                    config.getId(), e);
        }
    }

    public final void close() throws ATCException {
        try {
            topicSubscriber.close();
            topicSession.close();
            topicConnection.stop();
        } catch (JMSException e) {
            throw new ATCException("Exception occurred while closing subscriber " + config.getId(), e);
        }
    }

    public final void unsubscribe() {
    }

}
