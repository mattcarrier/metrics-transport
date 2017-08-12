/**
 * Copyright 2017 Matt Carrier mcarrieruri@gmail.com
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mattcarrier.metrics.transport.rabbit;

import io.github.mattcarrier.metrics.transport.consumption.MetricConsumer;
import io.github.mattcarrier.metrics.transport.serialization.Serializer;
import io.github.mattcarrier.metrics.transport.serialization.SerializerFactory;
import io.github.mattcarrier.metrics.transport.serialization.transportable.TransportableMetric;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ Client.
 *
 * @author mattcarrier
 * @since Apr 3, 2017
 */
public class RabbitClient {
  private final Connection conn;
  private final Channel    channel;
  private final String     queueName;
  private final Serializer serializer;

  protected RabbitClient(Connection conn, Channel channel, String queueName, Serializer serializer) {
    this.conn = conn;
    this.channel = channel;
    this.queueName = queueName;
    this.serializer = serializer;
  }

  /**
   * Publishes a {@link TransportableMetric} to RabbitMQ.
   *
   * @param metric
   *     the {@link TransportableMetric} to publish
   */
  public void publish(TransportableMetric metric) {
    try {
      channel.basicPublish("", queueName,
                           new BasicProperties.Builder().type(TransportType.TRANSPORTABLE_METRIC.getType()).build(),
                           serializer.serialize(metric)
      );
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Registers a {@link TransportableMetric} consumer with RabbitMQ.
   *
   * @param consumerTag
   *     the consumer tag
   * @param consumer
   *     the consumer
   * @throws IOException
   *     if there are any issues handling the deliveries
   */
  public <T> void consume(String consumerTag, MetricConsumer<T> consumer) throws IOException {
    channel.basicConsume(queueName, true, consumerTag, new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        try {
          consumer.consume(serializer.deserialize(body));
        } catch (Exception e) {
          throw new IOException(e);
        }
      }
    });
  }

  /**
   * Closes the RabbitMQ connection.
   *
   * @throws IOException
   *     if there is an issue with closing the channel or connection
   * @throws TimeoutException
   *     if there is a timeout when closing the channel or connection
   */
  public void close() throws IOException, TimeoutException {
    channel.close();
    conn.close();
  }

  /**
   * {@link RabbitClient} builder.
   *
   * @author mattcarrier
   * @since Apr 4, 2017
   */
  public static class Builder {
    private String username = "guest";
    private String password = "guest";
    private String host     = "localhost";
    private String port     = "5672";
    private String vhost    = "";

    private String              queue        = "metrics-rabbit";
    private boolean             isDurable    = true;
    private boolean             isExclusive  = false;
    private boolean             isAutoDelete = false;
    private Map<String, Object> arguments    = null;

    private String serializerBasePackage = null;

    public Builder username(String username) {
      this.username = username;
      return this;
    }

    public Builder password(String password) {
      this.password = password;
      return this;
    }

    public Builder host(String host) {
      this.host = host;
      return this;
    }

    public Builder port(String port) {
      this.port = port;
      return this;
    }

    public Builder vhost(String vhost) {
      this.vhost = vhost;
      return this;
    }

    public Builder queue(String queue) {
      this.queue = queue;
      return this;
    }

    public Builder durable(boolean isDurable) {
      this.isDurable = isDurable;
      return this;
    }

    public Builder exclusive(boolean isExclusive) {
      this.isExclusive = isExclusive;
      return this;
    }

    public Builder autoDelete(boolean isAutoDelete) {
      this.isAutoDelete = isAutoDelete;
      return this;
    }

    public Builder arguments(Map<String, Object> arguments) {
      this.arguments = arguments;
      return this;
    }

    public Builder serializerBasePackage(String serializerBasePackage) {
      this.serializerBasePackage = serializerBasePackage;
      return this;
    }

    /**
     * Builds the {@link RabbitClient}.
     *
     * @return the {@link RabbitClient}
     * @throws KeyManagementException
     *     if there is an issue with key management
     * @throws NoSuchAlgorithmException
     *     if there is an issue with the algo
     * @throws URISyntaxException
     *     if there is an issue with the URI
     * @throws IOException
     *     if an error is encountered
     * @throws TimeoutException
     *     if a timeout error is encountered
     * @throws InstantiationException
     *     if there is an issue creating the serializer
     * @throws IllegalAccessException
     *     if there is an issue creating the serializer
     */
    public RabbitClient build() throws KeyManagementException, NoSuchAlgorithmException, URISyntaxException,
        IOException, TimeoutException, InstantiationException, IllegalAccessException {
      final ConnectionFactory factory = new ConnectionFactory();
      factory.setUri(buildConnectionUri());
      final Connection conn = factory.newConnection();
      final Channel channel = conn.createChannel();

      channel.queueDeclare(queue, isDurable, isExclusive, isAutoDelete, arguments);
      final SerializerFactory serializerFactory = null == serializerBasePackage ? new SerializerFactory()
          : new SerializerFactory(serializerBasePackage);
      return new RabbitClient(conn, channel, queue, serializerFactory.serializer());
    }

    private String buildConnectionUri() {
      return "amqp://" + username + ":" + password + "@" + host + ":" + port + vhost;
    }
  }
}
