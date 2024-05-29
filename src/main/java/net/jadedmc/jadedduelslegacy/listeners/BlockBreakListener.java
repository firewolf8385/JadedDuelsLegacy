package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.GameState;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class BlockBreakListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public BlockBreakListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(BlockBreakEvent event) {
        if(event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();

        if(player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        Game game = plugin.gameManager().game(player);

        if(game == null) {
            event.setCancelled(true);
            return;
        }

        // If the game isn't running, cancel the event.
        if(game.gameState() == GameState.COUNTDOWN || game.gameState() == GameState.END) {
            event.setCancelled(true);
            return;
        }

        // Prevent spectators from placing/breaking blocks.
        if(game.spectators().contains(player)) {
            event.setCancelled(true);
            return;
        }

        if(!game.kit().build() && !game.blocks().contains(event.getBlock()) && !game.kit().breakableBlocks().contains(event.getBlock().getType())) {
            event.setCancelled(true);
            return;
        }

        // Use kit-specific BlockBreakEvent code.
        game.kit().onBlockBreak(game, event);

        // Get the drops from the block and add them to the inventory.
        Collection<ItemStack> drops = event.getBlock().getDrops(player.getInventory().getItemInHand());

        if(game.kit().dropItems()) {
            drops.forEach(drop -> player.getInventory().addItem(drop));
        }
    }
}