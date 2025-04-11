package org.momento.echidna;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.momento.echidna.network.BlockDTO;
import org.momento.echidna.network.ItemDTO;
import org.momento.echidna.player.BlockLogging;
import org.momento.echidna.player.ItemLogging;
import org.momento.echidna.services.MongoDBService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public final class Echidna extends JavaPlugin {

    public static ConfigurationSection databaseSection;

    // Avoid data race ig
    public static List<BlockDTO> blocksDTOS = Collections.synchronizedList(new ArrayList<>());
    public static List<ItemDTO> itemsDTOS = Collections.synchronizedList(new ArrayList<>());

    public static Plugin plugin;
    public static Logger logger;

    //TODO See if it's useful to run this in second thread if mongodb flow stream do the same
    public void sendData() {
        MongoDBService.sendManyData("blocks", blocksDTOS);
        blocksDTOS.clear();
        MongoDBService.sendManyData("items", itemsDTOS);
        itemsDTOS.clear();
    }

    @Override
    public void onEnable() {
        plugin = this;
        logger = this.getLogger();
        saveDefaultConfig();
        databaseSection = Optional.ofNullable(getConfig().getConfigurationSection("database"))
                .orElseThrow(() -> new IllegalArgumentException("Database section is null"));
        if (databaseSection == null)
            throw new NullPointerException("Database section is null");
        this.getServer().getPluginManager().registerEvents(
                new BlockLogging(),
                this
        );
        this.getServer().getPluginManager().registerEvents(
                new ItemLogging(),
                this
        );
        MongoDBService.connect();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::sendData, 0, 200);
    }

    @Override
    public void onDisable() {
        ItemLogging.inventorySyncs.forEach(inventorySync -> {
            ItemDTO pickup = inventorySync.getPickup();
            pickup.resetLocation();
            MongoDBService.sendData("items", pickup);
        });
        if (MongoDBService.isConnected())
            MongoDBService.disconnect();
    }
}
