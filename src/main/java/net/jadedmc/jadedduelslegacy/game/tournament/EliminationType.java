package net.jadedmc.jadedduelslegacy.game.tournament;

/**
 * Represents the type of event being run.
 */
public enum EliminationType {
    /**
     * A tournament with only 1 loss.
     */
    SINGLE_ELIMINATION("Single Elim"),

    /**
     * A tournament with 2 losses.
     */
    DOUBLE_ELIMINATION("Double Elim"),

    /**
     * No selection currently made.
     */
    NONE("None");

    private final String name;

    EliminationType(String toString) {
        this.name = toString;
    }

    public String getName() {
        return name;
    }
}