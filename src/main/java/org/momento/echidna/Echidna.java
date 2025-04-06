package org.momento.echidna;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.momento.echidna.network.PlayerBlockDTO;
import org.momento.echidna.network.PlayerItemDTO;
import org.momento.echidna.player.InventorySync;
import org.momento.echidna.services.MongoDBService;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

public final class Echidna extends JavaPlugin {

    public static ConfigurationSection databaseSection;

    // Avoid data race ig
    public static ConcurrentLinkedQueue<PlayerBlockDTO> playerBlocksDTOS = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<PlayerItemDTO> playerItemsDTOS = new ConcurrentLinkedQueue<>();

    public static Plugin plugin;
    public static Logger logger;

    //TODO See if it's useful to run this in second thread if mongodb flow stream do the same
    public void sendBlocks() {
        MongoDBService.sendManyData("blocks", playerBlocksDTOS);
        playerBlocksDTOS.clear();
    }

    public void sendItems() {
        MongoDBService.sendManyData("items", playerItemsDTOS);
        playerItemsDTOS.clear();
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
                new PlayerBlockLogging(),
                this
        );
        MongoDBService.connect();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::sendBlocks, 0, 200);
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::sendItems, 0, 200);
    }

    @Override
    public void onDisable() {
        if (MongoDBService.isConnected())
            MongoDBService.disconnect();
    }
}
