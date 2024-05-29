package net.jadedmc.jadedduelslegacy.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEggThrowEvent;

/**
 * Listens to when a player throws an egg.
 * Used to prevent eggs from hatching.
 */
public class PlayerEggThrowListener implements Listener {

    /**
     * Runs when an egg is thrown.
     * @param event Player Egg Throw Event.
     */
    @EventHandler
    public void onEggThrow(PlayerEggThrowEvent event) {
        // Prevent eggs from hatching.
        event.setHatching(false);
    }
}