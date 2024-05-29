package net.jadedmc.jadedduelslegacy.utils;

import net.jadedmc.jadedutils.MathUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GameUtils {

    /**
     * Get the health of a player, formatted, and in percent form.
     * @param player Player to get health of.
     * @return Formatted health of player.
     */
    public static String getFormattedHealth(Player player) {
        int percent = MathUtils.percent(player.getHealth(), player.getMaxHealth());
        ChatColor color;

        if(percent < 25) {
            color = ChatColor.RED;
        }
        else if(percent < 50) {
            color = ChatColor.GOLD;
        }
        else if(percent < 75) {
            color = ChatColor.YELLOW;
        }
        else {
            color = ChatColor.GREEN;
        }

        return "" + color + percent + "%";
    }

    /**
     * Get a colored string of a player's ping.
     * @param player Player to get ping of.
     * @return Formatted ping.
     */
    public static String getFormattedPing(Player player) {
        //int ping = player.getPing();
        int ping = 0;
        ChatColor color;

        if(ping < 40) {
            color = ChatColor.GREEN;
        }
        else if(ping < 70) {
            color = ChatColor.YELLOW;
        }
        else if (ping < 110) {
            color = ChatColor.GOLD;
        }
        else {
            color = ChatColor.RED;
        }
        return color + "" + ping + " ms";
    }
}