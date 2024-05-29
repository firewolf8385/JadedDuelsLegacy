package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodLevelChangeListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public FoodLevelChangeListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(FoodLevelChangeEvent event) {
        // Makes sure we are dealing with a player.
        if(!(event.getEntity() instanceof Player player)) {
            return;
        }

        Game game = plugin.gameManager().game(player);

        // Hunger won't lower outside of games
        if(game == null || !game.kit().hunger()) {
            event.setCancelled(true);
            return;
        }

        // Prevent spectators from having hunger.
        if(game.spectators().contains(player)) {
            event.setCancelled(true);
            return;
        }
    }
}