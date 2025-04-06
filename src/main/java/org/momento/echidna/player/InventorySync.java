package org.momento.echidna.player;

import com.google.common.primitives.Bytes;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.momento.echidna.Echidna;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;

public class InventorySync implements Listener {

    private final UUID playerUUID;

    public InventorySync(final Player player) {
        this.playerUUID = player.getUniqueId();
        Echidna.plugin.getServer().getPluginManager().registerEvents(this, Echidna.plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (!player.getUniqueId().equals(this.playerUUID)) return;

    }

    public static String hashItemStack(Date date, ItemStack itemStack) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException error) {
            Echidna.logger.log(Level.WARNING, "Can't hash this itemstack " + itemStack.toString());
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder(itemStack.getType().toString());
        stringBuilder.append(itemStack.getAmount());
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
                    stringBuilder.append(enchantment.toString());
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
