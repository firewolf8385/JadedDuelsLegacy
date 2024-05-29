package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public ProjectileLaunchListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        if(!(event.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        Game game = plugin.gameManager().game(player);

        if(game == null) {
            return;
        }

        if(game.spectators().contains(player)) {
            return;
        }

        game.kit().onProjectileLaunch(player, game, event);
    }
}