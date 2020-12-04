package org.acme.streams;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import com.google.gson.Gson;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

import io.smallrye.reactive.messaging.amqp.OutgoingAmqpMetadata;
import io.smallrye.reactive.messaging.annotations.Broadcast;
import io.smallrye.reactive.messaging.annotations.Merge;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class StreamConsumer {
  private final static Logger log = Logger.getLogger(StreamConsumer.class.getName());

  @Incoming("inbound-stream")
  @Outgoing("outbound-stream")
  @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
  @Broadcast
  @Merge
  //@StreamListener(target=EventChannels.STREAM_REQUEST, condition = "@checkHeaders.test(headers['event-type'])")
  public Message<String> handleRequestMessageEvent(String jsonMessage) {
      log.info("Handle stream message " + jsonMessage);
      Message<String> streamMessage = generateMessage(jsonMessage);
      log.info("Sending message [" + jsonMessage + "] to - " + "outbound.stream");
      //channels.streamResponse().send(streamMessage);
      return streamMessage;
  }

  @Incoming("inbound-stream")
  @Outgoing("outbound-stream")
  // Send to all subscribers
  @Broadcast
  // Acknowledge the messages before calling this method.
  @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
  @Merge
  public Message<String> transformRequestMessageEvent(String jsonMessage) {
      log.info("Transform stream message " + jsonMessage);
      Message<String> streamMessage = generateMessage(jsonMessage);
      log.info("Sending message [" + jsonMessage + "] to - " + "outbound.stream");
      return streamMessage;
  }

  @SuppressWarnings("unchecked")
  protected Message<String> generateMessage(String jsonMessage) {
      Map<String, String> map = new Gson().fromJson(jsonMessage, Map.class);

      Map<String, String> headers = new HashMap<String,String>();
      headers.put("date", "2000-01-01");
      headers.put("domain", "maas");
      headers.put("type", "instance");
      final JsonObject headerJson = new JsonObject(new Gson().toJson(headers));

      Map<String, String> fields = new HashMap<String,String>();
      fields.put("data", map.get("name"));
      String data = new Gson().toJson(fields);

      OutgoingAmqpMetadata metadata = OutgoingAmqpMetadata.builder().withApplicationProperties(headerJson).build();

      return Message.of(data).addMetadata(metadata);
  }
}
