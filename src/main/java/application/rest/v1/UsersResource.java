package application.rest.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.ibm.json.java.JSONObject;
import com.mongodb.util.JSON;

import application.database.UsersDatabaseHandler;

@Path("users")
public class UsersResource {
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(@Context final HttpServletRequest request, JSONObject body) {
		
		String username = (String) body.get("username"); 
		
		if (UsersDatabaseHandler.checkExistingUsername(username)) {
			JSONObject resp = new JSONObject(); 
			resp.put("Status", Response.Status.CONFLICT); 
			resp.put("reason", "Username already exists"); 
			return Response.status(Response.Status.CONFLICT).entity(JSON.serialize(resp)).build(); 
		}
		
		String name = (String) body.get("name"); 
		String email = (String) body.get("email"); 
		String password = (String) body.get("password");
		String token = UsersDatabaseHandler.addNewUser(name, email, username, password); 
		
		JSONObject response = new JSONObject();
		response.put("token", token); 
		
		return Response.ok(JSON.serialize(response)).entity("User has been created").build(); 
	}
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/{userToken}")
	public Response retrieveUser(@Context final HttpServletRequest request, @PathParam("userToken") String userToken) {
		
		if (UsersDatabaseHandler.checkExistingUser(userToken)) {
			return Response.ok().entity("User " + userToken + " Exists!").build(); 
		}
		return Response.status(Response.Status.NOT_FOUND).build(); 
	}
}
