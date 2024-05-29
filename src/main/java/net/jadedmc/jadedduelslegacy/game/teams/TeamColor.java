package net.jadedmc.jadedduelslegacy.game.teams;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;

/**
 * Represents a color option for a team.
 */
public enum TeamColor {
    DARK_RED(ChatColor.DARK_RED, Color.MAROON, DyeColor.RED, "Red", "R"),
    RED(ChatColor.RED, Color.RED, DyeColor.RED, "Red", "R"),
    ORANGE(ChatColor.GOLD, Color.ORANGE, DyeColor.ORANGE, "Orange", "O"),
    YELLOW(ChatColor.YELLOW, Color.YELLOW, DyeColor.YELLOW, "Yellow", "Y"),
    GREEN(ChatColor.GREEN, Color.LIME, DyeColor.LIME, "Lime", "L"),
    DARK_GREEN(ChatColor.DARK_GREEN, Color.GREEN, DyeColor.GREEN, "Green", "G"),
    AQUA(ChatColor.AQUA, Color.AQUA, DyeColor.LIGHT_BLUE, "Aqua", "A"),
    BLUE(ChatColor.BLUE, Color.BLUE, DyeColor.BLUE, "Blue", "B"),
    DARK_BLUE(ChatColor.DARK_BLUE, Color.NAVY, DyeColor.BLUE, "Blue", "B"),
    PURPLE(ChatColor.DARK_PURPLE, Color.PURPLE, DyeColor.PURPLE, "Purple", "P"),
    PINK(ChatColor.LIGHT_PURPLE, Color.FUCHSIA, DyeColor.PINK, "Pink", "P"),
    BLACK(ChatColor.BLACK, Color.BLACK, DyeColor.BLACK, "Black", "B"),
    WHITE(ChatColor.WHITE, Color.WHITE, DyeColor.WHITE, "White", "W"),
    GRAY(ChatColor.GRAY, Color.SILVER, DyeColor.SILVER, "Gray", "G"),
    DARK_GRAY(ChatColor.DARK_GRAY, Color.GRAY, DyeColor.GRAY, "Gray", "G");

    private final ChatColor chatColor;
    private final Color leatherColor;
    private final DyeColor woolColor;
    private final String displayName;
    private final String abbreviation;

    TeamColor(ChatColor chatColor, Color leatherColor, DyeColor woolColor, String displayName, String abbreviation) {
        this.chatColor = chatColor;
        this.leatherColor = leatherColor;
        this.woolColor = woolColor;
        this.displayName = displayName;
        this.abbreviation = abbreviation;
    }

    /**
     * Get the team color's abbreviation.
     * @return Team abbreviation.
     */
    public String abbreviation() {
        return abbreviation;
    }

    /**
     * Gets the chat color of a team.
     * @return Chat color of the team.
     */
    public ChatColor chatColor() {
        return chatColor;
    }

    /**
     * Get the team color's display name.
     * @return Team's displayName.
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Gets the leather color of a team.
     * @return Leather color of the team.
     */
    public Color leatherColor() {
        return leatherColor;
    }

    /**
     * Gets the Wool color of the team.
     * @return Color of the wool the team uses.
     */
    public DyeColor woolColor() {
        return woolColor;
    }
}