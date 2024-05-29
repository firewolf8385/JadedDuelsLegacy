package net.jadedmc.jadedduelslegacy.game.arena;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.jadedmc.jadedcore.JadedAPI;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.GameType;
import net.jadedmc.jadedduelslegacy.game.arena.builder.ArenaBuilder;
import net.jadedmc.jadedduelslegacy.game.kit.Kit;
import org.bson.Document;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

/**
 * This class manages the creation and loading of arenas.
 */
public class ArenaManager {
    private final JadedDuelsPlugin plugin;
    private final Map<String, Arena> arenas = new HashMap<>();
    private ArenaBuilder arenaBuilder;

    /**
     * Creates the ArenaManager.
     * @param plugin Instance of the plugin.
     */
    public ArenaManager(final JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Gets the current arena builder.
     * Returns null if there is none.
     * @return Active arena builder.
     */
    public ArenaBuilder arenaBuilder() {
        return arenaBuilder;
    }

    /**
     * Replaces the current arena builder.
     * Used when canceling arena building.
     * @param arenaBuilder New arena builder. Can be null.
     */
    public void arenaBuilder(ArenaBuilder arenaBuilder) {
        this.arenaBuilder = arenaBuilder;
    }

    /**
     * Get an arena from its id.
     * Null if invalid.
     * @param id ID of the arena.
     * @return Associated Arena object,
     */
    public Arena getArena(String id) {
        if(arenas.containsKey(id)) {
            return arenas.get(id);
        }

        return null;
    }

    /**
     * Gets all currently loaded arenas.
     * @return Collection of arenas.
     */
    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    /**
     * Gets all arenas that support a given kit.
     * @param kit Kit to get arenas for.
     * @param gameType Game type.
     * @return Collection of arenas for that mode.
     */
    public Collection<Arena> getArenas(Kit kit, GameType gameType) {
        Collection<Arena> kitArenas = new HashSet<>();

        if(gameType == GameType.TOURNAMENT) {
            for(Arena arena : getArenas()) {
                if(arena.isTournamentArena()) {
                    if(arena.kits().contains(kit)) {
                        kitArenas.add(arena);
                    }
                }
            }
        }
        else {
            for(Arena arena : getArenas()) {
                if(!arena.isTournamentArena()) {
                    if(arena.kits().contains(kit)) {
                        kitArenas.add(arena);
                    }
                }
            }
        }

        return kitArenas;
    }

    /**
     * Loads all arenas from
     */
    public void loadArenas() {
        arenas.clear();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            MongoDatabase database = JadedAPI.getMongoDB().client().getDatabase("duels_legacy");
            MongoCollection<Document> collection = database.getCollection("maps");

            // Loads all arenas set up in MongoDB.
            FindIterable<Document> documentIterator = collection.find();
            for(Document document : documentIterator) {
                loadArena(document);
            }
        });
    }

    /**
     * Loads an Arena from its config file.
     * Stored in MongoDB
     * @param document Arena configuration file.
     */
    public void loadArena(Document document) {
        Arena arena = new Arena(plugin, document);
        arenas.put(arena.fileName(), arena);
    }

    public void loadArena(String id) {
        Document document = JadedAPI.getMongoDB().client().getDatabase("duels_legacy").getCollection("maps").find(eq("fileName", id)).first();
        loadArena(document);
    }
}