package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class EntityRegainHealthListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public EntityRegainHealthListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if(!(event.getEntity() instanceof Player player)) {
            return;
        }

        Game game = plugin.gameManager().game(player);

        if(game == null) {
            return;
        }

        if(!game.kit().naturalRegeneration() && event.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            event.setCancelled(true);
        }
    }
}
