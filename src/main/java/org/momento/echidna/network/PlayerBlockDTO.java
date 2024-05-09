package org.momento.echidna.network;

import org.bson.Document;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class PlayerBlockDTO implements MongoDTO {

    private final UUID playerUUID;
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
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(playerUUID);
        sb.append(" => (");
        sb.append(blockX);
        sb.append(", ");
        sb.append(blockY);
        sb.append(", ");
        sb.append(blockZ);
        sb.append("): ");
        sb.append(material);
        if (broken)
            sb.append(" broken at ");
        else
            sb.append(" placed at ");
        sb.append(date);
        return sb.toString();
    }

    public Document toDocument() {
        Document doc = new Document("playerUUID", playerUUID);
        doc.append("blockX", blockX);
        doc.append("blockY", blockY);
        doc.append("blockZ", blockZ);
        doc.append("material", material);
        doc.append("broken", broken);
        doc.append("date", date);
        return doc;
    }

}
