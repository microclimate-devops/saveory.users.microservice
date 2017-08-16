package application.rest.v1;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.ibm.json.java.JSONObject;
import application.database.UsersDatabaseHandler;

@Path("/users")
public class UsersResource {
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response createUser(@Context final HttpServletRequest request, JSONObject body) {
		
		String username = (String) body.get("username"); 
		String password = (String) body.get("password"); 
		UsersDatabaseHandler.addNewUser(username, password);
		return Response.ok().build(); 
	}
}
