package org.momento.echidna.network;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Date;
import java.util.UUID;

public class BlockDTO implements DTO {

    @BsonId
    private ObjectId id;
    @BsonProperty("player_uuid")
    private UUID playerUUID;
    @BsonProperty("world_uuid")
    private UUID worldUUID;
    @BsonProperty("x")
    private double x;
    @BsonProperty("y")
    private double y;
    @BsonProperty("z")
    private double z;
    @BsonProperty("material")
    private Material material;
    @BsonProperty("broken")
    private boolean broken;
    @BsonProperty("date")
    private Date date;

    public BlockDTO(Player player, Block block, boolean broken) {
        this.playerUUID = player.getUniqueId();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.material = block.getType();
        this.broken = broken;
        this.date = new Date();
        this.worldUUID = block.getWorld().getUID();
    }

    public ObjectId getId() { return id; }
    public UUID getPlayerUUID() { return playerUUID; }
    public UUID getWorldUUID() { return worldUUID; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public Material getMaterial() { return material; }
    public boolean isBroken() { return broken; }
    public Date getDate() { return date; }

    public void setId(ObjectId id) { this.id = id; }
    public void setPlayerUUID(UUID playerUUID) { this.playerUUID = playerUUID; }
    public void setWorldUUID(UUID worldUUID) { this.worldUUID = worldUUID; }
    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setZ(double z) { this.z = z; }
    public void setMaterial(Material material) { this.material = material; }
    public void setBroken(boolean broken) { this.broken = broken; }
    public void setDate(Date date) { this.date = date; }
}
