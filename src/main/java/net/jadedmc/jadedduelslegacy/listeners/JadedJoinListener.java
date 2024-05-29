package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedcore.JadedAPI;
import net.jadedmc.jadedcore.events.JadedJoinEvent;
import net.jadedmc.jadedcore.minigames.Minigame;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedutils.chat.ChatUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JadedJoinListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public JadedJoinListener(final JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJadedJoin(JadedJoinEvent event) {
        // TODO: Load DuelPlayer object.
        Player player = event.getJadedPlayer().getPlayer();

        for(Game game : plugin.gameManager().games()) {
            if(!game.players().contains(player)) {
                continue;
            }

            game.addPlayer(player);
            return;
        }

        if(!JadedAPI.getPlugin().lobbyManager().isLobbyWorld(player.getWorld())) {
            event.getJadedPlayer().sendMessage("<red>Game not found! Seind you back to the lobby.");
            JadedAPI.sendToLobby(player, Minigame.DUELS_LEGACY);
        }
    }
}