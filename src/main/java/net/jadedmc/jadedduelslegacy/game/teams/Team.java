package net.jadedmc.jadedduelslegacy.game.teams;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Represents a group of players working together in a Game.
 */
public class Team {
    private final List<UUID> players = new ArrayList<>();
    private final Set<UUID> alivePlayers = new HashSet<>();
    private final Set<UUID> deadPlayers = new HashSet<>();
    private int score = 0;
    private final TeamColor teamColor;
    private final int id;
    private final long challongeID;

    public Team(List<String> uuids, TeamColor teamColor, int id) {
        this(uuids, teamColor, id, 0);
    }

    /**
     * Creates a team.
     * @param uuids UUIDs of all team members.
     * @param teamColor Team color being used.
     */
    public Team(List<String> uuids, TeamColor teamColor, int id, long challongeID) {
        // Load players.
        uuids.forEach(uuid -> players.add(UUID.fromString(uuid)));
        alivePlayers.addAll(players);

        // Cache team color.
        this.teamColor = teamColor;

        this.id = id;
        this.challongeID = challongeID;
    }

    /**
     * Adds a point to the team.
     */
    public void addPoint() {
        score++;
    }

    /**
     * Get all alive players on the team.
     * @return Set of alive players.
     */
    public Set<Player> alivePlayers() {
        Set<Player> onlinePlayers = new HashSet<>();

        // Loop through all stored team member uuids.
        for(UUID uuid : alivePlayers) {
            Player player = Bukkit.getPlayer(uuid);

            // Makes sure the player is valid.
            if(player == null || !player.isOnline()) {
                continue;
            }

            onlinePlayers.add(player);
        }

        // Return our created list.
        return onlinePlayers;
    }

    /**
     * Get all dead players on the team.
     * @return Set of dead players.
     */
    public Set<Player> deadPlayers() {
        Set<Player> onlinePlayers = new HashSet<>();

        // Loop through all stored team member uuids.
        for(UUID uuid : deadPlayers) {
            Player player = Bukkit.getPlayer(uuid);

            // Makes sure the player is valid.
            if(player == null || !player.isOnline()) {
                continue;
            }

            onlinePlayers.add(player);
        }

        // Return our created list.
        return onlinePlayers;
    }

    /**
     * Get the team's numerical id.
     * Used for tab list sorting.
     * @return Team id.
     */
    public int id() {
        return id;
    }

    /**
     * Add a player to the dead list,
     * and remove them from the alive list.
     * @param player Player to make dead.
     */
    public void killPlayer(Player player) {
        alivePlayers.remove(player.getUniqueId());
        deadPlayers.add(player.getUniqueId());
    }

    /**
     * Get all players on the team.
     * @return List of team players.
     */
    public List<Player> players() {
        List<Player> onlinePlayers = new ArrayList<>();

        // Loop through all stored team member uuids.
        for(UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);

            // Makes sure the player is valid.
            if(player == null || !player.isOnline()) {
                continue;
            }

            onlinePlayers.add(player);
        }

        // Return our created list.
        return onlinePlayers;
    }

    /**
     * Remove a player from the team.
     * @param player Player to remove.
     */
    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        alivePlayers.remove(player.getUniqueId());
        deadPlayers.remove(player.getUniqueId());
    }

    /**
     * Resets a team's alive and dead players.
     * Avoids removing any dead players that are no longer online.
     */
    public void reset() {
        new HashSet<>(deadPlayers()).forEach(player -> {
            alivePlayers.add(player.getUniqueId());
            deadPlayers.remove(player.getUniqueId());
        });
    }

    /**
     * Gets the score of the team.
     * @return Team score.
     */
    public int score() {
        return score;
    }

    /**
     * Get the team's current color.
     * @return Team's team color.
     */
    public TeamColor teamColor() {
        return teamColor;
    }

    /**
     * Get all player uuids on the team.
     * @return List of player uuids.
     */
    public List<UUID> uuids() {
        return players;
    }

    public long challongeID() {
        return challongeID;
    }

    public String name() {
        StringBuilder builder = new StringBuilder();

        int count = 0;
        for(UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);

            if(player == null || !player.isOnline()) {
                continue;
            }

            if(count > 0) {
                builder.append(", ");
            }

            builder.append(player.getName());

            count++;
        }

        return builder.toString();
    }
}