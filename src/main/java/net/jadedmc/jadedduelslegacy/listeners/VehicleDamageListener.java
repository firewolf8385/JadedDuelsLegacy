package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleDamageEvent;

public class VehicleDamageListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public VehicleDamageListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        if(!(event.getAttacker() instanceof Player player)) {
            return;
        }

        Game game = plugin.gameManager().game(player);

        if(game != null) {
            return;
        }

        if(player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        event.setCancelled(true);
    }
}