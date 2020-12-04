package org.acme.rest;

import com.google.gson.Gson;

import org.acme.database.entity.Account;
import org.acme.jms.QueueProducer;
import org.acme.streams.StreamProducer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/api")
public class Controller {

    @Inject
    QueueProducer queueProducer;

    @Inject
    StreamProducer streamProducer;

    /*
     * @Autowired private DatabaseConsumer databaseConsumer;
     */
    @PUT
    @Path("/queue/{dest}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public String sendQueueMessage(@PathParam("dest") String dest, @PathParam("name") String name) {
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("name", name);
        String message = new Gson().toJson(fields);
        queueProducer.sendMessage(dest, message);
        Map<String, String> output = new HashMap<String, String>();
        output.put("status", "success");
        output.put("sendMessage", message);
        return new Gson().toJson(output);
    }

    @PUT
    @Path("/stream/{dest}/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public String sendStreamMessage(@PathParam("dest") String dest, @PathParam("name") String name) {
        Map<String, String> fields = new HashMap<String, String>();
        fields.put("name", name);
        String message = new Gson().toJson(fields);
        streamProducer.sendMessage(dest, message);
        Map<String, String> output = new HashMap<String, String>();
        output.put("status", "success");
        output.put("sendMessage", message);
        return new Gson().toJson(output);
    }

    @GET
    @Path("/database/user/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public String findAccountByUser(@PathParam("name") String name) {
        Account account = Account.findByUserName(name);
        return new Gson().toJson(account);
    }

    @GET
    @Path("/database/name/{firstName}/{lastName}")
    @Produces(MediaType.APPLICATION_JSON)
    public String findAccountByName(@PathParam("firstName") String firstName, @PathParam("lastName") String lastName) {
        List<Account> accounts = Account.findByFirstNameAndLastNameOrderByUserName(firstName, lastName);
        return new Gson().toJson(accounts);
    }

}
