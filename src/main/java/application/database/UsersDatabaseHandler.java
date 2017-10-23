package application.database;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.types.ObjectId;
import com.google.common.hash.Hashing;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.eq;
import com.ibm.json.java.JSONObject;

public class UsersDatabaseHandler {

   public static final String DATABASE_NAME = "saveory_app";
   public static final String DATABASE_CLIENT_URI = "mongodb://sapphires:saveoryArmory@sapphires-db.rtp.raleigh.ibm.com/saveory_app";
   public static final String DATABASE_COLLECTION_NAME = "users";
   private static MongoClient mongo_instance;

   public static Document getUser(String token){
	return UsersDatabaseHandler.getUsersCollection().find(eq("_id", token)).first();
   }
   
   private static String hashAndSaltPassword(String password, String username) {
	   return  Hashing.sha256().hashString(username + password, StandardCharsets.UTF_8).toString(); 
   }
 
   /**
    * 
    */
   public static String addNewUser(String name, String email, String username, String password) {
	   
	   Document newUser = new Document();
	   newUser.append("name", name); 
	   newUser.append("email", email); 
	   newUser.append("username", username); 

	   String hashAndSalt = hashAndSaltPassword(password, username); 
	   newUser.append("password", hashAndSalt); 
	   
	   UsersDatabaseHandler.getUsersCollection().insertOne(newUser);
	   ObjectId token = newUser.getObjectId("_id");
	   return token.toString();
   }

   public static void deleteUser(String token){
	//Get the collection and remove the entry matching the token, which maps to document _ids
	UsersDatabaseHandler.getUsersCollection().deleteOne(eq("_id", token));   
   }

   public static boolean updateUser(String token, JSONObject newData){
	//Try to get the user data using the given token
	Document userData = getUser(token);
	if(userData == null){
		return false;
	}
	
	//Get the username
	String username = (String) newData.get("username");
	if(username == null) {
		username = userData.getString("username");
	}
	
	//update data
	String key;
	String data;
	for(Object field : newData.keySet()) {
		key = (String) field;
		data = (String) newData.get(field);
		
		//Hash if password
		if(key == "password") {
			data = hashAndSaltPassword(data, username);
		}
		
		userData.replace(key, data);
	}

	//Update database
	getUsersCollection().updateOne(eq("_id", token), userData);
	return true;
   }
   
   
   /**
    * 
    */
   public static boolean checkExistingUsername(String username) {
	   
	   long numberOfUsers = queryIfUserExists("username", username); 
	   return numberOfUsers == 0 ? false : true; 
   }
   
   
   /**
    * 
    */
   public static boolean checkExistingEmail(String email) {
	   
	   long numberOfUsers = queryIfUserExists("email", email); 
	   return numberOfUsers == 0 ? false : true; 
   }

   public static boolean checkExistingToken(String token){
	   long numberOfUsers = queryIfUserExists("_id", token);
	   return numberOfUsers == 0 ? false : true;
   }
   
   
   /**
    * 
    */
   public static boolean comparePassword(String username, String password) {
	   
	   FindIterable<Document> users = queryUsernameAndPassword(username, password); 
	   
	   // If we haven't found a user, that meant the query wasn't a match, so return false 
	   return users.first() == null ? false : true; 
   }
   
   
   /**
    * 
    */
   public static String retrieveUserToken(String username, String password) {
	   
	   FindIterable<Document> users = queryUsernameAndPassword(username, password);
	   
	   if (users.first() == null) {
		   return "No user token found"; 
	   }
	   
	   Document user = users.first();
	   ObjectId token = user.getObjectId("_id"); 
	   return token.toString(); 
   }

   public static String getUserField(String username, String password, String field){
	   FindIterable<Document> users = queryUsernameAndPassword(username, password);
	   
	   if (users.first() == null) {
		   return "No data in field "+field+" for user "+username; 
	   }
	   
	   Document user = users.first();
	   String val = user.getString(field); 
	   return val; 
	
   }
   
   
   /**
    * 
    */
   private static MongoClient getMongoClient() {

      if (UsersDatabaseHandler.mongo_instance == null) {
         MongoClientURI connectionString = new MongoClientURI(UsersDatabaseHandler.DATABASE_CLIENT_URI);
         UsersDatabaseHandler.mongo_instance = new MongoClient(connectionString);
      }
      return UsersDatabaseHandler.mongo_instance;
   }
   
   
   /**
    * 
    */
   private static MongoCollection<Document> getUsersCollection() {

      MongoDatabase database = UsersDatabaseHandler.getMongoClient().getDatabase(UsersDatabaseHandler.DATABASE_NAME);
      return database.getCollection(UsersDatabaseHandler.DATABASE_COLLECTION_NAME);
   }
   
   
   /**
    * 
    */
   private static FindIterable<Document> queryUsernameAndPassword(String username, String password) {
	   
	   String hashAndSalt = hashAndSaltPassword(password, username); 
	   BasicDBObject query = new BasicDBObject(); 
	   List<BasicDBObject> params = new ArrayList<>(); 
	   params.add(new BasicDBObject("username", username)); 
	   params.add(new BasicDBObject("password", hashAndSalt)); 
	   query.put("$and", params);
	   
	   return UsersDatabaseHandler.getUsersCollection().find(query); 
   }
   
   
   /**
    * 
    */
   private static long queryIfUserExists(String queryField, String queryString) {
	   
	   BasicDBObject query = new BasicDBObject(); 
	   query.put(queryField, queryString); 
	   return UsersDatabaseHandler.getUsersCollection().count(query); 
   }

   
   
}
