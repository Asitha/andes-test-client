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
import org.atc.config.PublisherConfig;
import org.atc.config.SubscriberConfig;
import org.atc.config.TestConfiguration;
import org.atc.amqp.topic.AMQPDurableTopicSubscriber;
import org.atc.amqp.queue.AMQPQueueReceiver;
import org.atc.amqp.queue.AMQPQueueSender;
import org.atc.amqp.topic.AMQPTopicPublisher;
import org.atc.amqp.topic.AMQPTopicSubscriber;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.LoggerFactory;

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

    static final MetricRegistry metrics = new MetricRegistry();
    static final MetricRegistry gauges = new MetricRegistry();

    private static ConsoleReporter reporter;
    private static JmxReporter jmxReporter;
    private static CsvReporter csvReporter;
    private static CsvReporter csvGaugeReporter;
    private static Slf4jReporter slf4jReporter;

    private Main() {
    }

    public static void main(String[] args) throws NamingException, ATCException, FileNotFoundException,
            InterruptedException, ParseException, NoSuchFieldException, IllegalAccessException {

        Options options = createOptions();
        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args, false);

        Histogram latencyHist = Main.metrics.histogram(
                name("global", "consumer", "latency")
        );
        Meter consumerRate = Main.metrics.meter(
                name("global", "consumer", "rate"));


        String configFilePath;
        if (cmd.hasOption("c")) {
            configFilePath = cmd.getOptionValue("c");
        } else {
            configFilePath = System.getProperty("user.dir") + "/conf/client.yaml";
        }

        TestConfiguration config = ConfigReader.parseConfig(configFilePath);
        System.setProperty("qpid.flow_control_wait_failure", "1500000");

        startStatReporting(config);

        int subscriberCount = config.getTopicSubscribers().size() +
                config.getQueueSubscribers().size() + config.getDurableTopicSubscribers().size();
        final List<Thread> threadList = new ArrayList<Thread>(subscriberCount);

        AMQPTopicSubscriber topicSubscriber;
        for (SubscriberConfig subscriberConfig : config.getTopicSubscribers()) {
            topicSubscriber = new AMQPTopicSubscriber();
            topicSubscriber.subscribe(subscriberConfig);
            Thread subThread = new Thread(new ConsumerThread(topicSubscriber, latencyHist, consumerRate));
            subThread.start();
            threadList.add(subThread);
        }

        SimpleConsumer queueReceiver;
        for (SubscriberConfig subscriberConfig : config.getQueueSubscribers()) {
            queueReceiver = new AMQPQueueReceiver();
            queueReceiver.subscribe(subscriberConfig);
            Thread subThread = new Thread(new ConsumerThread(queueReceiver, latencyHist, consumerRate));
            subThread.start();
            threadList.add(subThread);
        }

        AMQPDurableTopicSubscriber durableTopicSubscriber;
        for (SubscriberConfig subscriberConfig : config.getDurableTopicSubscribers()) {
            durableTopicSubscriber = new AMQPDurableTopicSubscriber();
            durableTopicSubscriber.subscribe(subscriberConfig);
            Thread subThread = new Thread(new ConsumerThread(durableTopicSubscriber, latencyHist, consumerRate));
            subThread.start();
            threadList.add(subThread);
        }

        // Publishers
        AMQPTopicPublisher topicPublisher;
        for (PublisherConfig publisherConfig : config.getTopicPublishers()) {
            topicPublisher = new AMQPTopicPublisher();
            topicPublisher.init(publisherConfig);
            Thread pubThread = new Thread(new PublisherThread(topicPublisher));
            pubThread.start();
            threadList.add(pubThread);
        }

        AMQPQueueSender queuePublisher;
        for (PublisherConfig publisherConfig : config.getQueuePublishers()) {
            queuePublisher = new AMQPQueueSender();
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


        // barrier. wait till all done
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

    private static void startStatReporting(TestConfiguration config) {
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

    private static void startCSVReport(int csvReportRefreshRate) {
        csvReporter = CsvReporter.forRegistry(metrics)
                .formatFor(Locale.US)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build(new File(System.getProperty("user.dir") + "/logs/metrics"));
        csvReporter.start(csvReportRefreshRate, TimeUnit.SECONDS);
    }
}
