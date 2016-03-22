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
import org.atc.SimplePublisher;
import org.atc.config.PublisherConfig;
import org.atc.amqp.MessageUtils;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class AMQPQueueSender implements SimplePublisher {

    private QueueConnection queueConnection;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private PublisherConfig config;

    public final void init(PublisherConfig conf) throws NamingException, ATCException {
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
            if (conf.isTransactional()) {
                queueSession = queueConnection.createQueueSession(true, 0);
            } else {
                queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
            }
//        Queue queue = (Queue)ctx.lookup(queueName);
            Queue queue = queueSession.createQueue(queueName);
            queueSender = queueSession.createSender(queue);
            config = conf;
        } catch (JMSException e) {
            throw new ATCException("Publisher initialisation failed. Publisher id " + conf.getId(), e);
        }
    }

    public final ATCMessage createTextMessage(String text) throws ATCException {
        return new ATCMessage(text);
    }

    public final void send(ATCMessage atcMessage) throws ATCException {
        try {
            Message m = MessageUtils.fromATCToJMS(queueSession, atcMessage);
            queueSender.send(m);
        } catch (JMSException e) {
            throw new ATCException("Error occurred while sending message. Publisher id" + config.getId(), e);
        }
    }

    public final void commit() throws ATCException {
        try {
            queueSession.commit();
        } catch (JMSException e) {
            throw new ATCException("Error occurred while committing. Publisher id " + config.getId(), e);
        }
    }

    public final void rollback() throws ATCException {
        try {
            queueSession.rollback();
        } catch (JMSException e) {
            throw new ATCException("Error occurred while rolling back. Publisher id " + config.getId(), e);
        }
    }

    public final PublisherConfig getConfigs() {
        return config;
    }

    public final void close() throws ATCException {
        try {
            queueSender.close();
            queueSession.close();
            queueConnection.close();
        } catch (JMSException e) {
            throw new ATCException("Exception occurred while closing publisher " + config.getId(), e);

        }
    }
}
