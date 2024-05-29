package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;

public class BlockFormListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public BlockFormListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onForm(BlockFormEvent event) {
        Game game = plugin.gameManager().game(event.getBlock().getWorld());

        if(game == null) {
            return;
        }

        // Doesn't work with 1.8. So we'll need another solution eventually.
        game.addBlock(event.getBlock(), event.getBlock().getType());
    }
}