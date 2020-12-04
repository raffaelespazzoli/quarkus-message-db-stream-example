package org.acme.jms;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

/**
 * A bean consuming prices from the JMS queue.
 */
@ApplicationScoped
public class QueueConsumer implements Runnable {
    private final static Logger log = Logger.getLogger(QueueConsumer.class.getName());

    @Inject
    ConnectionFactory connectionFactory;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    void onStart(@Observes StartupEvent ev) {
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @ConfigProperty(name = "app.queues.inbound")
    String inboundQueue;

    @ConfigProperty(name = "app.queues.outbound")
    String outboundQueue;    

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(context.createQueue(inboundQueue),"JMSCorrelationID LIKE 'Correlation%'");
            JMSProducer producer = context.createProducer();
            while (true) {
                Message message = consumer.receive();
                if (message == null) continue;
                Map<String,Object> headerMap=new HashMap<String,Object>();
                for (String key: Collections.list((Enumeration<String>)message.getPropertyNames())){
                    headerMap.put(key,message.getObjectProperty(key));
                }
                String headers = headerMap.entrySet().stream()
                .map(entry -> " " + entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
                String jsonMessage=message.getBody(String.class);
                log.info("Received queue message:\n" + headers + "\n body: " + jsonMessage);
                
                //Map map = new Gson().fromJson(jsonMessage, Map.class);
                producer.send(context.createQueue(outboundQueue), jsonMessage);
            }
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }
}