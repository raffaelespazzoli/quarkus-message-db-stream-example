package org.acme.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class TopicConsumer implements Runnable {
  
  private final static Logger log = Logger.getLogger(TopicConsumer.class.getName());

  @Inject
  ConnectionFactory connectionFactory;

  @ConfigProperty(name = "app.topics.inbound")
  String inboundTopic;

  @ConfigProperty(name = "app.topics.outbound")
  String outboundTopic; 

  private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

  void onStart(@Observes StartupEvent ev) {
      scheduler.submit(this);
  }

  void onStop(@Observes ShutdownEvent ev) {
      scheduler.shutdown();
  }

  @Override
  public void run() {
      try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
          JMSConsumer consumer = context.createConsumer(context.createTopic(inboundTopic));
          JMSProducer producer = context.createProducer();
          while (true) {
              Message message = consumer.receive();
              Map<String, String> headers = new HashMap<String,String>();
              headers.put("date", "2000-01-01");
              headers.put("domain", "maas");
              headers.put("type", "instance");
              if (message == null) return;
              String jsonMessage=message.getBody(String.class);
              log.info("Received queue message: body: " + jsonMessage);
              TextMessage txtMsg = context.createTextMessage(jsonMessage);
              for (String key : headers.keySet()){
                txtMsg.setObjectProperty(key, headers.get(key));
              }
              producer.send(context.createTopic(outboundTopic), txtMsg);
          }
      } catch (JMSException e) {
          throw new RuntimeException(e);
      }
  }
}
