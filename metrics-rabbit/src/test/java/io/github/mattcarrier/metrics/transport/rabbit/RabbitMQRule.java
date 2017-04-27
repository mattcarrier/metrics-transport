/**
 * Copyright 2017 Matt Carrier mcarrieruri@gmail.com
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.mattcarrier.metrics.transport.rabbit;

import static org.junit.Assert.fail;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import org.junit.rules.ExternalResource;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ExternalResource} that provides access to RabbitMQ.
 * 
 * @author mattcarrier
 * @since Apr 4, 2017
 */
public class RabbitMQRule extends ExternalResource {
  private static final String   QUEUE_NAME       = "metrics-rabbit";

  public static final String    DEFAULT_USERNAME = "guest";
  public static final String    DEFAULT_PASSWORD = "guest";
  public static final String    DEFAULT_HOST     = "localhost";
  public static final String    DEFAULT_PORT     = "5672";
  public static final String    DEFAULT_VHOST    = "";

  private final String          connectionUri;

  private Connection            conn;
  private Channel               channel;

  private volatile boolean      isExpecting      = false;
  private volatile List<byte[]> messages;
  private volatile int          expected;

  public RabbitMQRule() {
    this(DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_HOST, DEFAULT_PORT, DEFAULT_VHOST);
  }

  public RabbitMQRule(String username, String password, String host, String port, String vhost) {
    connectionUri = "amqp://" + username + ":" + password + "@" + host + ":" + port + vhost;
  }

  /**
   * Initiates expectation mode which starts storing messages until the
   * requested amount is retrieved
   * 
   * @param expected
   *          the number of messages to store
   */
  public void expect(int expected) {
    if (1 > expected) {
      return;
    }

    messages = new ArrayList<>(expected);
    this.expected = expected;
    isExpecting = true;
  }

  /**
   * Wait until the requested amount of messages from expect has been received.
   * 
   * @param timeout
   *          the timeout
   * @return the received messages
   */
  public List<byte[]> wait(Duration timeout) {
    long stop = System.currentTimeMillis() + timeout.toMillis();
    while (isExpecting && stop > System.currentTimeMillis()) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }

    if (isExpecting) {
      fail("Reached timeout waiting for expected messages [" + expected + "]");
    }

    List<byte[]> messages = new ArrayList<>(this.messages);
    this.messages = new ArrayList<>(expected);
    return messages;
  }

  /**
   * Purges the queue.
   */
  public void purge() {
    try {
      channel.queuePurge(QUEUE_NAME);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void before() throws Throwable {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setUri(connectionUri);
    conn = factory.newConnection();
    channel = conn.createChannel();

    // create the queue and register the consumer
    channel.queueDeclare(QUEUE_NAME, false, false, true, null);
    channel.basicConsume(QUEUE_NAME, true, "metrics-rabbit-consumer", new DefaultConsumer(channel) {
      @Override
      public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
          throws IOException {
        if (!isExpecting || !QUEUE_NAME.equals(envelope.getRoutingKey())) {
          return;
        }

        messages.add(body);
        if (messages.size() == expected) {
          isExpecting = false;
        }
      }
    });
  }

  @Override
  protected void after() {
    try {
      channel.close();
      conn.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
