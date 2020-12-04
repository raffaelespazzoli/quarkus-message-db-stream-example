package org.acme.jms;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import com.google.gson.Gson;

/**
 * A bean producing random prices every 5 seconds and sending them to the prices
 * JMS queue.
 */
@ApplicationScoped
public class QueueProducer {

    @Inject
    ConnectionFactory connectionFactory;

    private final static Logger log = Logger.getLogger(QueueProducer.class.getName());

    @SuppressWarnings("unchecked")
    public void sendMessage(String dest, final String message) {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            final String correlationID = "CorrelationID";
            Map<String, String> map = (Map<String, String>)new Gson().fromJson(message, Map.class);
            final String textMessage = "Hello " + map.get("name");
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("date", "2000-01-01");
            headers.put("domain", "maas");
            headers.put("type", "instance");
            final String headerJson = new Gson().toJson(headers);
            log.info("Sending message [" + message + "] to - " + dest);
            TextMessage txtMsg = context.createTextMessage(textMessage);
            txtMsg.setJMSCorrelationID(correlationID);
            txtMsg.setStringProperty("header", headerJson);
            if (dest.endsWith("queue")){
                context.createProducer().send(context.createQueue(dest), txtMsg);
            }
            if (dest.endsWith("topic")){
                context.createProducer().send(context.createTopic(dest), txtMsg);
            }       
        } catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}