package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.GameState;
import net.jadedmc.jadedduelslegacy.gui.KitGUI;
import net.jadedmc.jadedutils.chat.ChatUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerInteractListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public PlayerInteractListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        Game game = plugin.gameManager().game(player);

        if(game != null) {
            if(game.gameState() != GameState.RUNNING) {
                event.setCancelled(true);
                // Fixes visual glitch with throwables during countdown.
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), player.getItemInHand());
                return;
            }
            else {
                game.kit().onPlayerInteract(game, event);
            }
        }

        // Prevent using items during game countdown.
        if(game != null && game.gameState() != GameState.RUNNING) {
            event.setCancelled(true);
        }

        // Exit if the item is null.
        if(event.getItem() == null) {
            return;
        }

        // Exit if item meta is null.
        if(event.getItem().getItemMeta() == null) {
            return;
        }

        String item = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());

        if(item == null) {
            return;
        }

        if(game != null) {
            game.kit().onNamedItemClick(game, player, item);
        }

        switch (item) {
            case "Kits" -> {
                if(plugin.queueManager().getKit(player) != null) {
                    ChatUtils.chat(player, "<red>You must leave your current queue first!");
                    event.setCancelled(true);
                    return;
                }

                new KitGUI(plugin).open(player);
                event.setCancelled(true);
            }

            case "Settings" -> {
                ChatUtils.chat(player, "&cComing soon.");
                event.setCancelled(true);
            }

            case "Leave Queue" -> {
                plugin.queueManager().leaveQueue(player);
                event.setCancelled(true);
            }

            default -> {
                if(game != null && game.spectators().contains(player)) {
                    event.setCancelled(true);
                }
            }
        }

    }
}