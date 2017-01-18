WSO2 Message Broker Test Client
==============

Overview
-----------
Performance testing client for WSO2 Message Broker. 

Goal of this project is to write a comprehensive test client for 
[WSO2 Message Broker (MB)](http://wso2.com/products/message-broker/). 
This is a java based  console application with the capability to run test scenarios according to a configuration file 
given at startup. 

Currently this client supports testing AMQP protocol based functionality of MB. Client is written using 
[JMS](https://docs.oracle.com/javaee/6/tutorial/doc/bncdr.html)

#### Supported functionality

> - Message Publishers (Topic and queue)
>   - multiple publishers (same and different queues)
>   - [Disruptor](https://lmax-exchange.github.io/disruptor/) based transactional message publishing
>   - publisher failover configuration
>  
> - Message Subscribers (Topic and queue)
>   - multiple subscribers ( same and different queues)
>   - subscriber failover configuration
>   - durable topic subscribers
> 
> - Test results analytics
>   - supports test results analytics with [metrics](https://dropwizard.github.io/metrics/) library 
>   - graphical view of results
>   - message publishing rate (individual publisher and total publisher rates)
>   - subscriber receiving rate (individual subscriber and total subscriber rates)
>   - message latency (publishing to receiving message latency)
>   
> - SL4j logging support
>  

Build
------
To get started clone this project and build using maven (or else get the binaries from the 
[releases](https://github.com/Asitha/andes-test-client/releases).)

```
 mvn clean install
```

Run
----
Extract andes-test-client-\<VERSION\>-pack.zip in `target/` directory. Inside the extracted file execute the 
**andes-test-client_\<VERSION\>** executable to run the client. This will use the default configuration file 
(\<ATC_HOME\>/conf/client.yaml) to run the test.
```
$ ./andes-test-client_1.0-SNAPSHOT
```
If you want to run a different test configuration point to that file using option `--conf` or `-c`

```
$ ./andes-test-client_1.0-SNAPSHOT --conf <path to configuration file>

$ ./andes-test-client_1.0-SNAPSHOT -c <path to configuration file>
```

Configure Test Cases
-------------------------

Use `conf/client.yaml` to configure test cases
For more information on how to configure test cases see following wiki pages

- [Get started with writing your own test configuration](https://github.com/Asitha/andes-test-client/wiki/Get-started-with-writing-your-own-test)
- [Configuration attributes in detail](https://github.com/Asitha/andes-test-client/wiki/Configuration-attributes-in-detail)


Results
---------
After running a test go to `logs/metrics/` directory to view the metrics csv output.
To generate graphs of the results use `result-viewer.html` 
