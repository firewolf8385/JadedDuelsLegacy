package net.jadedmc.jadedduelslegacy.game.tournament;

/**
 * Represents the size of a team, and how the team is picked.
 */
public enum TeamType {
    ONE_V_ONE("1v1", 2),
    TWO_V_TWO_RANDOM("2v2 Random", 3),
    THREE_V_THREE_RANDOM("3v3 Random", 4),
    NONE("None", 0);

    private final String displayName;
    private final int minimumPlayers;

    /**
     * Creates the team size.
     * @param displayName The name of the team size.
     * @param minimumPlayers Minimum number of players.
     */
    TeamType(String displayName, int minimumPlayers) {
        this.displayName = displayName;
        this.minimumPlayers = minimumPlayers;
    }

    /**
     * Gets the display name of the team.
     * @return Team display name.
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Gets the required players to start a tournament.
     * @return Minimum number of players.
     */
    public int minimumPlayers() {
        return minimumPlayers;
    }
}