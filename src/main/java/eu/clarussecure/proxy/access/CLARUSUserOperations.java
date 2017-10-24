package eu.clarussecure.proxy.access;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
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

    private String confFile = "/etc/clarus/clarus-mgmt-tools.conf";
    private String mongoDBHostname = "localhost"; // Default server
    private int mongoDBPort = 27017; // Default port
    private String clarusDBName = "CLARUS"; // Default DB name

    protected CLARUSUserOperations() {
        // Correctly configure the log level
        Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
        mongoLogger.setLevel(Level.SEVERE);
        // Open the configuraiton file to extract the information from it.
        this.processConfigurationFile();
        // Create a new client connecting to "localhost" on port 
        this.mongoClient = new MongoClient(this.mongoDBHostname, this.mongoDBPort);

        // Get the database (will be created if not present)
        this.db = mongoClient.getDatabase(this.clarusDBName);

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

    private void processConfigurationFile() throws RuntimeException {
        // Open the file in read-only mode. This will avoid any permission problem
        try {
            // Read all the lines and join them in a single string
            List<String> lines = Files.readAllLines(Paths.get(this.confFile));
            String content = lines.stream().reduce("", (a, b) -> a + b);

            // Use the bson document parser to extract the info
            Document doc = Document.parse(content);
            this.mongoDBHostname = doc.getString("CLARUS_metadata_db_hostname");
            this.mongoDBPort = doc.getInteger("CLARUS_metadata_db_port");
            this.clarusDBName = doc.getString("CLARUS_metadata_db_name");
        } catch (IOException e) {
            throw new RuntimeException("CLARUS configuration file could not be processed", e);
        }
    }
}
