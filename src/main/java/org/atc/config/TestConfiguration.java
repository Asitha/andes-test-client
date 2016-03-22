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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestConfiguration {

    @XmlAttribute(required = true)
    private int port;

    @XmlAttribute(required = true)
    private String hostname;

    @XmlAttribute(required = true)
    private String username;

    @XmlAttribute(required = true)
    private String password;

    @XmlAttribute(required = true)
    private String initialContextFactory;

    @XmlAttribute(required = true)
    private String connectionFactoryPrefix;

    @XmlAttribute(required = true)
    private String connectionFactoryName;

    @XmlAttribute(required = true)
    private String clientID;

    @XmlAttribute(required = true)
    private String virtualHostName;

    @XmlAttribute
    private int printPerMessages;

    @XmlAttribute
    private boolean enableConsoleReport;

    @XmlAttribute
    private boolean jmxReportEnable;

    @XmlAttribute
    private boolean csvReportEnable;

    @XmlAttribute
    private int consoleReportUpdateInterval;

    @XmlAttribute
    private int csvUpdateInterval;

    @XmlAttribute
    private int csvGaugeUpdateInterval;

    @XmlElement(name = "topicPublishers")
    private List<PublisherConfig> topicPublishers;

    @XmlElement(name = "queuePublishers")
    private List<PublisherConfig> queuePublishers;

    @XmlElement(name = "queueSubscribers")
    private List<SubscriberConfig> queueSubscribers;

    @XmlElement(name = "topicSubscribers")
    private List<SubscriberConfig> topicSubscribers;

    @XmlElement(name = "durableTopicSubscribers")
    private List<SubscriberConfig> durableTopicSubscribers;

    public TestConfiguration() {
        topicPublishers = new ArrayList<>();
        queuePublishers = new ArrayList<>();
        topicSubscribers = new ArrayList<>();
        durableTopicSubscribers = new ArrayList<>();
        queueSubscribers = new ArrayList<>();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    public String getConnectionFactoryPrefix() {
        return connectionFactoryPrefix;
    }

    public void setConnectionFactoryPrefix(String connectionFactoryPrefix) {
        this.connectionFactoryPrefix = connectionFactoryPrefix;
    }

    public String getConnectionFactoryName() {
        return connectionFactoryName;
    }

    public void setConnectionFactoryName(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getVirtualHostName() {
        return virtualHostName;
    }

    public void setVirtualHostName(String virtualHostName) {
        this.virtualHostName = virtualHostName;
    }

    public int getPrintPerMessages() {
        return printPerMessages;
    }

    public void setPrintPerMessages(int printPerMessages) {
        this.printPerMessages = printPerMessages;
    }

    public boolean isEnableConsoleReport() {
        return enableConsoleReport;
    }

    public void setEnableConsoleReport(boolean enableConsoleReport) {
        this.enableConsoleReport = enableConsoleReport;
    }

    public boolean isJmxReportEnable() {
        return jmxReportEnable;
    }

    public void setJmxReportEnable(boolean jmxReportEnable) {
        this.jmxReportEnable = jmxReportEnable;
    }

    public boolean isCsvReportEnable() {
        return csvReportEnable;
    }

    public void setCsvReportEnable(boolean csvReportEnable) {
        this.csvReportEnable = csvReportEnable;
    }

    public int getConsoleReportUpdateInterval() {
        return consoleReportUpdateInterval;
    }

    public void setConsoleReportUpdateInterval(int consoleReportUpdateInterval) {
        this.consoleReportUpdateInterval = consoleReportUpdateInterval;
    }

    public int getCsvUpdateInterval() {
        return csvUpdateInterval;
    }

    public void setCsvUpdateInterval(int csvUpdateInterval) {
        this.csvUpdateInterval = csvUpdateInterval;
    }

    public int getCsvGaugeUpdateInterval() {
        return csvGaugeUpdateInterval;
    }

    public void setCsvGaugeUpdateInterval(int csvGaugeUpdateInterval) {
        this.csvGaugeUpdateInterval = csvGaugeUpdateInterval;
    }

    public List<PublisherConfig> getTopicPublishers() {
        return topicPublishers;
    }

    public void setTopicPublishers(List<PublisherConfig> topicPublishers) {
        this.topicPublishers = topicPublishers;
    }

    public List<PublisherConfig> getQueuePublishers() {
        return queuePublishers;
    }

    public void setQueuePublishers(List<PublisherConfig> queuePublishers) {
        this.queuePublishers = queuePublishers;
    }

    public List<SubscriberConfig> getQueueSubscribers() {
        return queueSubscribers;
    }

    public void setQueueSubscribers(List<SubscriberConfig> queueSubscribers) {
        this.queueSubscribers = queueSubscribers;
    }

    public List<SubscriberConfig> getTopicSubscribers() {
        return topicSubscribers;
    }

    public void setTopicSubscribers(List<SubscriberConfig> topicSubscribers) {
        this.topicSubscribers = topicSubscribers;
    }

    public List<SubscriberConfig> getDurableTopicSubscribers() {
        return durableTopicSubscribers;
    }

    public void setDurableTopicSubscribers(List<SubscriberConfig> durableTopicSubscribers) {
        this.durableTopicSubscribers = durableTopicSubscribers;
    }
}
