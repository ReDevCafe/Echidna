package org.momento.echidna.network;

import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class PlayerBlockDTO implements MongoDTO {

    private final UUID playerUUID;
    private final UUID worldUUID;
    private final double blockX;
    private final double blockY;
    private final double blockZ;
    private final Material material;
    private final boolean broken;
    private final Date date;

    public PlayerBlockDTO(Player player, Block block, boolean broken) {
        this.playerUUID = player.getUniqueId();
        this.blockX = block.getX();
        this.blockY = block.getY();
        this.blockZ = block.getZ();
        this.material = block.getType();
        this.broken = broken;
        this.date = new Date();
        this.worldUUID = block.getWorld().getUID();
    }

    public Document toDocument() {
        Document doc = new Document("playerUUID", playerUUID);
        doc.append("blockX", blockX);
        doc.append("blockY", blockY);
        doc.append("blockZ", blockZ);
        doc.append("worldUUID", worldUUID);
        doc.append("material", material);
        doc.append("broken", broken);
        doc.append("date", date);
        return doc;
    }

}
