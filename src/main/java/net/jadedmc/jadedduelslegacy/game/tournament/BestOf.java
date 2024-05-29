package net.jadedmc.jadedduelslegacy.game.tournament;

public enum BestOf {
    NONE(0, "BoO"),
    ONE(1, "Bo1"),
    THREE(2, "Bo3"),
    FIVE(3, "Bo5"),
    SEVEN(4, "Bo7");

    private final int neededWins;
    private final String name;
    BestOf(int neededWins, String toString) {
        this.neededWins = neededWins;
        this.name = toString;
    }

    public int neededWins() {
        return neededWins;
    }

    public String getName() {
        return name;
    }
}