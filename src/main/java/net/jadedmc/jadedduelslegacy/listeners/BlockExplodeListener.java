package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public class BlockExplodeListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public BlockExplodeListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Game game = plugin.gameManager().game(event.getBlock().getWorld());

        if(game == null) {
            return;
        }

        for(Block block : event.blockList()) {
            game.addBlock(block, block.getType());
        }
    }
}