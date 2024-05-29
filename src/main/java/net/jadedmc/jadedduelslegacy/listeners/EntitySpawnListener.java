package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class EntitySpawnListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public EntitySpawnListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if(!(event.getEntity() instanceof FallingBlock fallingBlock)) {
            return;
        }

        Game game = plugin.gameManager().game(fallingBlock.getWorld());

        if(game == null) {
            return;
        }

        Block block = fallingBlock.getWorld().getBlockAt(fallingBlock.getLocation());
        Material type = fallingBlock.getMaterial();
        game.addBlock(block, type);
    }
}