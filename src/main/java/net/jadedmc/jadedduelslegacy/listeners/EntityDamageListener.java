package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public EntityDamageListener(final JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Cancel damage from specific causes.
        switch (event.getCause()) {
            case LIGHTNING:
            case FALL:
                event.setCancelled(true);
                return;
        }

        Game game = plugin.gameManager().game(player);

        if(game == null) {
            return;
        }

        // Prevent spectators from taking damage.
        if(game.spectators().contains(player)) {
            event.setCancelled(true);
            return;
        }

        // Players can only take damage when the game is running.
        if(game.gameState() != GameState.RUNNING) {
            event.setCancelled(true);
            return;
        }

        // Prevents "killing" a player twice.
        if(event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            return;
        }

        // Kill player if they won't survive.
        if(event.getFinalDamage() >= player.getHealth()) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTaskLater(plugin, () -> game.playerKilled(player), 1);
        }
    }
}