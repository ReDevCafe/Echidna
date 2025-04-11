package org.momento.echidna.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.momento.echidna.network.ItemDTO;
import org.momento.echidna.services.MongoDBService;

import java.util.ArrayList;
import java.util.List;

public class ItemLogging implements Listener {


    public static List<InventorySync> inventorySyncs = new ArrayList<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        inventorySyncs.add(new InventorySync(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InventorySync inventorySync = inventorySyncs.stream()
                .filter(is -> is.getPlayerUUID().equals(player.getUniqueId()))
                .findFirst().orElse(null);
        if (inventorySync == null) return;
        ItemDTO pickup = inventorySync.getPickup();
        if (pickup == null) return;
        MongoDBService.sendData("items", pickup);
        inventorySyncs.remove(inventorySync);
    }

}
