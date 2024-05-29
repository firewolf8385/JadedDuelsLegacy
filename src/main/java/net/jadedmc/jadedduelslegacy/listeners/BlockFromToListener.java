package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class BlockFromToListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public BlockFromToListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(BlockFromToEvent event) {
        Game game = plugin.gameManager().game(event.getBlock().getWorld());

        if(game == null) {
            return;
        }

        game.addBlock(event.getBlock(), Material.AIR);
        game.addBlock(event.getToBlock(), Material.AIR);
    }
}