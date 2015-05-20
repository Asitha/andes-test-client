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

import static org.atc.Main.*;
import static org.atc.config.ConfigReader.CF_NAME;
import static org.atc.config.ConfigReader.CF_NAME_PREFIX;
import static org.atc.config.ConfigReader.CLIENT_ID;
import static org.atc.config.ConfigReader.CONSOLE_REPORT;
import static org.atc.config.ConfigReader.CONSOLE_REPORT_UPDATE_INTERVAL;
import static org.atc.config.ConfigReader.CSV_GAUGE_UPDATE_INTERVAL;
import static org.atc.config.ConfigReader.CSV_REPORT;
import static org.atc.config.ConfigReader.CSV_UPDATE_INTERVAL;
import static org.atc.config.ConfigReader.HOSTNAME;
import static org.atc.config.ConfigReader.ICF;
import static org.atc.config.ConfigReader.JMX_REPORT;
import static org.atc.config.ConfigReader.PASSWORD;
import static org.atc.config.ConfigReader.PORT;
import static org.atc.config.ConfigReader.USERNAME;
import static org.atc.config.ConfigReader.VIRTUALHOST_NAME;

public class GlobalConfig {
    
    private int port;
    private String hostname;
    private String username;
    private String password;
    private String initialContextFactory;
    private String connectionFactoryPrefix;
    private String connectionFactoryName;
    private String clientID;
    private String virtualHostName;
    private int printPerMessages;
    private boolean enableConsoleReport;
    private boolean jmxReportEnable;
    private boolean csvReportEnable;
    private int consoleReportUpdateInterval;
    private int csvUpdateInterval;
    private int csvGaugeUpdateInterval;


    public GlobalConfig(final Object username, final Object password,
                        final Object hostname, final Object port,
                        final Object icf, final Object cfNamePrefix,
                        final Object cfName, final Object clientID,
                        final Object virtualHostName,
                        final Object printsMessages,
                        final Object consoleReportEnable,
                        final Object consoleReportUpdateInterval,
                        final Object jmxReportEnable,
                        final Object csvReportEnable,
                        final Object csvUpdateInterval,
                        final Object csvGaugeUpdateInterval) {

        setUsername(username);
        setPassword(password);
        setHostname(hostname);
        setPort(port);
        setInitialContextFactory(icf);
        setConnectionFactoryPrefix(cfNamePrefix);
        setConnectionFactoryName(cfName);
        setClientID(clientID);
        setVirtualHostName(virtualHostName);
        setPrintPerMessages(printsMessages);
        setEnableConsoleReport(consoleReportEnable);
        setConsoleReportUpdateInterval(consoleReportUpdateInterval);
        setJmxReportEnable(jmxReportEnable);
        setCsvReportEnable(csvReportEnable);
        setCsvUpdateInterval(csvUpdateInterval);
        setCsvGaugeUpdateInterval(csvGaugeUpdateInterval);
    }

    public int getPort() {
        return port;
    }

