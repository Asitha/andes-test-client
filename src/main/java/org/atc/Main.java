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

package org.atc;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import org.atc.config.ConfigReader;
import org.atc.config.GlobalConfig;
import org.atc.config.PublisherConfig;
import org.atc.config.SubscriberConfig;
import org.atc.config.TestConfiguration;
import org.atc.jms.ConsumerThread;
import org.atc.jms.PublisherThread;
import org.atc.jms.SimpleJMSConsumer;
import org.atc.jms.durable.TestDurableTopicSubscriber;
import org.atc.jms.queue.TestQueueReceiver;
import org.atc.jms.queue.TestQueueSender;
import org.atc.jms.topic.TestTopicPublisher;
import org.atc.jms.topic.TestTopicSubscriber;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

public class Main {

    private static Log log = LogFactory.getLog(Main.class);

    public static final MetricRegistry metrics = new MetricRegistry();
    public static final MetricRegistry gauges = new MetricRegistry();

    public static String CONFIG_FILE_PATH;
    private static ConsoleReporter reporter;
    private static JmxReporter jmxReporter;
    private static CsvReporter csvReporter;
    private static CsvReporter csvGaugeReporter;
    private static Slf4jReporter slf4jReporter;

    public static void main(String[] args) throws NamingException, JMSException, FileNotFoundException,
            InterruptedException, ParseException, CloneNotSupportedException {

        Options options = createOptions();

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args, false);

        Histogram latencyHist = Main.metrics.histogram(
                name("global", "consumer", "latency")
        );
        Meter consumerRate = Main.metrics.meter(
                name("global", "consumer", "rate"));


        if (cmd.hasOption("c")) {
            CONFIG_FILE_PATH = cmd.getOptionValue("c");
        } else {
            CONFIG_FILE_PATH = System.getProperty("user.dir") + "/conf/client.yaml";
        }

        TestConfiguration config = ConfigReader.parseConfig(CONFIG_FILE_PATH);
//        System.setProperty("qpid.flow_control_wait_failure", "1500000");
        // Subscribers

        startStatReporting(config.getGlobalConfig());

        int subscriberCount = config.getTopicSubscriberConfigList().size() +
                config.getQueueSubscriberConfigList().size() + config.getDurableSubscriberConfigList().size();
        final List<Thread> threadList = new ArrayList<Thread>(subscriberCount);

        TestTopicSubscriber topicSubscriber;
        for (SubscriberConfig subscriberConfig : config.getTopicSubscriberConfigList()) {
            topicSubscriber = new TestTopicSubscriber();
            topicSubscriber.subscribe(subscriberConfig);
            Thread subThread = new Thread(new ConsumerThread(topicSubscriber, latencyHist, consumerRate));
            subThread.start();
            threadList.add(subThread);
        }

        SimpleJMSConsumer queueReceiver;
        for (SubscriberConfig subscriberConfig : config.getQueueSubscriberConfigList()) {
            queueReceiver = new TestQueueReceiver();
            queueReceiver.subscribe(subscriberConfig);
            Thread subThread = new Thread(new ConsumerThread(queueReceiver, latencyHist, consumerRate));
            subThread.start();
            threadList.add(subThread);
        }

        TestDurableTopicSubscriber durableTopicSubscriber;
        for (SubscriberConfig subscriberConfig : config.getDurableSubscriberConfigList()) {
            durableTopicSubscriber = new TestDurableTopicSubscriber();
            durableTopicSubscriber.subscribe(subscriberConfig);
            Thread subThread = new Thread(new ConsumerThread(durableTopicSubscriber, latencyHist, consumerRate));
            subThread.start();
            threadList.add(subThread);
        }

        // Publishers

        TestTopicPublisher topicPublisher;
        for (PublisherConfig publisherConfig : config.getTopicPublisherList()) {
            topicPublisher = new TestTopicPublisher();
            topicPublisher.init(publisherConfig);
            Thread pubThread = new Thread(new PublisherThread(topicPublisher));
            pubThread.start();
            threadList.add(pubThread);
        }

        TestQueueSender queuePublisher;
        for (PublisherConfig publisherConfig : config.getQueuePublisherConfigList()) {
            queuePublisher = new TestQueueSender();
            queuePublisher.init(publisherConfig);
            Thread pubThread = new Thread(new PublisherThread(queuePublisher));
            pubThread.start();
            threadList.add(pubThread);
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                log.info("Shutting down test client.");
                slf4jReporter.report();
                csvGaugeReporter.report();
                reporter.report();
                if(null != jmxReporter) {
                    jmxReporter.close();
                }

                if(null != csvReporter) {
                    csvReporter.report();
                    csvReporter.close();
                }

                for (Thread t: threadList) {
                    t.interrupt();
                }
            }
        });


        // barrier. wait till all the done
        for (Thread thread : threadList) {
            thread.join();
        }

        log.info("Test Complete!");
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption("c", "conf", true, "Path to configuration file. Default is /conf/client.yaml");
        options.addOption("f", "fresh-logs", false, "Removes all old files log/ and run with fresh log files");
        return options;
    }

    static void startStatReporting(GlobalConfig config) {
        // console reporter is created by default to provide a report when shutting down
        reporter = ConsoleReporter.forRegistry(metrics)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        csvGaugeReporter = CsvReporter.forRegistry(gauges)
                .formatFor(Locale.US)
                .convertRatesTo(TimeUnit.MILLISECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(new File(System.getProperty("user.dir") + "/logs/metrics"));
        csvGaugeReporter.start(config.getCsvGaugeUpdateInterval(), TimeUnit.MILLISECONDS);

        slf4jReporter = Slf4jReporter.forRegistry(metrics)
                .outputTo(LoggerFactory.getLogger("com.example.metrics"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();

        if(config.isEnableConsoleReport()) {
            log.info("Console reporting enabled. Refresh rate: every " + config.getConsoleReportUpdateInterval() + " seconds");
            reporter.start(config.getConsoleReportUpdateInterval(), TimeUnit.SECONDS);
            slf4jReporter.start(config.getConsoleReportUpdateInterval(), TimeUnit.SECONDS);
        }

        if(config.isJmxReportEnable()) {
            log.info("JMX reporting enabled.");
            jmxReporter = JmxReporter.forRegistry(metrics).build();
            jmxReporter.start();
        }

        if(config.isCsvReportEnable()) {
            log.info("CSV reporting enabled. Refresh rate: every " + config.getCsvUpdateInterval() + " seconds");
            startCSVReport(config.getCsvUpdateInterval());
        }
    }

    static void startCSVReport(int csvReportRefreshRate) {
        csvReporter = CsvReporter.forRegistry(metrics)
                .formatFor(Locale.US)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(new File(System.getProperty("user.dir") + "/logs/metrics"));
        csvReporter.start(csvReportRefreshRate, TimeUnit.SECONDS);
    }
}
