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
import application.database.UsersDatabaseHandler;

@Path("users")
public class UsersResource {
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(@Context final HttpServletRequest request, JSONObject body) {
		
		String name = (String) body.get("name"); 
		String email = (String) body.get("email"); 
		String username = (String) body.get("username"); 
		String password = (String) body.get("password");
		
		String token = UsersDatabaseHandler.addNewUser(name, email, username, password); 
		
		JSONObject response = new JSONObject();
		response.put("bearer_token", token); 
		return Response.ok(response).build();
	}
	
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/user/{userId}")
	public Response retrieveUser(@Context final HttpServletRequest request, @PathParam("userId") String userId) {
		
		if (UsersDatabaseHandler.checkExistingUser(userId)) {
			
			return Response.ok("User exists!").build();
		}
		
		return Response.status(Response.Status.NOT_FOUND).build(); 
	}
}
