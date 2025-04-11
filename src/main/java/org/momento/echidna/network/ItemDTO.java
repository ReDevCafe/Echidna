package org.momento.echidna.network;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.momento.echidna.player.InventorySync;

import java.util.Date;
import java.util.UUID;

public class ItemDTO implements Cloneable, DTO {

    @BsonId
    private ObjectId id;
    @BsonProperty("last_owner_uuid")
    private UUID lastOwnerUUID;
    @BsonProperty("material")
    private Material material;
    @BsonProperty("quantity")
    private int quantity;
    @BsonProperty("date")
    private Date date;
    @BsonProperty("hash")
    private String hash;
    @BsonProperty("previous_id")
    private ObjectId previousId;
    @BsonProperty("is_in_a_storage")
    private boolean isInAStorage;
    @BsonProperty("slot")
    private int slot;
    @BsonProperty("x")
    private int x;
    @BsonProperty("y")
    private int y;
    @BsonProperty("z")
    private int z;
    @BsonProperty("word_uuid")
    private UUID wordUUID;

    public ItemDTO(ItemStack item, Location location, int slot) {
        this.material = item.getType();
        this.quantity = item.getAmount();
        this.date = new Date();
        this.hash = InventorySync.hashItemStack(item);
        this.previousId = null;
        this.slot = slot;
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.isInAStorage = true;
        this.id = new ObjectId();
    }

    public ItemDTO(Player player, ItemStack item, int slot) {
        this.material = item.getType();
        this.quantity = item.getAmount();
        this.date = new Date();
        this.hash = InventorySync.hashItemStack(item);
        this.previousId = null;
        this.lastOwnerUUID = player.getUniqueId();
        this.slot = slot;
        this.x = -1;
        this.y = -1;
        this.z = -1;
        this.isInAStorage = false;
        this.id = new ObjectId();
    }

    public ObjectId getPreviousHash() { return previousId; }
    public UUID getLastOwnerUUID() { return lastOwnerUUID; }
    public Material getMaterial() { return material; }
    public int getQuantity() { return quantity; }
    public Date getDate() { return date; }
    public ObjectId getId() { return id; }
    public String getHash() { return hash; }
    public int getSlot() { return slot; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getZ() { return z; }
    public Location getLocation() { return new Location(Bukkit.getWorld(wordUUID), x, y, z); }
    public boolean isInAStorage() { return isInAStorage; }

    public void setLastOwnerUUID(UUID lastOwnerUUID) { this.lastOwnerUUID = lastOwnerUUID; }
    public void setPreviousId(ObjectId previousId) { this.previousId = previousId; }
    public void setHash(String hash) { this.hash = hash; }
    public void setDate(Date date) { this.date = date; }
    public void setMaterial(Material material) { this.material = material; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setId(ObjectId id) { this.id = id; }
    public void setSlot(int slot) { this.slot = slot; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public void setZ(int z) { this.z = z; }
    public void setIsInAStorage(boolean isInAStorage) { this.isInAStorage = isInAStorage; }

    public void setLocation(Location location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.isInAStorage = true;
        this.wordUUID = location.getWorld().getUID();
    }

    public void resetLocation() {
        this.x = -1;
        this.y = -1;
        this.z = -1;
        this.isInAStorage = false;
        this.wordUUID = null;
    }

    @Override
    public ItemDTO clone() {
        try {
            return (ItemDTO) super.clone();
        } catch (CloneNotSupportedException error) {
            throw new AssertionError(error.getMessage());
        }
    }
}
