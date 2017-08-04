package application.rest.v1;

import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;


@Path("hello")
public class Example {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response hello(){
		//Create a simple json object with a message
		JsonObject helloObject = Json.createObjectBuilder().add("message", "Hello Saveory").build();
		
		//Respond with our welcoming json object
		return Response.ok(helloObject.toString()).build(); 
	}


}
