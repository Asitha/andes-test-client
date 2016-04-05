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

    @XmlAttribute
    private int publisherInitialDelaySeconds;

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

    public final int getPort() {
        return port;
    }

    public final void setPort(int port) {
        this.port = port;
    }

    public final String getHostname() {
        return hostname;
    }

    public final void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public final String getUsername() {
        return username;
    }

    public final void setUsername(String username) {
        this.username = username;
    }

    public final String getPassword() {
        return password;
    }

    public final void setPassword(String password) {
        this.password = password;
    }

    public final String getInitialContextFactory() {
        return initialContextFactory;
    }

    public final void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    public final String getConnectionFactoryPrefix() {
        return connectionFactoryPrefix;
    }

    public final void setConnectionFactoryPrefix(String connectionFactoryPrefix) {
        this.connectionFactoryPrefix = connectionFactoryPrefix;
    }

    public final String getConnectionFactoryName() {
        return connectionFactoryName;
    }

    public final void setConnectionFactoryName(String connectionFactoryName) {
        this.connectionFactoryName = connectionFactoryName;
    }

    public final String getClientID() {
        return clientID;
    }

    public final void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public final String getVirtualHostName() {
        return virtualHostName;
    }

    public final void setVirtualHostName(String virtualHostName) {
        this.virtualHostName = virtualHostName;
    }

    public final int getPrintPerMessages() {
        return printPerMessages;
    }

    public final void setPrintPerMessages(int printPerMessages) {
        this.printPerMessages = printPerMessages;
    }

    public final boolean isEnableConsoleReport() {
        return enableConsoleReport;
    }

    public final void setEnableConsoleReport(boolean enableConsoleReport) {
        this.enableConsoleReport = enableConsoleReport;
    }

    public final boolean isJmxReportEnable() {
        return jmxReportEnable;
    }

    public final void setJmxReportEnable(boolean jmxReportEnable) {
        this.jmxReportEnable = jmxReportEnable;
    }

    public final boolean isCsvReportEnable() {
        return csvReportEnable;
    }

    public final void setCsvReportEnable(boolean csvReportEnable) {
        this.csvReportEnable = csvReportEnable;
    }

    public final int getConsoleReportUpdateInterval() {
        return consoleReportUpdateInterval;
    }

    public final void setConsoleReportUpdateInterval(int consoleReportUpdateInterval) {
        this.consoleReportUpdateInterval = consoleReportUpdateInterval;
    }

    public final int getCsvUpdateInterval() {
        return csvUpdateInterval;
    }

    public final void setCsvUpdateInterval(int csvUpdateInterval) {
        this.csvUpdateInterval = csvUpdateInterval;
    }

    public final int getCsvGaugeUpdateInterval() {
        return csvGaugeUpdateInterval;
    }

    public final void setCsvGaugeUpdateInterval(int csvGaugeUpdateInterval) {
        this.csvGaugeUpdateInterval = csvGaugeUpdateInterval;
    }

    public final List<PublisherConfig> getTopicPublishers() {
        return topicPublishers;
    }

    public final void setTopicPublishers(List<PublisherConfig> topicPublishers) {
        this.topicPublishers = topicPublishers;
    }

    public final List<PublisherConfig> getQueuePublishers() {
        return queuePublishers;
    }

    public final void setQueuePublishers(List<PublisherConfig> queuePublishers) {
        this.queuePublishers = queuePublishers;
    }

    public final List<SubscriberConfig> getQueueSubscribers() {
        return queueSubscribers;
    }

    public final void setQueueSubscribers(List<SubscriberConfig> queueSubscribers) {
        this.queueSubscribers = queueSubscribers;
    }

    public final List<SubscriberConfig> getTopicSubscribers() {
        return topicSubscribers;
    }

    public final void setTopicSubscribers(List<SubscriberConfig> topicSubscribers) {
        this.topicSubscribers = topicSubscribers;
    }

    public final List<SubscriberConfig> getDurableTopicSubscribers() {
        return durableTopicSubscribers;
    }

    public final void setDurableTopicSubscribers(List<SubscriberConfig> durableTopicSubscribers) {
        this.durableTopicSubscribers = durableTopicSubscribers;
    }

    public int getPublisherInitialDelaySeconds() {
        return publisherInitialDelaySeconds;
    }

    public void setPublisherInitialDelaySeconds(int publisherInitialDelaySeconds) {
        this.publisherInitialDelaySeconds = publisherInitialDelaySeconds;
    }
}
