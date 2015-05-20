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

import org.atc.jms.SimpleJMSPublisher;
import org.atc.config.PublisherConfig;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.QueueSession;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

public class TestTopicPublisher implements SimpleJMSPublisher {

    private TopicPublisher topicPublisher;
    private TopicSession topicSession;
    private TopicConnection topicConnection;
    private PublisherConfig conf;

    @Override
    public void send(Message message) throws JMSException {
        topicPublisher.send(message);
    }

    @Override
    public void commit() throws JMSException{
        topicSession.commit();
    }

    @Override
    public void rollback() throws JMSException {
        topicSession.rollback();
    }

    @Override
    public void init(PublisherConfig conf) throws NamingException, JMSException {

        this.conf = conf;
        String topicName = conf.getQueueName();
        String tcpConnectionURL = conf.getTCPConnectionURL();
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, conf.getInitialContextFactory());
        properties.put(conf.getConnectionFactoryPrefix() + "." + conf.getConnectionFactoryName(),
                tcpConnectionURL);
        properties.put("topic." + topicName, topicName);
        System.out.println("getTCPConnectionURL(userName,password) = " + tcpConnectionURL);
        InitialContext ctx = new InitialContext(properties);
        // Lookup connection factory
        TopicConnectionFactory connFactory = (TopicConnectionFactory) ctx.lookup(conf.getConnectionFactoryName());
        topicConnection = connFactory.createTopicConnection();
        topicConnection.start();
        if(conf.isTransactional()) {
            topicSession = topicConnection.createTopicSession(true, 0);
        } else {
            topicSession = topicConnection.createTopicSession(false, QueueSession.AUTO_ACKNOWLEDGE);
        }
        Topic topic = (Topic)ctx.lookup(conf.getQueueName());
//        Topic topic = topicSession.createTopic(conf.getQueueName());
        // create the message to send
        topicPublisher = topicSession.createPublisher(topic);
    }

    @Override
    public Message createTextMessage(String text) throws JMSException {
        return topicSession.createTextMessage(text);
    }

    @Override
    public PublisherConfig getConfigs() {
        return conf;
    }

    @Override
    public void close() throws JMSException {
        topicPublisher.close();
        topicSession.close();
        topicConnection.stop();
        topicConnection.close();
    }
}
