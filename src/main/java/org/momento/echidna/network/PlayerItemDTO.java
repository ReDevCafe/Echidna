package org.momento.echidna.network;

import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.UUID;

public class PlayerItemDTO implements MongoDTO {

    private final UUID playerUUID;
    private final Material material;
    private final Date date;

    public PlayerItemDTO(Player player, ItemStack item) {
        this.playerUUID = player.getUniqueId();
        this.material = item.getType();
        this.date = new Date();
    }

    public Document toDocument() {
        Document doc = new Document("playerUUID", playerUUID);
        doc.append("material", material);
        doc.append("date", date);
        return doc;
    }
}
