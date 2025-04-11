package org.momento.echidna.player;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.momento.echidna.Echidna;
import org.momento.echidna.network.BlockDTO;

public class BlockLogging implements Listener {

    private void addToDatabase(Player player, Block block, boolean broken) {
        if (player.isOp()) return;
        //TODO Possibly check a specific permission instead
        BlockDTO dto = new BlockDTO(player, block, broken);
        Echidna.blocksDTOS.add(dto);
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
