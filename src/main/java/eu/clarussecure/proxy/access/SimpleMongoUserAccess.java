package eu.clarussecure.proxy.access;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import static com.mongodb.client.model.Filters.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

public class SimpleMongoUserAccess extends CLARUSUserOperations implements CLARUSAccess {
    private static SimpleMongoUserAccess instance = null;
    private int instancesNumber;

    private SimpleMongoUserAccess() {
        // Correctly configure the log level
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);
        // Create a new client connecting to "localhost" on port 
        this.mongoClient = new MongoClient("localhost", 27017);

        // Get the database (will be created if not present)
        this.db = mongoClient.getDatabase("CLARUS");

        this.instancesNumber++;
    }

    @Override
    public void deleteInstance() {
        this.instancesNumber--;

        if (this.instancesNumber <= 0) {
            this.mongoClient.close();
            SimpleMongoUserAccess.instance = null;
        }
    }

    public static SimpleMongoUserAccess getInstance() {
        if (SimpleMongoUserAccess.instance == null)
            SimpleMongoUserAccess.instance = new SimpleMongoUserAccess();
        return SimpleMongoUserAccess.instance;
    }

    @Override
    public boolean authenticate(String username, String password) {
        // This method assumes the username exists. If not, it will fail.
        // It is encourages to use this.identify(username= before this method.
        MongoCollection<Document> collection = db.getCollection("users");

        // Search the user entry
        Document user = collection.find(eq("username", username)).first();
        // Compare the passwords

        return user.getString("password").equals(password);
    }

    @Override
    public boolean identify(String username) {
        MongoCollection<Document> collection = db.getCollection("users");

        // Search all the entries with the given username
        long users = collection.count(eq("username", username));

        // Return true iff only one was found
        return users == 1;
    }

    public String userProfile(String username) {
        MongoCollection<Document> collection = db.getCollection("users");

        // Search the user entry
        Document user = collection.find(eq("username", username)).first();
        return user.getString("profile");
    }
}