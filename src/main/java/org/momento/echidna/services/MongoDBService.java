package org.momento.echidna.services;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.UuidRepresentation;
import org.bson.types.ObjectId;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.momento.echidna.Echidna;
import org.momento.echidna.network.ItemDTO;
import org.momento.echidna.player.InventorySync;

import java.util.List;
import java.util.Objects;

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

    public static <T> void sendData(String collectionName, T dto) {
        Class<T> clazz = (Class<T>) dto.getClass();
        MongoCollection<T> collection = database.getCollection(collectionName, clazz);
        collection.insertOne(dto);
    }

    public static <T> void sendManyData(String collectionName, List<T> dtoList) {
        if (dtoList.isEmpty()) return;
        Class<T> clazz = (Class<T>) dtoList.get(0).getClass();
        MongoCollection<T> collection = database.getCollection(collectionName, clazz);
        collection.insertMany(dtoList);
    }

    public static ItemDTO getStorageItem(ItemStack itemStack, int slot, Location location) {
        MongoCollection<ItemDTO> collection = database.getCollection("items", ItemDTO.class);
        return Objects.requireNonNullElse(
                collection.find(Filters.and(
                Filters.eq("is_in_a_storage", true),
                    Filters.eq("hash", InventorySync.hashItemStack(itemStack)),
                    Filters.eq("slot", slot),
                    Filters.eq("x", location.getBlockX()),
                    Filters.eq("y", location.getBlockY()),
                    Filters.eq("z", location.getBlockZ()))
                ).first(), new ItemDTO(itemStack, location, slot));
    }

    public static ItemDTO getPlayerItem(ObjectId id) {
        MongoCollection<ItemDTO> collection = database.getCollection("items", ItemDTO.class);
        return collection.find(Filters.eq("_id", id)).first();
    }

    public static ItemDTO getPlayerItem(Player player, ItemStack itemStack, int slot) {
        MongoCollection<ItemDTO> collection = database.getCollection("items", ItemDTO.class);
        return Objects.requireNonNullElse(
                collection.find(Filters.and(
                        Filters.eq("is_in_a_storage", false),
                        Filters.eq("last_owner_uuid", player.getUniqueId().toString()),
                        Filters.eq("hash", InventorySync.hashItemStack(itemStack)),
                        Filters.eq("slot", slot))
                ).first(), new ItemDTO(player, itemStack, slot));
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
