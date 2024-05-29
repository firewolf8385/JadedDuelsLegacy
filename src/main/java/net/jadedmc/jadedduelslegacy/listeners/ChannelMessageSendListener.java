package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedchat.features.channels.events.ChannelMessageSendEvent;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.teams.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ChannelMessageSendListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public ChannelMessageSendListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMessage(ChannelMessageSendEvent event) {

        switch (event.getChannel().name().toUpperCase()) {
            case "GAME" -> gameChannel(event);
            case "TEAM" -> teamChannel(event);
        }
    }

    private void gameChannel(ChannelMessageSendEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.gameManager().game(player);

        if(game == null) {
            event.setCancelled(true);
            return;
        }

        List<Player> viewers = new ArrayList<>(game.players());
        viewers.addAll(game.spectators());

        // Remove duplicates.
        Set<Player> set = new LinkedHashSet<>(viewers);
        viewers.clear();
        viewers.addAll(set);

        event.setViewers(viewers);
    }

    private void teamChannel(ChannelMessageSendEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.gameManager().game(player);

        if(game == null) {
            event.setCancelled(true);
            return;
        }

        Team team = game.teamManager().team(player);
        if(team == null) {
            event.setCancelled(true);
            return;
        }

        List<Player> viewers = new ArrayList<>(team.players());

        event.setViewers(viewers);
    }
}