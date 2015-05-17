Andes Test Client
==============

Overview
-----------
Performance testing client for WSO2 Message Broker. 

Goal of this project is to write a comprehensive test client for [WSO2 Message Broker (MB)](http://wso2.com/products/message-broker/). This is a java based  console application with the capability to run test scenarios according to a configuration file given at startup. 

Currently this client supports testing AMQP protocol based functionality of MB. Client is written using [JMS](https://docs.oracle.com/javaee/6/tutorial/doc/bncdr.html)

#### Supported functionality

> - Message Publishers (Topic and queue)
>   - multiple publishers (same and different queues)
>   - transactional message publishing
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
To get started clone this project and build using maven
```
 mvn clean install
```

Run
----
Extract andes-test-client-\<VERSION\>-pack.zip in `target/` directory and execute 
```
sh client.sh
```

Configure Test Cases
-------------------------

Use `conf/client.yaml` to configure test cases

Results
---------
After running a test go to `logs/metrics/` directory to view the metrics csv output.
To generate graphs of the results use `result-viewer.html` 
