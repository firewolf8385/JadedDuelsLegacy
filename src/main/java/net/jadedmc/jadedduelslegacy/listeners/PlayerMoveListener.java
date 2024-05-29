package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.GameState;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public PlayerMoveListener(JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Game game = plugin.gameManager().game(player);

        if(game == null) {
            return;
        }

        if(game.kit().waterKills()) {
            if(game.spectators().contains(player)) {
                player.teleport(game.arena().spectatorSpawn(game.world()));
                return;
            }

            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            Block block2 = player.getLocation().getBlock();

            if(block.getType() == Material.WATER || block2.getType() == Material.WATER) {
                game.playerKilled(player);
                return;
            }
        }

        if(player.getLocation().getY() < game.kit().voidLevel()) {
            if(game.gameState() == GameState.COUNTDOWN || game.gameState() == GameState.END) {
                player.teleport(game.arena().spectatorSpawn(game.world()));
                return;
            }

            if(game.spectators().contains(player)) {
                player.teleport(game.arena().spectatorSpawn(game.world()));
                return;
            }

            game.playerKilled(player);
            player.teleport(game.arena().spectatorSpawn(game.world()));
        }
    }
}