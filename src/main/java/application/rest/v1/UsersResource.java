package application.rest.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
		
		JSONObject response = new JSONObject();
		
		String username = (String) body.get("username"); 
		if (UsersDatabaseHandler.checkExistingUsername(username)) {
			response.put("message", "Username already exists");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JSON.serialize(response)).build(); 
		}
		
		String email = (String) body.get("email"); 
		if (UsersDatabaseHandler.checkExistingEmail(email)) {
			response.put("message", "Email already exists"); 
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JSON.serialize(response)).build(); 
		}
		
		String name = (String) body.get("name"); 
		String password = (String) body.get("password");
		String token = UsersDatabaseHandler.addNewUser(name, email, username, password); 
		response.put("token", token);
		
		return Response.status(Response.Status.CREATED).entity(JSON.serialize(response)).build();
	}
	
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/login")
	public Response loginUser(@Context final HttpServletRequest request, JSONObject body) {
		
		JSONObject response = new JSONObject();
		String username = (String) body.get("username"); 
		String password = (String) body.get("password");
		
		if (!(UsersDatabaseHandler.checkExistingUsername(username))) {
			response.put("message", "User not found"); 
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JSON.serialize(response)).build(); 
		}

		boolean match = UsersDatabaseHandler.comparePassword(username, password); 
		
		if (!match) {
			response.put("message", "Incorrect username or password");
			return Response.status(Response.Status.UNAUTHORIZED).entity(JSON.serialize(response)).build(); 
		}
		
		response.put("message", "User is authenticated"); 
		response.put("token", UsersDatabaseHandler.retrieveUserToken(username, password)); 
		return Response.status(Response.Status.OK).entity(JSON.serialize(response)).build();
	}
	
}