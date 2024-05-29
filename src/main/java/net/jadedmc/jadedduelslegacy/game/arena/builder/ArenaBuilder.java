package net.jadedmc.jadedduelslegacy.game.arena.builder;

import net.jadedmc.jadedcore.JadedAPI;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.arena.Arena;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

/**
 * Stores data of an arena that is still being set up.
 */
public class ArenaBuilder {
    private final JadedDuelsPlugin plugin;
    private String spectatorSpawn = null;
    private String name;
    private String builders;
    private String id;
    private int voidLevel = -1;
    private final Collection<String> kits = new HashSet<>();
    private final List<String> spawns = new ArrayList<>();
    private boolean editMode = false;
    private String tournamentSpawn = null;
    private final World world;

    /**
     * Creates the arena builder.
     * @param plugin Instance of the plugin.
     */
    public ArenaBuilder(final JadedDuelsPlugin plugin, World world) {
        this.plugin = plugin;
        this.world = world;
    }

    /**
     * Creates an arena builder using an existing arena.
     * Used to edit the existing arena.
     * @param plugin Instance of the plugin.
     * @param arena Arena to be edited.
     */
    public ArenaBuilder(final JadedDuelsPlugin plugin, Arena arena, World world) {
        this.plugin = plugin;
        this.world = world;
        this.id = arena.fileName();
        this.builders = arena.builders();
        this.spectatorSpawn = arena.spectatorSpawnRaw();
        this.voidLevel = arena.voidLevel();
        this.name = arena.name();

        kits.addAll(arena.kitsRaw());
        editMode = true;

        spawns.addAll(arena.spawnsRaw());
    }

    /**
     * Adds a supported kit to the arena.
     * @param kit Kit to add.
     */
    public void addKit(String kit) {
        kits.add(kit);
    }

    /**
     * Set the builders of the arena.
     * @param builders Arena builders.
     */
    public void builders(String builders) {
        this.builders = builders;
    }

    /**
     * Get if the arena builder is in edit mode.
     * @return If in edit mode.
     */
    public boolean editMode() {
        return editMode;
    }

    /**
     * Get the id of the arena being created.
     * @return Arena id.
     */
    public String id() {
        return id;
    }

    /**
     * Set the id of the arena.
     * @param id Arena id.
     */
    public void id(String id) {
        this.id = id;
    }

    public boolean isSet() {
        spawns.clear();

        TreeMap<Integer, String> tempSpawnLocations = new TreeMap<>();

        // Loop through all spawn locations.
        System.out.println("Loaded Chunks: " + world.getLoadedChunks().length);
        for(Chunk chunk : world.getLoadedChunks()) {
            World world = chunk.getWorld();
            for(int x = 0; x < 16; x++) {
                for(int y = 0; y < (world.getMaxHeight() - 1); y++) {
                    for(int z = 0; z < 16; z++) {
                        Block block = chunk.getBlock(x, y, z);

                        if(block.getType() == Material.SIGN || block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN) {
                            Sign sign = (Sign) block.getState();
                            String[] lines = sign.getLines();

                            if(lines.length < 2) {
                                System.out.println("Sign found with not enough lines!");
                                continue;
                            }

                            if(!lines[0].toLowerCase().equalsIgnoreCase("[Spawn]")) {
                                System.out.println(lines[0]);
                                continue;
                            }

                            org.bukkit.material.Sign signMaterial = (org.bukkit.material.Sign) block.getState().getData();
                            float yaw = yawFromBlockFace(signMaterial.getFacing());

                            String locationString = "world," + block.getX() + "," + block.getY() + "," + block.getZ() + "," + yaw + ",0";
                            System.out.println("Sign Found: " + locationString + ": " + lines[1]);


                            switch(lines[1].toLowerCase()) {
                                case "tournament" -> tournamentSpawn = locationString;
                                case "spectate" -> spectatorSpawn = locationString;
                                default -> tempSpawnLocations.put(Integer.parseInt(lines[1]) - 1, locationString);
                            }
                        }
                    }
                }
            }
        }

        for(int index : tempSpawnLocations.keySet()) {
            spawns.add(tempSpawnLocations.get(index));
        }

        if(spectatorSpawn == null) {
            System.out.println("No Spectator Spawn Found");
            return false;
        }

        if(spawns.size() < 2) {
            System.out.println("Not Enough Spawns! Found" + spawns.size());
            return false;
        }

        return true;
    }

    /**
     * Gets all kits the arena is set for.
     * @return All kits.
     */
    public Collection<String> kits() {
        return kits;
    }

    /**
     * Set the name of the arena.
     * @param name Arena's name.
     */
    public void name(String name) {
        this.name = name;
    }

    /**
     * Set the void level of the arena.
     * @param voidLevel Arena void level.
     */
    public void voidLevel(int voidLevel) {
        this.voidLevel = voidLevel;
    }

    public void save() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            Document document = new Document("fileName", id)
                    .append("name", name)
                    .append("builders", builders)
                    .append("voidLevel", voidLevel)
                    .append("kits", kits).append("spectatorSpawn", spectatorSpawn)
                    .append("spawns", spawns);

            if(tournamentSpawn != null) {
                document.append("tournamentSpawn", tournamentSpawn);
            }

            // Add the document to MongoDB.
            if(!editMode) {
                JadedAPI.getMongoDB().client().getDatabase("duels_legacy").getCollection("maps").insertOne(document);
            }
            else {
                // Replaces the existing file.
                Document old = JadedAPI.getMongoDB().client().getDatabase("duels_legacy").getCollection("maps").find(eq("fileName", id)).first();
                JadedAPI.getMongoDB().client().getDatabase("duels_legacy").getCollection("maps").replaceOne(old, document);
            }

            File worldFolder = world.getWorldFolder();

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                world.getPlayers().forEach(player -> JadedAPI.getPlugin().lobbyManager().sendToLobby(player));
                Bukkit.unloadWorld(world, true);

                // Saves the world to MongoDB.
                JadedAPI.getPlugin().worldManager().saveWorld(worldFolder, id);

                // Load the new arena.
                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    JadedAPI.getRedis().publish("duels_legacy", "arena " + id);
                });
            });
        });
    }

    private float yawFromBlockFace(BlockFace blockFace) {
        switch (blockFace) {
            case NORTH -> {
                return 180f;
            }
            case NORTH_EAST -> {
                return -135f;
            }
            case EAST -> {
                return -90f;
            }
            case SOUTH_EAST -> {
                return -45f;
            }
            case SOUTH -> {
                return 0f;
            }
            case SOUTH_WEST -> {
                return 45f;
            }
            case WEST -> {
                return 90f;
            }
            case NORTH_WEST -> {
                return 135f;
            }
            default -> {
                return 1f;
            }
        }
    }
}