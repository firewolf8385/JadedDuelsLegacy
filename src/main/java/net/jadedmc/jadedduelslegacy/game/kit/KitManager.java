package net.jadedmc.jadedduelslegacy.game.kit;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.kit.kits.ArcherKit;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Stores all information about a kit.
 */
public class KitManager {
    private final Set<Kit> activeKits = new LinkedHashSet<>();
    private final Set<Kit> duelKits = new LinkedHashSet<>();

    /**
     * Creates the Kit Manager.
     * @param plugin Instance of the plugin.
     */
    public KitManager(final JadedDuelsPlugin plugin) {
        activeKits.add(new ArcherKit(plugin));
    }

    /**
     * Get a kit from its name,
     * @param kitName Name of the kit.
     * @return Kit from name.
     */
    public Kit kit(final String kitName) {
        for(Kit kit : this.activeKits) {
            if(kit.name().equalsIgnoreCase(kitName) || kit.id().equalsIgnoreCase(kitName)) {
                return kit;
            }
        }

        for(Kit kit : this.duelKits) {
            if(kit.name().equalsIgnoreCase(kitName) || kit.id().equalsIgnoreCase(kitName)) {
                return kit;
            }
        }

        return null;
    }

    /**
     * Get all active kits.
     * @return All active kits.
     */
    public Set<Kit> activeKits() {
        return activeKits;
    }

    /**
     * Get all currently loaded kits, both active and duel kits.
     * @return All kits.
     */
    public Set<Kit> kits() {
        Set<Kit> kits = new LinkedHashSet<>();
        kits.addAll(activeKits);
        kits.addAll(duelKits);

        return kits;
    }
}