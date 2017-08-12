metrics-transport
=================

A serialization and transport library for [Dropwizard Metrics](http://metrics.dropwizard.io/).

Aggregate your metrics for easier storage and reporting by serializing and transporting them to a centralized service.

#### Available Serializers:
* Java Serialization (default)
* [Kryo Serialization](https://github.com/EsotericSoftware/kryo)

#### Available Transports:
* [RabbitMQ](https://www.rabbitmq.com/)

[![Travis](https://img.shields.io/travis/mattcarrier/metrics-transport.svg)](https://travis-ci.org/mattcarrier/metrics-transport)
[![SonarQube Tech Debt](https://sonarqube.com/api/badges/measure?key=io.github.mattcarrier.metrics.transport:metrics-transport-parent&metric=sqale_debt_ratio)](https://sonarqube.com/dashboard?id=io.github.mattcarrier.metrics.transport%3Ametrics-transport-parent)
[![Coveralls](https://img.shields.io/coveralls/mattcarrier/metrics-transport.svg)](https://coveralls.io/github/mattcarrier/metrics-transport)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.mattcarrier.metrics.transport/metrics-transport-parent.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.github.mattcarrier.metrics.transport%22)

Usage
-----

1. Include the appropriate transport library
```xml
...
<dependency>
  <groupId>io.github.mattcarrier.metrics.transport</groupId>
  <artifactId>metrics-rabbit</artifactId>
  <version>0.4.0</version>
</dependency>
...
```
2. Optional: Include a serialization library (will default to Java Serialiation if none available)
```xml
...
<dependency>
  <groupId>io.github.mattcarrier.metrics.transport</groupId>
  <artifactId>metrics-serialization-kryo</artifactId>
  <version>0.4.0</version>
</dependency>
...
```
3. Include the appropriate consumption library
```xml
...
<dependency>
  <groupId>io.github.mattcarrier.metrics.transport</groupId>
  <artifactId>metrics-consumption-influxdb</artifactId>
  <version>0.4.0</version>
</dependency>
...
```
4a. Serialization: Create and start the transport reporter
```java
RabbitReporter reporter = new RabbitReporter.Builder(registry).metricMeta(metricMeta)
    .build(new RabbitClient.Builder().host(RABBIT_HOST).durable(false).autoDelete(true).build());
reporter.start(100, TimeUnit.MILLISECONDS);
```
4b. Deserialization: Create and start the consumer
```java
RabbitClient client = new RabbitClient.Builder().build();
InfluxDbMetricConsumer consumer = new InfluxDbMetricConsumer.Builder().build();
client.consume("myConsumerTag", consumer);
```

Development
-----------

Metrics-Transport is built entirely using [Docker](https://www.docker.com/) and [dobi](https://github.com/dnephin/dobi).

#### Requirements
* [Docker](https://www.docker.com/)
* [Docker Compose](https://docs.docker.com/compose/)
* [dobi](https://github.com/dnephin/dobi)

#### Build
```bash
dobi
```
