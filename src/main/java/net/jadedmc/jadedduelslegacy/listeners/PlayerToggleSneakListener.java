package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.GameState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class PlayerToggleSneakListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public PlayerToggleSneakListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(PlayerToggleSneakEvent event) {
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

        game.kit().onPlayerToggleSneak(game, event);
    }
}