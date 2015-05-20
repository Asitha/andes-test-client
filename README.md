Andes Test Client (ATC)
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
Extract andes-test-client-\<VERSION\>-pack.zip in `target/` directory. Inside the extracted file execute the **client.sh** file to run the client. This will use the default configuration file (\<ATC_HOME\>/conf/client.yaml) to run the test.
```
sh client.sh
```
If you want to run a different test configuration point to that file using option `--conf` or `-c`

```
sh client.sh --conf <path to configuration file>

sh client.sh -c <path to configuration file>
```

Configure Test Cases
-------------------------

Use `conf/client.yaml` to configure test cases

Configuration file contains four scopes for configuration variables. They are

| Scope | Description |
|------| ------------|
| Global | Relevant to the whole test. for instance stat reporting. These values can be put only on top of the configuration. Not under publisher or subscriber configuration|
| PubSub | Common to subscribers and publishers. If put under global level publishers and subscribers will inherit that value unless it is overidden by setting a value at publisher or subscriber level.  |
| Publisher | Only publisher related. Can only be used with a publishers |
| Subscriber | Only subscriber related. Can only be used with a subscribers | 

Following are the configurations and their types

| Variable | Scope | Type | Description |
|----------|-------|------|-------------|
| console_report_enable | Global | Boolean | Writes performance statistics to the console if enabled
| console_report_update_interval_seconds | Global | Integer | Time interval (milliseconds) between consecutive console reports 
| csv_report_enable | Global | Boolean | If true performance statistics will be saved in a csv format files in \<ATC_HOME\>/logs/metrics directory
| csv_report_update_interval_seconds | Global | Integer | Tme interval to update the csv files
| csv_gauges_update_interval_milis | Global | Integer | Gauge will calculate messages received and published within this interval. This is a rate per csvGaugeUpdateInterval
|jmx_report_enable | Global | Boolean | Outputs performance statistics through JMX. Through an application like JConsole performance stats can be viewed
|||||
|hostname | PubSub | String | Hostname of the connecting Message Broker
| port | PubSub | Integer | port of the connecting Message Broker node
| username | PubSub | String | username to log into Message Broker 
| password | PubSub | String | passeord to log into Message Broker
| queue_name | PubSub | String | Queue or topic name to publish or subscribe to


Results
---------
After running a test go to `logs/metrics/` directory to view the metrics csv output.
To generate graphs of the results use `result-viewer.html` 
