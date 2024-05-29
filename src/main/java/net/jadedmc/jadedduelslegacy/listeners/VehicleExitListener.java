package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class VehicleExitListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public VehicleExitListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onExit(VehicleExitEvent event) {

        if(!(event.getExited() instanceof Player player)) {
            return;
        }

        Game game = plugin.gameManager().game(player);

        if(game == null) {
            return;
        }

        if(!event.getExited().isValid()) {
            return;
        }

        if(game.spectators().contains(player)) {
            return;
        }

        if(!game.kit().exitVehicle()) {
            event.setCancelled(true);
        }
    }
}