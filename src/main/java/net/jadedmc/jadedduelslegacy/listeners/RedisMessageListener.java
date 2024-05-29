package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedchat.utils.StringUtils;
import net.jadedmc.jadedcore.JadedAPI;
import net.jadedmc.jadedcore.events.RedisMessageEvent;
import net.jadedmc.jadedcore.minigames.Minigame;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.GameType;
import net.jadedmc.jadedduelslegacy.game.kit.Kit;
import net.jadedmc.jadedduelslegacy.game.tournament.BestOf;
import net.jadedmc.jadedduelslegacy.game.tournament.EliminationType;
import net.jadedmc.jadedduelslegacy.game.tournament.TeamType;
import net.jadedmc.jadedutils.chat.ChatUtils;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import redis.clients.jedis.Jedis;

import java.util.*;

public class RedisMessageListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public RedisMessageListener(final JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMessage(RedisMessageEvent event) {
        if(event.getChannel().equalsIgnoreCase("duels_legacy")) {
            String[] args = event.getMessage().split(" ");

            if(args.length > 4) {
                return;
            }

            switch(args[0].toLowerCase()) {
                case "create" -> {
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        String gameUUID = args[1];

                        try(Jedis jedis = JadedAPI.getRedis().jedisPool().getResource()) {
                            Document document = Document.parse(jedis.get("duels:legacy:games:" + gameUUID));

                            if(!document.getString("server").equalsIgnoreCase(JadedAPI.getCurrentInstance().getName())) {
                                return;
                            }

                            plugin.gameManager().fromDocument(document).whenComplete((result, error) -> error.printStackTrace());
                        }
                    });
                }

                case "setup" -> {
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        String gameUUID = args[1];

                        System.out.println("Setup received: " + gameUUID);

                        try(Jedis jedis = JadedAPI.getRedis().jedisPool().getResource()) {
                            Collection<String> playerUUIDs = new HashSet<>();

                            Document document = Document.parse(jedis.get("duels:legacy:games:" + gameUUID));
                            String serverName = document.getString("server");

                            if(serverName.equalsIgnoreCase(JadedAPI.getCurrentInstance().getName())) {
                                return;
                            }


                            Document teamsDocument = document.get("teams", Document.class);
                            Set<String> teamsList = teamsDocument.keySet();
                            System.out.println(teamsList);

                            for(String team : teamsList) {
                                Document teamDocument = teamsDocument.get(team, Document.class);
                                playerUUIDs.addAll(teamDocument.getList("uuids", String.class));
                            }

                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                for(Player player : plugin.getServer().getOnlinePlayers()) {
                                    if(!playerUUIDs.contains(player.getUniqueId().toString())) {
                                        continue;
                                    }

                                    JadedAPI.sendBungeecordMessage(player, "BungeeCord", "Connect", serverName);
                                }
                            });
                        }
                    });
                }

                case "arena" -> {
                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                        String arenaID = args[1];
                        System.out.println("Arena Update Received: " + arenaID);
                        plugin.arenaManager().loadArena(arenaID);
                    });
                }

                case "spectator" -> {
                    UUID gameUUID = UUID.fromString(args[1]);
                    UUID spectatorUUID = UUID.fromString(args[2]);

                    for(Game game : plugin.gameManager().games()) {
                        if(game.uuid().equals(gameUUID)) {
                            game.addSpectator(spectatorUUID);
                            JadedAPI.sendToServer(spectatorUUID, JadedAPI.getCurrentInstance().getName());
                            break;
                        }
                    }
                }
            }
        }
        else if(event.getChannel().equalsIgnoreCase("tournament")) {
            String[] args = event.getMessage().split(" ");

            switch (args[0].toLowerCase()) {
                case "create" -> {
                    String host = args[1];
                    Kit kit = plugin.kitManager().kit(args[2]);
                    TeamType teamType = TeamType.valueOf(args[3]);
                    EliminationType eliminationType = EliminationType.valueOf(args[4]);
                    BestOf bestOf = BestOf.valueOf(args[5]);

                    plugin.tournamentManager().setupTournament(host, kit, teamType, eliminationType, bestOf);
                }

                case "clear" -> {
                    plugin.tournamentManager().reset();
                }

                case "broadcast" -> {
                    String message = StringUtils.join(Arrays.copyOfRange(args, 1, args.length), " ");

                    for(Game game : plugin.gameManager().games()) {
                        if(game.gameType() == GameType.TOURNAMENT) {
                            ChatUtils.broadcast(game.world(), message);
                        }
                    }

                    if(JadedAPI.getCurrentInstance().getMinigame() == Minigame.TOURNAMENTS_LEGACY) {
                        ChatUtils.broadcast(message);
                    }
                }
            }
        }
    }
}