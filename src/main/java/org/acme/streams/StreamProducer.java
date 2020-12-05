package org.acme.streams;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.google.gson.Gson;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.reactive.messaging.amqp.OutgoingAmqpMetadata;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class StreamProducer {
  private final static Logger log = Logger.getLogger(StreamProducer.class.getName());
  @Inject 
  @Channel("inbound-stream") 
  Emitter<String> emitter;

  public void sendMessage(String streamDestination, final String message) {
    Map<String, String> headers = new HashMap<String, String>();
    headers.put("date", "2000-01-01");
    headers.put("domain", "maas");
    headers.put("type", "instance");
    final JsonObject headerJson = new JsonObject(new Gson().toJson(headers));

    OutgoingAmqpMetadata metadata = OutgoingAmqpMetadata.builder().withAddress(streamDestination)
        .withApplicationProperties(headerJson).build();

    log.info("Sending message [" + message + "] to - " + streamDestination);
    emitter.send(Message.of(message).addMetadata(metadata));

  }

  
}