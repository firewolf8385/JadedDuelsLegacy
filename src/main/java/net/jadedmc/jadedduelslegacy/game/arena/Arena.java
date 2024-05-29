package net.jadedmc.jadedduelslegacy.game.arena;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.kit.Kit;
import net.jadedmc.jadedutils.LocationUtils;
import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Represents an area in which a game is played.
 */
public class Arena {
    private final JadedDuelsPlugin plugin;
    private final String fileName;
    private final String name;
    private final String builders;
    private final List<String> kits;
    private final Location spectatorSpawn;
    private final boolean tournamentArena;
    private final Location tournamentSpawn;
    private final List<Location> spawns = new ArrayList<>();
    private final int voidLevel;

    // Raw Location data.
    private final List<String> spawnsRaw;
    private final String tournamentSpawnRaw;
    private final String spectatorSpawnRaw;

    /**
     * Creates the arena.
     * @param plugin Instance of the plugin.
     * @param config Configuration file for the arena, from MongoDB.
     */
    public Arena(JadedDuelsPlugin plugin, Document config) {
        this.plugin = plugin;
        this.fileName = config.getString("fileName");
        this.name = config.getString("name");
        this.builders = config.getString("builders");
        this.voidLevel = config.getInteger("voidLevel");
        this.kits = new ArrayList<>(config.getList("kits", String.class));

        this.spectatorSpawnRaw = config.getString("spectatorSpawn");
        this.spectatorSpawn = LocationUtils.fromString(spectatorSpawnRaw);

        // Set up tournament information.
        if(config.containsKey("tournamentSpawn")) {
            tournamentArena = true;
            tournamentSpawnRaw = config.getString("tournamentSpawn");
            tournamentSpawn = LocationUtils.fromString(config.getString("tournamentSpawn"));
        }
        else {
            tournamentArena = false;
            tournamentSpawn = null;
            tournamentSpawnRaw = null;
        }

        // Load the arena spawns.
        spawnsRaw = new ArrayList<>(config.getList("spawns", String.class));
        for(String spawn : spawnsRaw) {
            spawns.add(LocationUtils.fromString(spawn));
        }
    }

    /**
     * Gets the builders of the arena.
     * @return Arena builders.
     */
    public String builders() {
        return builders;
    }

    /**
     * Gets the file name of the arena.
     * @return Arena file name.
     */
    public String fileName() {
        return fileName;
    }

    /**
     * Check if an arena can use a given kit.
     * @param kit Kit to check.
     * @return Whether the kit can be used.
     */
    public boolean hasKit(Kit kit) {
        return kits.contains(kit.id());
    }

    /**
     * Check if the Arena is a tournament arena.
     * Tournament arenas are intentionally segregated for experience purposes.
     * @return Whether the arena is a tournament arena.
     */
    public boolean isTournamentArena() {
        return tournamentArena;
    }

    /**
     * Get all kits the arena is made for.
     * @return Arena kits.
     */
    public Collection<Kit> kits() {
        Collection<Kit> kits = new HashSet<>();

        for(String id : this.kits) {
            if(plugin.kitManager().kit(id) == null) {
                continue;
            }

            kits.add(plugin.kitManager().kit(id));
        }

        return kits;
    }

    /**
     * Gets a raw collection of the kit ids the arena is made for.
     * @return Arena kit ids.
     */
    public Collection<String> kitsRaw() {
        return kits;
    }

    /**
     * Get the name of the arena.
     * @return Arena name.
     */
    public String name() {
        return name;
    }

    /**
     * Get the player spawns of the arena.
     * @param world World to get the spawns of.
     * @return List of player spawns.
     */
    public List<Location> spawns(World world) {
        List<Location> temp = new ArrayList<>();
        spawns.forEach(spawn -> temp.add(LocationUtils.replaceWorld(world, spawn)));
        return temp;
    }

    /**
     * Gets the raw spawns location string list.
     * @return List of raw spawn location strings.
     */
    public List<String> spawnsRaw() {
        return spawnsRaw;
    }

    /**
     * Get the spectator area of the arena in a specific world.
     * @param world World to get spectator spawn of.
     * @return Spectator spawn location.
     */
    public Location spectatorSpawn(World world) {
        return LocationUtils.replaceWorld(world, spectatorSpawn);
    }

    /**
     * Gets the raw spectator spawn location string.
     * @return Spectator Spawn Location String.
     */
    public String spectatorSpawnRaw() {
        return spectatorSpawnRaw;
    }

    /**
     * Get the arena's tournament spawn.
     * Returns null if it doesn't have one.
     * @param world World to get the tournament spawn of.
     * @return The arena's tournament spawn.
     */
    public Location tournamentSpawn(World world) {
        return LocationUtils.replaceWorld(world, tournamentSpawn);
    }

    /**
     * Get the raw tournament spawn location string.
     * @return Tournament Spawn Location String.
     */
    public String tournamentSpawnRaw() {
        return tournamentSpawnRaw;
    }

    /**
     * Gets the y-level in which players die.
     * @return Arena void level.
     */
    public int voidLevel() {
        return voidLevel;
    }
}