package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedcore.events.LobbyJoinEvent;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedutils.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class LobbyJoinListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public LobbyJoinListener(final JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLobbyJoin(LobbyJoinEvent event) {
        Player player = event.getPlayer();

        player.getInventory().setItem(4, new ItemBuilder(Material.NETHER_STAR).setDisplayName("<green><bold>Kits").build());
        player.getInventory().setItem(5, new ItemBuilder(Material.EYE_OF_ENDER).setDisplayName("<green><bold>Spectate").build());
    }
}