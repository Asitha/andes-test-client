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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.lang.reflect.Field;
import java.util.UUID;

@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class PubSubConfig {

    @XmlAttribute
    private int port;
    @XmlAttribute
    private String hostname;
    @XmlAttribute
    private String username;
    @XmlAttribute
    private String password;
    @XmlAttribute
    private String initialContextFactory;
    @XmlAttribute
    private String connectionFactoryPrefix;
    @XmlAttribute
    private String connectionFactoryName;
    @XmlAttribute
    private String clientID;
    @XmlAttribute
    private String virtualHostName;
    @XmlAttribute(required = true)
    private long messageCount;
    @XmlAttribute
    private int parallelThreads;
    @XmlAttribute(required = true)
    private String queueName;
    @XmlAttribute
    private String id;
    @XmlAttribute
    private boolean isTransactional;
    @XmlAttribute
    private int transactionBatchSize;
    @XmlAttribute
    private String failoverParams;
    @XmlAttribute
    private int delayBetweenMsgs;

    PubSubConfig() {
        id = UUID.randomUUID().toString();
    }

    void addGlobalConfigurationsIfAbsent(TestConfiguration tc) {
        if(port == 0){
            setPort(tc.getPort());
        }
        if(StringUtils.isBlank(getHostname())) {
            setHostname(tc.getHostname());
        }
        if(StringUtils.isBlank(getUsername())) {
            setUsername(tc.getUsername());
        }
        if (StringUtils.isBlank(getPassword())) {
            setPassword(tc.getPassword());
        }
        if (StringUtils.isBlank(getInitialContextFactory())) {
            setInitialContextFactory(tc.getInitialContextFactory());
        }
        if (StringUtils.isBlank(getConnectionFactoryPrefix())) {
            setConnectionFactoryPrefix(tc.getConnectionFactoryPrefix());
        }
        if (StringUtils.isBlank(getConnectionFactoryName())) {
            setConnectionFactoryName(tc.getConnectionFactoryName());
        }
        if (StringUtils.isBlank(getClientID())) {
            setClientID(tc.getClientID());
        }
        if(StringUtils.isBlank(getVirtualHostName())) {
            setVirtualHostName(tc.getVirtualHostName());
        }
    }

    public String getTCPConnectionURL() {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        StringBuilder builder = new StringBuilder();
        builder.append("amqp://").append(getUsername()).append(":").append(getPassword()).append("@").
                append(getClientID()).append("/").append(getVirtualHostName()).append("?");

        if(StringUtils.isEmpty(getFailoverParams())) {
            builder.append("brokerlist='tcp://").append(getHostname()).append(":").append(getPort()).append("'");
        } else {
            builder.append(getFailoverParams());
        }

        return builder.toString();
    }

    public int getPort() {
        return port;
    }

    void setPort(int port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUsername() {
        return username;
    }

    void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    void setPassword(String password) {
        this.password = password;
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    public String getConnectionFactoryPrefix() {
        return connectionFactoryPrefix;
    }

    void setConnectionFactoryPrefix(String connectionFactoryPrefix) {
        this.connectionFactoryPrefix = connectionFactoryPrefix;
    }

    public String getConnectionFactoryName() {
        return connectionFactoryName;
    }

    void setConnectionFactoryName(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
    }

    public String getClientID() {
        return clientID;
    }

    void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getVirtualHostName() {
        return virtualHostName;
    }

    void setVirtualHostName(String virtualHostName) {
        this.virtualHostName = virtualHostName;
    }

    public long getMessageCount() {
        return messageCount;
    }

    void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public String getQueueName() {
        return queueName;
    }

    void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public boolean isTransactional() {
        return isTransactional;
    }

    void setTransactional(boolean transactional) {
        isTransactional = transactional;
    }

    public int getTransactionBatchSize() {
        return transactionBatchSize;
    }

    void setTransactionBatchSize(int transactionBatchSize) {
        this.transactionBatchSize = transactionBatchSize;
    }

    public String getFailoverParams() {
        return failoverParams;
    }

    void setFailoverParams(String failoverParams) {
        this.failoverParams = failoverParams;
    }

    public int getDelayBetweenMsgs() {
        return delayBetweenMsgs;
    }

    void setDelayBetweenMsgs(int delayBetweenMsgs) {
        this.delayBetweenMsgs = delayBetweenMsgs;
    }

    public int getParallelThreads() {
        return parallelThreads;
    }

    public void setParallelThreads(int parallelThreads) {
        this.parallelThreads = parallelThreads;
    }

    Object copyMembers(Object original, Object copy) throws NoSuchFieldException, IllegalAccessException {
        for(Field originalsField: original.getClass().getDeclaredFields()) {
            Field copyField = copy.getClass().getDeclaredField(originalsField.getName());
            copyField(originalsField, copyField, original, copy);
        }
        for (Field originalsField: original.getClass().getSuperclass().getDeclaredFields()) {
            Field copyField = copy.getClass().getSuperclass().getDeclaredField(originalsField.getName());
            copyField(originalsField, copyField, original, copy);
        }
        return copy;
    }

    private void copyField(Field originalField, Field copyField, Object original, Object copy) throws IllegalAccessException {
        originalField.setAccessible(true);
        copyField.setAccessible(true);
        copyField.set(copy, originalField.get(original));
    }
}

