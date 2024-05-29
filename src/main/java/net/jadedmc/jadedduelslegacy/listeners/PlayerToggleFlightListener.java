package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;

public class PlayerToggleFlightListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public PlayerToggleFlightListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(PlayerToggleFlightEvent event) {
        Game game = plugin.gameManager().game(event.getPlayer());

        if(game == null) {
            return;
        }

        if(game.gameState() != GameState.RUNNING) {
            return;
        }

        if(game.spectators().contains(event.getPlayer())) {
            return;
        }

        game.kit().onPlayerToggleFlight(game, event);
    }
}