package org.momento.echidna;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import org.momento.echidna.network.PlayerBlockDTO;
import org.momento.echidna.services.MongoDBService;

import java.util.LinkedList;
import java.util.Optional;

public final class Echidna extends JavaPlugin {

    public static ConfigurationSection databaseSection;
    public static LinkedList<PlayerBlockDTO> playerBlocksDTOS = new LinkedList<>();

    //TODO See if it's useful to run this in second thread if mongodb flow stream do the same
    public void sendDTO() {
        MongoDBService.sendManyData("blocks", playerBlocksDTOS);
        playerBlocksDTOS.clear();
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        databaseSection = Optional.ofNullable(getConfig().getConfigurationSection("database"))
                .orElseThrow(() -> new IllegalArgumentException("Database section is null"));
        if (databaseSection == null)
            throw new NullPointerException("Database section is null");
        this.getServer().getPluginManager().registerEvents(
                new PlayerBlockLogging(),
                this);
        MongoDBService.connect();
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, this::sendDTO, 0, 200);
    }

    @Override
    public void onDisable() {
        if (MongoDBService.isConnected())
            MongoDBService.disconnect();
    }
}
