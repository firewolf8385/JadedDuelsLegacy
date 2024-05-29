package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.GameState;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockPlaceListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public BlockPlaceListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.gameManager().game(player);

        // Exit if the game is null.
        if(game == null) {
            return;
        }

        // If the game isn't running, cancel the event.
        if(game.gameState() == GameState.COUNTDOWN || game.gameState() == GameState.END) {
            event.setCancelled(true);
            return;
        }

        // Run kit-specific BlockPlaceEvent code.
        game.kit().onBlockPlace(game, event);


        game.addBlock(event.getBlock(), Material.AIR);
    }
}