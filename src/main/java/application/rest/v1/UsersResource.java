package application.rest.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.ibm.json.java.JSONObject;
import com.mongodb.util.JSON;
import application.database.UsersDatabaseHandler;

@Path("users")
public class UsersResource {	
	//Signup	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createUser(@Context final HttpServletRequest request, JSONObject body) {
		
		JSONObject response = new JSONObject();
		
		//Make sure username is unique	
		String username = (String) body.get("username"); 
		if (UsersDatabaseHandler.checkExistingUsername(username)) {
			response.put("message", "Username already exists");
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JSON.serialize(response)).build(); 
		}
		

		//Make sure email is unique
		String email = (String) body.get("email"); 
		if (UsersDatabaseHandler.checkExistingEmail(email)) {
			response.put("message", "Email already exists"); 
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JSON.serialize(response)).build(); 
		}
		
		//Setup new user data and add to database
		String name = (String) body.get("name"); 
		String password = (String) body.get("password");
		String token = UsersDatabaseHandler.addNewUser(name, email, username, password); 
		//Give the token back so other services can authenticate the user and include name and username for UI
		response.put("token", token);
		response.put("name", name);
		response.put("username", username);
		response.put("email", email);
		
		return Response.status(Response.Status.CREATED).entity(JSON.serialize(response)).build();
	}
	
	//Login
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/login")
	public Response loginUser(@Context final HttpServletRequest request, JSONObject body) {
		
		JSONObject response = new JSONObject();
		String username = (String) body.get("username"); 
		String password = (String) body.get("password");
		
		//Make sure the username exists
		if (!(UsersDatabaseHandler.checkExistingUsername(username))) {
			response.put("message", "User not found"); 
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JSON.serialize(response)).build(); 
		}
		
		//Compare the inputed password to the user's actual password
		boolean match = UsersDatabaseHandler.comparePassword(username, password); 
		
		if (!match) {
			response.put("message", "Incorrect username or password");
			return Response.status(Response.Status.UNAUTHORIZED).entity(JSON.serialize(response)).build(); 
		}
		
		//Respond with message and user info
		response.put("message", "User is authenticated"); 
		response.put("token", UsersDatabaseHandler.retrieveUserToken(username, password)); 
		response.put("name", UsersDatabaseHandler.getUserField(username, password, "name"));
		response.put("username", username);
		response.put("email", UsersDatabaseHandler.getUserField(username, password, "email"));

		return Response.status(Response.Status.OK).entity(JSON.serialize(response)).build();
	}
	
	//User data
	@GET
	@Path("/{user_token}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUser(@PathParam("user_token") final String token) {
		JSONObject response = new JSONObject();
		
		//Make sure the token exists
		if(!UsersDatabaseHandler.checkExistingToken(token)){
			response.put("message", "Token invalid");	
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JSON.serialize(response)).build(); 
		}
		
		return Response.status(Response.Status.OK).entity(JSON.serialize(UsersDatabaseHandler.getUserJSON(token))).build();
	}

	//Update
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{user_token}")
	public Response updateUser(@PathParam("user_token") final String token, JSONObject body){
		JSONObject response = new JSONObject();
		
		//Make sure the token exists
		if(!UsersDatabaseHandler.checkExistingToken(token)){
			response.put("message", "Token invalid");	
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JSON.serialize(response)).build(); 
		}

		if(!UsersDatabaseHandler.updateUser(token, body)) {
			response.put("message", "Could not find user data for that token");	
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(JSON.serialize(response)).build(); 
		}
		
		response.put("message", "updated user");	
		response.put("token", token);
		return Response.status(Response.Status.OK).entity(JSON.serialize(response)).build();
		
	}
	
}
