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

package org.atc.jms.queue;

import org.atc.jms.SimpleJMSPublisher;
import org.atc.config.PublisherConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

public class TestQueueSender implements SimpleJMSPublisher {

    private static Log log = LogFactory.getLog(TestQueueReceiver.class);
    private QueueConnection queueConnection;
    private QueueSession queueSession;
    private QueueSender queueSender;
    private PublisherConfig config;

    public void init(PublisherConfig conf) throws NamingException, JMSException {
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
        if(conf.isTransactional()){
            queueSession = queueConnection.createQueueSession(true, 0);
        } else {
            queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        }
//        Queue queue = (Queue)ctx.lookup(queueName);
        Queue queue = queueSession.createQueue(queueName);
        queueSender = queueSession.createSender(queue);
        config = conf;

    }

    @Override
    public Message createTextMessage(String text) throws JMSException {
        return queueSession.createTextMessage(text);
    }

    @Override
    public void send(Message message) throws JMSException {
        queueSender.send(message);
    }

    @Override
    public void commit() throws JMSException {
        queueSession.commit();
    }

    @Override
    public void rollback() throws JMSException {
        queueSession.rollback();
    }

    @Override
    public PublisherConfig getConfigs() {
        return config;
    }

    @Override
    public void close() throws JMSException {
        queueSender.close();
        queueSession.close();
        queueConnection.close();
    }
}