    public void setPort(Object port) {
        if(port instanceof Integer) {
            this.port = (Integer)port;
        } else {
            throw new IllegalArgumentException(PORT + " should be of type Integer in configuration " + 
                    CONFIG_FILE_PATH);
        }

    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(Object hostname) {
        String tmpVal = hostname.toString();
        if(StringUtils.isEmpty(tmpVal)){
            throw new IllegalArgumentException(HOSTNAME + " should be non empty in configuration file " + CONFIG_FILE_PATH);
        }
        this.hostname = tmpVal;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(Object username) {
        String tmpVal = username.toString();
        if(StringUtils.isEmpty(tmpVal)){
            throw new IllegalArgumentException(USERNAME + " should be non empty in configuration file " + CONFIG_FILE_PATH);
        }
        
        this.username = tmpVal;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(Object password) {
        String tmpVal = password.toString();
        if(StringUtils.isEmpty(tmpVal)){
            throw new IllegalArgumentException(PASSWORD + " should be non empty in configuration file " + CONFIG_FILE_PATH);
        }
        this.password = tmpVal;
    }

    public String getInitialContextFactory() {
        return initialContextFactory;
    }

    public void setInitialContextFactory(Object initialContextFactory) {
        String tmpVal = initialContextFactory.toString();
        if(StringUtils.isEmpty(tmpVal)){
            throw new IllegalArgumentException(ICF + " should be non empty in configuration file " + CONFIG_FILE_PATH);
        }
        this.initialContextFactory = tmpVal;
    }

    public String getConnectionFactoryPrefix() {
        return connectionFactoryPrefix;
    }

    public void setConnectionFactoryPrefix(Object connectionFactoryPrefix) {
        String tmpVal = connectionFactoryPrefix.toString();
        if(StringUtils.isEmpty(tmpVal)){
            throw new IllegalArgumentException(CF_NAME_PREFIX + " should be non empty in configuration file " + CONFIG_FILE_PATH);
        }
        this.connectionFactoryPrefix = tmpVal;
    }

    public String getConnectionFactoryName() {
        return connectionFactoryName;
    }

    public void setConnectionFactoryName(Object connectionFactoryName) {
        String tmpVal = connectionFactoryName.toString();
        if(StringUtils.isEmpty(tmpVal)){
            throw new IllegalArgumentException(CF_NAME + " should be non empty in configuration file " + CONFIG_FILE_PATH);
        }
        this.connectionFactoryName = tmpVal;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(Object clientID) {
        String tmpVal = clientID.toString();
        if(StringUtils.isEmpty(tmpVal)){
            throw new IllegalArgumentException(CLIENT_ID + " should be non empty in configuration file " + CONFIG_FILE_PATH);
        }
        this.clientID = tmpVal;
    }

    public String getVirtualHostName() {
        return virtualHostName;
    }

    public void setVirtualHostName(Object virtualHostName) {
        String tmpVal = virtualHostName.toString();
        if(StringUtils.isEmpty(tmpVal)){
            throw new IllegalArgumentException(VIRTUALHOST_NAME + " should be non empty in configuration file " + CONFIG_FILE_PATH);
        }
        this.virtualHostName = tmpVal;
    }

    public int getPrintPerMessages() {
        return printPerMessages;
    }

    public void setPrintPerMessages(Object printPerMessages) {
        int count = (Integer)printPerMessages;
        if(count <= 0) {
            this.printPerMessages = 1000;
        } else {
            this.printPerMessages = count;
        }
    }



    public boolean isEnableConsoleReport() {
        return enableConsoleReport;
    }

    public void setEnableConsoleReport(Object enableConsoleReport) {
        if(enableConsoleReport instanceof Boolean) {
            this.enableConsoleReport = (Boolean) enableConsoleReport;
        } else {
            throw new IllegalArgumentException(CONSOLE_REPORT + " should be of type Boolean in configuration " +
                    CONFIG_FILE_PATH);
        }
    }

    public int getConsoleReportUpdateInterval() {
        return consoleReportUpdateInterval;
    }

    public void setConsoleReportUpdateInterval(Object consoleReportUpdateInterval) {
        if(consoleReportUpdateInterval instanceof Integer) {
            this.consoleReportUpdateInterval = (Integer) consoleReportUpdateInterval;
        } else {
            throw new IllegalArgumentException(CONSOLE_REPORT_UPDATE_INTERVAL + " should be of type Integer in configuration " +
                    CONFIG_FILE_PATH);
        }
    }

    public boolean isJmxReportEnable() {
        return jmxReportEnable;
    }

    public void setJmxReportEnable(Object jmxReportEnable) {
        if(jmxReportEnable instanceof Boolean) {
            this.jmxReportEnable = (Boolean) jmxReportEnable;
        } else {
            throw new IllegalArgumentException(JMX_REPORT + " should be of type Boolean in configuration " +
                    CONFIG_FILE_PATH);
        }
    }

    public boolean isCsvReportEnable() {
        return csvReportEnable;
    }

    public void setCsvReportEnable(Object csvReportEnable) {
        if(csvReportEnable instanceof Boolean) {
            this.csvReportEnable = (Boolean) csvReportEnable;
        } else {
            throw new IllegalArgumentException(CSV_REPORT + " should be of type Boolean in configuration " +
                    CONFIG_FILE_PATH);
        }
    }

    public int getCsvUpdateInterval() {
        return csvUpdateInterval;
    }

    public void setCsvUpdateInterval(Object csvUpdateInterval) {
        if(csvUpdateInterval instanceof Integer) {
            this.csvUpdateInterval = (Integer) csvUpdateInterval;
        } else {
            throw new IllegalArgumentException(CSV_UPDATE_INTERVAL + " should be of type Integer in configuration " +
                    CONFIG_FILE_PATH);
        }
    }

    public int getCsvGaugeUpdateInterval() {
        return csvGaugeUpdateInterval;
    }

    public void setCsvGaugeUpdateInterval(Object csvGaugeUpdateInterval) {
        if(csvGaugeUpdateInterval instanceof Integer) {
            this.csvGaugeUpdateInterval = (Integer)csvGaugeUpdateInterval;
        } else {
            throw new IllegalArgumentException(CSV_GAUGE_UPDATE_INTERVAL + " should be of type Integer in configuration " +
                    CONFIG_FILE_PATH);
        }
    }
}
