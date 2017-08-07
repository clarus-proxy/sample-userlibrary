package eu.clarussecure.proxy.access;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

public class CLARUSUserOperations {
    // Singleton implementation
    private static CLARUSUserOperations instance = null;
    private int instancesNumber;
    protected MongoDatabase db;
    protected MongoClient mongoClient;

    protected CLARUSUserOperations() {
        // Correctly configure the log level
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);
        // Create a new client connecting to "localhost" on port 
        this.mongoClient = new MongoClient("localhost", 27017);

        // Get the database (will be created if not present)
        this.db = mongoClient.getDatabase("CLARUS");

        this.instancesNumber++;
    }

    public void deleteInstance() {
        this.instancesNumber--;

        if (this.instancesNumber <= 0) {
            this.mongoClient.close();
            CLARUSUserOperations.instance = null;
        }
    }

    public static CLARUSUserOperations getInstance() {
        if (CLARUSUserOperations.instance == null)
            CLARUSUserOperations.instance = new CLARUSUserOperations();
        return CLARUSUserOperations.instance;
    }

    public boolean addUser(String username, String password, String role) {
        MongoCollection<Document> collection = db.getCollection("users");

        Document doc = new Document("username", username);
        // FIXME - Only admin user are added in the meantime
        doc.append("profile", role);
        doc.append("password", password);

        collection.insertOne(doc);

        return true;
    }

    public Set<String> listUsers() {
        MongoCollection<Document> collection = db.getCollection("users");
        Set<String> result = new HashSet<>();

        // Iterate over the results
        MongoCursor<Document> cursor = collection.find().iterator();

        while (cursor.hasNext()) {
            result.add(cursor.next().getString("username"));
        }

        return result;
    }
}
