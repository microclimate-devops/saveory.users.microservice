package application.database;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import com.google.common.hash.Hashing;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.nio.charset.StandardCharsets;

public class UsersDatabaseHandler {

   public static final String DATABASE_NAME = "saveory_app";
   public static final String DATABASE_CLIENT_URI = "mongodb://sapphires:saveoryArmory@sapphires-db.rtp.raleigh.ibm.com/saveory_app";
   public static final String DATABASE_COLLECTION_NAME = "users";
   private static MongoClient mongo_instance;

   	
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
   public static String addNewUser(String name, String email, String username, String password) {
	   
	   Document newUser = new Document();
	   newUser.append("name", name); 
	   newUser.append("email", email); 
	   newUser.append("username", username); 

	   String hashAndSalt = Hashing.sha256().hashString(username + password, StandardCharsets.UTF_8).toString(); 
	   newUser.append("password", hashAndSalt); 
	   
	   UsersDatabaseHandler.getUsersCollection().insertOne(newUser);
	   ObjectId token = newUser.getObjectId("_id");
	   return token.toString();
   }
   
   
   /**
    * 
    */
   public static boolean checkExistingUser(String id) {
	   
	   BasicDBObject userQuery = new BasicDBObject(); 
	   userQuery.put("_id", id); 
	   FindIterable<Document> users = UsersDatabaseHandler.getUsersCollection().find(userQuery); 
	   
	   // Return false if that username is not found, true if we found that username
	   return users == null ? false : true; 
   }
   
   
   /**
    * 
    */
   public static boolean comparePassword(String username, String password) {
	   
	   String hashAndSalt = Hashing.sha256().hashString(username + password, StandardCharsets.UTF_8).toString(); 
	   BasicDBObject query = new BasicDBObject(); 
	   List<BasicDBObject> params = new ArrayList<>(); 
	   params.add(new BasicDBObject("username", username)); 
	   params.add(new BasicDBObject("password", hashAndSalt)); 
	   query.put("$and", params); 
	   FindIterable<Document> users = UsersDatabaseHandler.getUsersCollection().find(query); 
	   
	   // Return false if that password does not match with the username
	   return users == null ? false : true; 
   }
}