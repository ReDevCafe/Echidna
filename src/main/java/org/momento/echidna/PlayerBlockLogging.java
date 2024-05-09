package org.momento.echidna;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.momento.echidna.network.PlayerBlockDTO;

public class PlayerBlockLogging implements Listener {

    private void addToDatabase(Player player, Block block, boolean broken) {
        if (player.isOp()) return;
        //TODO Possibly check a specific permission instead
        PlayerBlockDTO dto = new PlayerBlockDTO(player, block, broken);
        Echidna.playerBlocksDTOS.add(dto);
    }

    @EventHandler
    public void onPlayerBlockBreak(BlockBreakEvent event) {
        addToDatabase(event.getPlayer(), event.getBlock(), true);
    }

    @EventHandler
    public void onPlayerPlaceBreak(BlockPlaceEvent event) {
        addToDatabase(event.getPlayer(), event.getBlock(), false);
    }
}
