package org.momento.echidna.services;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.momento.echidna.Echidna;
import org.momento.echidna.network.MongoDTO;

import java.util.List;
import java.util.stream.Collectors;

public class MongoDBService {

    private static final String URL = "mongodb://";
    private static MongoDatabase database;
    private static MongoClient client;
    private static boolean connected = false;

    public static void connect() {
        String ip = Echidna.databaseSection.getString("ip");
        String port = Echidna.databaseSection.getString("port");
        String dbName = Echidna.databaseSection.getString("dbName");
        if (ip == null || port == null || dbName == null)
            throw new IllegalArgumentException("ip, port, or dbName is null");
        ConnectionString connectionString = new ConnectionString(URL + ip + ":" + port);
        client = MongoClients.create(MongoClientSettings.builder()
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .applyConnectionString(connectionString)
                .build());
        database = client.getDatabase(dbName);
        connected = true;
    }

    public static <T extends MongoDTO> void sendManyData(String collectionName, List<T> dtoList) {
        if (dtoList.isEmpty()) return;
        MongoCollection<Document> collection = database.getCollection(collectionName);
        List<Document> documents = dtoList.stream()
                .map(MongoDTO::toDocument)
                .collect(Collectors.toList());
        collection.insertMany(documents);
    }

    public static void disconnect() {
        if (client != null) {
            client.close();
            client = null;
        }
        database = null;
        connected = false;
    }

    public static boolean isConnected() {
        return connected;
    }
}
