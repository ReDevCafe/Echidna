package org.momento.echidna.player;

import org.bson.types.ObjectId;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.momento.echidna.Echidna;
import org.momento.echidna.network.ItemDTO;
import org.momento.echidna.services.MongoDBService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class InventorySync implements Listener {

    private final UUID playerUUID;
    private final ItemDTO[] inventory;
    private ItemDTO pickup;

    private static Map<String, ItemStack> drops = new HashMap<>();


    public InventorySync(final Player player) {
        ItemStack[] playerInventory = player.getInventory().getContents();
        this.playerUUID = player.getUniqueId();
        this.inventory = new ItemDTO[playerInventory.length];
        this.pickup = null;
        Echidna.plugin.getServer().getPluginManager().registerEvents(this, Echidna.plugin);
        for (int i = 0; i < playerInventory.length; i++) {
            if (playerInventory[i] != null && !playerInventory[i].getType().equals(Material.AIR))
                this.inventory[i] = MongoDBService.getPlayerItem(player, playerInventory[i], i);
            else
                this.inventory[i] = null;
        }
    }

    public ItemDTO getPickup() { return pickup; }
    public UUID getPlayerUUID() { return playerUUID; }

    public ItemDTO getItemDTO(final ItemStack itemStack) {
        String hash = hashItemStack(itemStack);
        for (ItemDTO itemDTO : this.inventory) {
            if (itemDTO.getHash().equals(hash))
                return itemDTO;
        }
        return null; //TODO maybe create a new ItemDTO
    }

    public void removeItemDTO(final ItemDTO itemDTO) {
        if (itemDTO == null) return;
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null && inventory[i].getId().equals(itemDTO.getId())) {
                inventory[i] = null;
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!player.getUniqueId().equals(this.playerUUID)) return;
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        InventoryAction action = event.getAction();
        Inventory inventory = event.getInventory();
        int slot = event.getSlot();
        Location location = event.getInventory().getLocation();
        switch (action) {
            case PICKUP_ALL -> {
                if (inventory.getType().equals(InventoryType.PLAYER)) {
                    pickup = getItemDTO(item);
                    pickup.setPreviousId(pickup.getId());
                    pickup.setId(new ObjectId());
                } else if (location != null)
                    pickup = MongoDBService.getStorageItem(item, slot, location);
                pickup.setLastOwnerUUID(player.getUniqueId());
            }
            case PICKUP_HALF, PICKUP_ONE -> {
                ItemDTO itemDTO;
                if (inventory.getType().equals(InventoryType.PLAYER)) {
                    itemDTO = getItemDTO(item);
                    pickup = itemDTO.clone();
                } else if (location != null) {
                    pickup = MongoDBService.getStorageItem(item, slot, location);
                    itemDTO = pickup.clone();
                } else {
                    Echidna.logger.log(Level.WARNING, "Trying to synchronise a item in a virtual inventory");
                    return;
                }
                pickup.setLastOwnerUUID(player.getUniqueId());
                pickup.setPreviousId(pickup.getId());
                pickup.setId(new ObjectId());
                pickup.setDate(new Date());
                pickup.setQuantity(item.getAmount());
                itemDTO.setQuantity(event.getCursor().getAmount());
                itemDTO.setPreviousId(itemDTO.getId());
                itemDTO.setId(new ObjectId());
                Echidna.itemsDTOS.add(itemDTO);
            }
            case PLACE_ALL -> {
                if (inventory.getType().equals(InventoryType.PLAYER))
                    pickup.resetLocation();
                else if (location != null)
                    pickup.setLocation(location);
                else
                    Echidna.logger.log(Level.WARNING, "Trying to synchronise a item in a virtual inventory");
                Echidna.itemsDTOS.add(pickup);
                pickup = null;
            }
            case PLACE_SOME, PLACE_ONE -> {
                ItemDTO itemDTO;
                if (inventory.getType().equals(InventoryType.PLAYER))
                    pickup.resetLocation();
                else if (location != null)
                    pickup.setLocation(location);
                else
                    Echidna.logger.log(Level.WARNING, "Trying to synchronise a item in a virtual inventory");

                Echidna.itemsDTOS.add(pickup);

                pickup = null;
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!player.getUniqueId().equals(this.playerUUID)) return;
        ItemStack item = event.getItemDrop().getItemStack();
        ItemDTO dto = getItemDTO(item);
        if (dto == null) return;
        removeItemDTO(dto);
        drops.put(dto.getHash(), item);
    }

    public static String hashItemStack(ItemStack itemStack) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException error) {
            Echidna.logger.log(Level.WARNING, "Can't hash this itemstack " + itemStack.toString());
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(itemStack.getType().toString());
        //stringBuilder.append(itemStack.getAmount());
        if (itemStack.getData() != null)
            stringBuilder.append(itemStack.getData().toString());
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName())
                stringBuilder.append(meta.getDisplayName());
            if (meta.hasLore())
                meta.getLore().forEach(stringBuilder::append);
            if (meta.hasEnchants()) {
                meta.getEnchants().forEach((enchantment, level) -> {
                    stringBuilder.append(enchantment.getKey());
                    stringBuilder.append(level);
                });
            }
            if (meta.hasCustomModelData())
                stringBuilder.append(meta.getCustomModelData());
            if (meta.hasAttributeModifiers()) {
                meta.getAttributeModifiers().forEach((attribute, modifier) -> {
                    stringBuilder.append(attribute.toString());
                    stringBuilder.append(modifier.toString());
                });
            }
            if (meta instanceof Damageable damageable)
                stringBuilder.append(damageable.getDamage());
        }
        byte[] hash = digest.digest(stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash)
            hexString.append(String.format("%02x", b));
        return hexString.toString();
    }

}
