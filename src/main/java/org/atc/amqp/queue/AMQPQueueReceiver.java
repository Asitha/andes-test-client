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

package org.atc.amqp.queue;

import org.atc.ATCException;
import org.atc.ATCMessage;
import org.atc.SimpleConsumer;
import org.atc.config.SubscriberConfig;
import org.atc.amqp.MessageUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.MessageConsumer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class AMQPQueueReceiver implements SimpleConsumer {


    private QueueConnection queueConnection;
    private QueueSession queueSession;
    private MessageConsumer consumer;
    private SubscriberConfig config;

    public ATCMessage receive() throws ATCException {
        try {
            Message message = consumer.receive();
            message.acknowledge();
            return MessageUtils.fromJMSToATC(message);
        } catch (JMSException e) {
            throw new ATCException("Error occurred while processing received message. Subscriber id: " +
                    config.getId(), e);
        }

    }

    public void close() throws ATCException {
        try {
            consumer.close();
            queueSession.close();
            queueConnection.stop();
            queueConnection.close();
        } catch (JMSException e) {
            throw new ATCException("Exception occurred while closing publisher " + config.getId(), e);
        }
    }

    public void unsubscribe() {

    }

    public MessageConsumer subscribe(SubscriberConfig conf) throws NamingException, ATCException {
        try {
            String queueName = conf.getQueueName();
            Properties properties = new Properties();
            properties.put(Context.INITIAL_CONTEXT_FACTORY, conf.getInitialContextFactory());
            properties.put(conf.getConnectionFactoryPrefix() + "." + conf.getConnectionFactoryName(), conf.getTCPConnectionURL());
            properties.put("queue." + queueName, queueName);
            InitialContext ctx = new InitialContext(properties);
            // Lookup connection factory
            QueueConnectionFactory connFactory = (QueueConnectionFactory) ctx.lookup(conf.getConnectionFactoryName());
            queueConnection = connFactory.createQueueConnection();
            queueConnection.start();
            queueSession =
                    queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            //Receive message
            Queue queue = (Queue) ctx.lookup(queueName);
            consumer = queueSession.createConsumer(queue);
            config = conf;
            return consumer;
        } catch (JMSException e) {
            throw new ATCException("Subscriber initialisation failed. Subscriber id " + config.getId(), e);
        }
    }

    public SubscriberConfig getConfigs() {
        return config;
    }
}
