package org.acme.streams;

import javax.enterprise.inject.Produces;
import javax.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.vertx.amqp.AmqpClientOptions;
import io.vertx.core.net.PemTrustOptions;

public class OutboundStreamOptions {

  @ConfigProperty(name = "app.certs.ca.crt")
  String caCrtPath;

  @ConfigProperty(name = "mp.messaging.outgoing.outbound-stream.host")
  String host;
  
  @ConfigProperty(name = "mp.messaging.outgoing.outbound-stream.port")
  int port;

  @ConfigProperty(name = "mp.messaging.outgoing.outbound-stream.username")
  String username;

  @ConfigProperty(name = "mp.messaging.outgoing.outbound-stream.password")
  String password;  

  @Produces
  @Named("amqps-ssl-outbound-stream")
  public AmqpClientOptions getNamedOptions() {
      PemTrustOptions trust =
          new PemTrustOptions().addCertPath(caCrtPath);
  
      return new AmqpClientOptions()
          .setSsl(true)
          .setPemTrustOptions(trust)
          .setHost(host)
          .setPort(port)
          .setUsername(username)
          .setPassword(password)
          //.addEnabledSaslMechanism("EXTERNAL")
          //.setHostnameVerificationAlgorithm("")
          //.setConnectTimeout(30000)
          //.setReconnectInterval(5000)
          //.setContainerId("my-container")
          ;
  }

}


