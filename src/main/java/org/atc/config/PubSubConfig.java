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

public abstract class PubSubConfig extends GlobalConfig{

    private long messageCount;
    private int parallelPublishers;
    private String queueName;
    private String id;
    private boolean isTransactional;
    private int transactionBatchSize;
    private String failoverParams;

    public PubSubConfig(GlobalConfig globalConfig) {
        super(globalConfig.getUsername(), globalConfig.getPassword(),
                globalConfig.getHostname(), globalConfig.getPort(),
                globalConfig.getInitialContextFactory(), globalConfig.getConnectionFactoryPrefix(),
                globalConfig.getConnectionFactoryName(), globalConfig.getClientID(),
                globalConfig.getVirtualHostName(), globalConfig.getPrintPerMessages(),
                globalConfig.isEnableConsoleReport(), globalConfig.getConsoleReportUpdateInterval(),
                globalConfig.isJmxReportEnable(), globalConfig.isCsvReportEnable(),
                globalConfig.getCsvUpdateInterval(), globalConfig.getCsvGaugeUpdateInterval());
    }

    public PubSubConfig(PubSubConfig config) {
        super(config.getUsername(), config.getPassword(),
                config.getHostname(), config.getPort(),
                config.getInitialContextFactory(), config.getConnectionFactoryPrefix(),
                config.getConnectionFactoryName(), config.getClientID(),
                config.getVirtualHostName(), config.getPrintPerMessages(),
                config.isEnableConsoleReport(), config.getConsoleReportUpdateInterval(),
                config.isJmxReportEnable(), config.isCsvReportEnable(),
                config.getCsvUpdateInterval(), config.getCsvGaugeUpdateInterval());

        messageCount = config.getMessageCount();
        parallelPublishers = config.getParallelPublishers();
        queueName = config.getQueueName();
        id = config.getId();
        isTransactional = config.isTransactional();
        transactionBatchSize = config.getTransactionBatchSize();
        failoverParams = config.getFailoverParams();
    }

    void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    void setParallelPublishers(int parallelPublishers) {
        this.parallelPublishers = parallelPublishers;
    }

    void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public int getParallelPublishers() {
        return parallelPublishers;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isTransactional() {
        return isTransactional;
    }


    public String getFailoverParams() {
        return failoverParams;
    }

    public void setFailoverParams(String failoverParams) {
        this.failoverParams = failoverParams;
    }

    public void setTransactional(boolean isTransactional) {
        this.isTransactional = isTransactional;
    }

    public int getTransactionBatchSize() {
        return transactionBatchSize;
    }

    public void setTransactionBatchSize(int transactionBatchSize) {
        this.transactionBatchSize = transactionBatchSize;
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
}
