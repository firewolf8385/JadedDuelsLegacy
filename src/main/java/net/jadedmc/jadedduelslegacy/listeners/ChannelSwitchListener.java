package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedchat.JadedChat;
import net.jadedmc.jadedchat.features.channels.events.ChannelSwitchEvent;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChannelSwitchListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public ChannelSwitchListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSwitch(ChannelSwitchEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.gameManager().game(player);

        if(game == null) {
            if(event.getToChannel().name().equalsIgnoreCase("GAME") || event.getToChannel().name().equalsIgnoreCase("TEAM")) {
                event.setToChannel(JadedChat.getDefaultChannel());
            }
        }
        else {
            if(event.getToChannel().equals(JadedChat.getDefaultChannel())) {
                event.setToChannel(JadedChat.getChannel("GAME"));
            }
        }
    }
}