package net.jadedmc.jadedduelslegacy.game;

import at.stefangeyer.challonge.exception.DataAccessException;
import at.stefangeyer.challonge.model.Match;
import at.stefangeyer.challonge.model.Tournament;
import at.stefangeyer.challonge.model.query.MatchQuery;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import net.jadedmc.jadedchat.JadedChat;
import net.jadedmc.jadedcore.JadedAPI;
import net.jadedmc.jadedcore.minigames.Minigame;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.arena.Arena;
import net.jadedmc.jadedduelslegacy.game.kit.Kit;
import net.jadedmc.jadedduelslegacy.game.teams.Team;
import net.jadedmc.jadedduelslegacy.game.teams.TeamColor;
import net.jadedmc.jadedduelslegacy.game.teams.TeamManager;
import net.jadedmc.jadedduelslegacy.utils.GameUtils;
import net.jadedmc.jadedutils.Timer;
import net.jadedmc.jadedutils.chat.ChatUtils;
import net.jadedmc.jadedutils.items.ItemBuilder;
import org.bson.Document;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Represents a singular match.
 */
public class Game {
    private final JadedDuelsPlugin plugin;
    private final World world;
    private final TeamManager teamManager = new TeamManager();

    // Important variables.
    private final Kit kit;
    private final Arena arena;
    private final UUID uuid;
    private final GameType gameType;
    private Timer timer;
    private GameState gameState;
    private final Collection<UUID> spectators = new HashSet<>();
    private int round = 0;
    private final Map<Block, Material> blocks = new HashMap<>();
    private final int pointsNeeded;
    private final long matchID;
    private final String tournamentURL;

    public Game(JadedDuelsPlugin plugin, World world, Document document) {
        this.plugin = plugin;
        this.world = world;

        this.kit = plugin.kitManager().kit(document.getString("kit"));
        this.arena = plugin.arenaManager().getArena(document.getString("arena"));
        this.uuid = UUID.fromString(document.getString("uuid"));
        this.gameType = GameType.valueOf(document.getString("gameType"));
        this.timer = new Timer(plugin);
        this.pointsNeeded = document.getInteger("pointsNeeded");

        gameState = GameState.WAITING;

        // Try to setup teams
        Document teamsDocument = document.get("teams", Document.class);
        Set<String> teamsList = teamsDocument.keySet();

        for(String team : teamsList) {
            Document teamDocument = teamsDocument.get(team, Document.class);

            if(gameType == GameType.TOURNAMENT) {
                // turning long, to int, to long here too
                teamManager.createTeam(teamDocument.getList("uuids", String.class), TeamColor.valueOf(team), Long.valueOf(teamDocument.getInteger("challongeID")));
            }
            else {
                teamManager.createTeam(teamDocument.getList("uuids", String.class), TeamColor.valueOf(team));
            }
        }

        if(gameType == GameType.TOURNAMENT) {
            // Fuck MongoDB for making me do this
            matchID = Long.valueOf(document.getInteger("matchID"));
            tournamentURL = document.getString("tournamentURL");
        }
        else {
            matchID = 0;
            tournamentURL = null;
        }

        // Deletes setup signs
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            world.getBlockAt(arena.spectatorSpawn(world)).setType(Material.AIR);
            if(arena.isTournamentArena()) world.getBlockAt(arena.tournamentSpawn(world)).setType(Material.AIR);
            arena.spawns(world).forEach(location -> world.getBlockAt(location).setType(Material.AIR));
        });

        updateRedis();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            JadedAPI.getRedis().publish("duels_legacy", "setup " + uuid);
        });
    }

    private void startGame() {
        if(gameState != GameState.WAITING) {
            return;
        }

        gameState = GameState.STARTING;

        // Run the start code for all players.
        for(Player player : players()) {
            Team team = this.teamManager.team(player);
            List<Player> opponents = new ArrayList<>();

            // Load all opponents.
            teamManager.teams().forEach(opposingTeam -> {
                if(!opposingTeam.equals(team)) {
                    opponents.addAll(opposingTeam.players());
                }
            });

            System.out.println("Player: " + player.getName());

            // Displays the start message.
            ChatUtils.chat(player, "<center><dark_gray><st>+-----------------------***-----------------------+");
            ChatUtils.chat(player, ChatUtils.centerText("<green><bold>" + kit.name() + " Duel"));
            ChatUtils.chat(player, "");

            // Display the opponents label, based on the number.
            if(opponents.size() == 1) {
                ChatUtils.chat(player, "<center><green>Opponent:");
            }
            else {
                ChatUtils.chat(player, "<center><green>Opponents:");
            }

            // Lists the opponents.
            for(Player opponent : opponents) {
                ChatUtils.chat(player, ChatUtils.centerText(JadedAPI.getJadedPlayer(opponent).getRank().getChatPrefix() + opponent.getName()));
            }

            ChatUtils.chat(player, "");
            ChatUtils.chat(player, "<center><dark_gray><st>+-----------------------***-----------------------+");
        }

        // Start the round.
        startRound();
    }

    private void startRound() {
        teamManager.reset();
        round++;

        updateRedis();

        // Remove old entities at the start of each round.
        for(Entity entity : world.getEntities()) {
            if(entity instanceof Player) {
                continue;
            }

            entity.remove();
        }

        // Reset players
        for(Player player : players()) {
            spectators.remove(player.getUniqueId());
            player.spigot().setCollidesWithEntities(true);

            // Clears arrows from the player. Requires craftbukkit.
            JadedAPI.clearArrows(player);

            for(Player other : players()) {
                other.showPlayer(player);
            }
        }

        // Spawn teams.
        for(Team team : teamManager.teams()) {
            int spawnNumber = teamManager.teams().indexOf(team);
            Location spawn = arena.spawns(world).get(spawnNumber);

            // Spawn in each player in the team.
            for(Player player : team.players()) {
                player.teleport(spawn);
                kit.apply(this, player);
                kit.scoreboard(this, player).update(player);
            }
        }

        // Show invisible players for round reset.
        for(Player player : players()) {
            for(Player other : players()) {
                if(player.equals(other)) {
                    continue;
                }

                player.showPlayer(other);
            }

            // Show each player to each spectator.
            for(Player spectator : spectators()) {
                spectator.showPlayer(player);
            }
        }

        roundCountdown();
    }

    private void roundCountdown() {
        // Make sure the game isn't already in countdown.
        if(gameState == GameState.COUNTDOWN) {
            return;
        }

        gameState = GameState.COUNTDOWN;

        updateRedis();

        BukkitRunnable countdown = new  BukkitRunnable() {
            int counter = 4;
            public void run() {
                counter--;

                if(gameState == GameState.END) {
                    cancel();
                }

                if(counter  != 0) {
                    ChatUtils.broadcast(world, "<green>Starting in " + counter + "...");

                    for(Player player : players()) {
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1, 1);
                    }
                }
                else {
                    for(Player player : players()) {
                        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.parseSound(), 1, 2);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> runRound(), 1);
                        cancel();
                    }
                }
            }
        };
        countdown.runTaskTimer(plugin, 0, 20);
    }

    private void runRound() {
        if(gameState == GameState.RUNNING) {
            return;
        }

        gameState = GameState.RUNNING;
        timer = new Timer(plugin);
        timer.start();

        updateRedis();

        // Spawn teams.
        for(Team team : teamManager.teams()) {
            int spawnNumber = teamManager.teams().indexOf(team);
            Location spawn = arena.spawns(world).get(spawnNumber);

            // Spawn in each player in the team.
            for(Player player : team.players()) {
                player.teleport(spawn);
                player.closeInventory();
                player.setFireTicks(0);
                kit.onRoundStart(this, player);
            }
        }
    }

    private void endRound(Team winner) {
        // Prevent from running the code twice.
        if(gameState == GameState.END) {
            return;
        }

        // Prevent issues if the game ends during countdown.
        if(gameState != GameState.COUNTDOWN) {
            timer.stop();
        }

        winner.addPoint();
        gameState = GameState.END;
        timer.stop();

        for(Player player : players()) {
            kit.onRoundEnd(this, player);
        }

        // Display the game over message.
        {
            ChatUtils.broadcast(world, "<center><dark_gray><st>+-----------------------***-----------------------+");
            ChatUtils.broadcast(world, "");
            ChatUtils.broadcast(world, "<center><green><bold>" + kit.name() + " Duel</bold> <gray>- <white><bold>" + timer.toString());
            ChatUtils.broadcast(world, "");

            if(winner.players().size() > 1) {
                ChatUtils.broadcast(world, "<center><green>Winners:");
            }
            else {
                ChatUtils.broadcast(world, "<center><green>Winner:");
            }

            for(Player player : winner.players()) {
                if(teamManager.team(player).deadPlayers().contains(player)) {
                    ChatUtils.broadcast(world, "<center>" + JadedAPI.getJadedPlayer(player).getRank().getChatPrefix() + player.getName() + "<green>(<red>0%<green>)");
                }
                else {
                    ChatUtils.broadcast(world, "<center>" + JadedAPI.getJadedPlayer(player).getRank().getChatPrefix() + player.getName() + " &a(" + GameUtils.getFormattedHealth(player) + "&a)");
                }
            }

            if(pointsNeeded > 1 && gameType != GameType.FFA) {
                ChatUtils.broadcast(world, "");
                ChatUtils.broadcast(world, ChatUtils.centerText("&aScore: &f" + winner.score() + " - " + teamManager.opposingTeam(winner).score()));
            }

            ChatUtils.broadcast(world, "");
            ChatUtils.broadcast(world, "<center><dark_gray><st>+-----------------------***-----------------------+");
        }

        updateRedis();

        if(winner.score() < pointsNeeded) {
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                resetArena();
                startRound();
            }, 5*20);
        }
        else {

            if(gameType == GameType.TOURNAMENT) {
                Team loser = teamManager.opposingTeam(winner);

                plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                    MatchQuery.MatchQueryBuilder builder;
                    Match match = null;
                    try {
                        Tournament tournament = plugin.tournamentManager().getChallonge().getTournament(tournamentURL);
                        match = plugin.tournamentManager().getChallonge().getMatch(tournament, matchID);
                    }
                    catch (DataAccessException e) {
                        throw new RuntimeException(e);
                    }

                    if(winner.challongeID() == match.getPlayer1Id()) {
                        builder = MatchQuery.builder()
                                .winnerId(winner.challongeID())
                                .scoresCsv(winner.score() + "-" + loser.score());
                    }
                    else {
                        builder = MatchQuery.builder()
                                .winnerId(winner.challongeID())
                                .scoresCsv(loser.score() + "-" + winner.score());
                    }

                    boolean sent = false;
                    while(!sent) {
                        try {
                            plugin.tournamentManager().getChallonge().updateMatch(match, builder.build());
                            sent = true;
                            Thread.sleep(1000);
                        }
                        catch (DataAccessException | InterruptedException exception) {
                            exception.printStackTrace();
                        }

                    }
                }, 3*20);

                plugin.tournamentManager().broadcastMessage("&a&lTournament &8» &f" + winner.name() + " &ahas defeated &f" + loser.name() + " &7(&f" + winner.score() + " &8- &f" + loser.score() + "&7)&a.");
            }

            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                for(Player player : players()) {
                    if(gameType == GameType.TOURNAMENT) {
                        JadedAPI.sendToLobby(player, Minigame.TOURNAMENTS_LEGACY);
                    }
                    else {
                        JadedAPI.sendToLobby(player, Minigame.DUELS_LEGACY);
                    }
                }

                for(Player player : spectators()) {
                    if(gameType == GameType.TOURNAMENT) {
                        JadedAPI.sendToLobby(player, Minigame.TOURNAMENTS_LEGACY);
                    }
                    else {
                        JadedAPI.sendToLobby(player, Minigame.DUELS_LEGACY);
                    }
                }


                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                    JadedAPI.getRedis().del("duels:legacy:games:" + uuid);
                });

                for(Entity entity : world.getEntities()) {
                    if(entity instanceof Player) {
                        continue;
                    }

                    entity.remove();
                }

                plugin.gameManager().deleteGame(this);
            }, 5*20);
        }
    }

    public void addBlock(Block block, Material material) {
        if(blocks.containsKey(block)) {
            return;
        }

        blocks.put(block, material);
    }

    public void addPlayer(Player player) {
        if(spectators.contains(player.getUniqueId())) {
            addSpectator(player);
            return;
        }

        Team playerTeam = teamManager.team(player);
        int spawn = teamManager.teams().indexOf(playerTeam);
        player.teleport(arena.spawns(world).get(spawn));

        updateRedis();

        // Update player's chat channel.
        if(JadedChat.getChannel(player).isDefaultChannel()) {
            if(gameType == GameType.TOURNAMENT) {
                JadedChat.setChannel(player, JadedChat.getChannel("TOURNAMENT"));
            }
            else {
                JadedChat.setChannel(player, JadedChat.getChannel("GAME"));
            }
        }

        // Check if we can start the game.
        {
            int count = 0;
            int expected = 0;

            // Loop through all teams checking if the player counts match.
            for(Team team : teamManager.teams()) {
                count += team.players().size();
                expected += team.uuids().size();
            }

            // If they do, start the game.
            if(count == expected) {
                startGame();
            }
        }
    }

    /**
     * Gets the arena being used in the game.
     * @return Game arena.
     */
    public Arena arena() {
        return arena;
    }

    /**
     * Add a spectator to the game.
     * @param spectator Spectator to add.
     */
    public void addSpectator(Player spectator) {
        if(!spectators.contains(spectator.getUniqueId())) {
            spectators.add(spectator.getUniqueId());
        }

        updateRedis();

        // Doesn't teleport player if they were in the game before.
        if(teamManager.team(spectator) == null) {
            if(gameType != GameType.TOURNAMENT) {
                spectator.teleport(arena.spectatorSpawn(this.world));
            }
            else {
                spectator.teleport(arena.tournamentSpawn(this.world));
            }
        }

        if(gameType != GameType.TOURNAMENT) {
            spectator.getInventory().clear();
            spectator.getInventory().setArmorContents(null);
            spectator.setAllowFlight(true);
            spectator.setFlying(true);
            spectator.setMaxHealth(20.0);
            spectator.setHealth(20.0);
            spectator.setFoodLevel(20);
            spectator.setFireTicks(0);
            spectator.setGameMode(GameMode.ADVENTURE);

            // Prevents player from interfering.
            spectator.spigot().setCollidesWithEntities(false);

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for(Player pl : world.getPlayers()) {
                    pl.hidePlayer(spectator);

                    if(spectators.contains(pl.getUniqueId())) {
                        spectator.hidePlayer(pl);
                    }
                }
            }, 2);
        }
        else {
            spectator.getInventory().clear();
            spectator.getInventory().setArmorContents(null);
            spectator.setMaxHealth(20.0);
            spectator.setHealth(20.0);
            spectator.setFoodLevel(20);
            spectator.setFireTicks(0);
            spectator.setGameMode(GameMode.ADVENTURE);

            if(teamManager.team(spectator) != null) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for(Player pl : world.getPlayers()) {
                        pl.hidePlayer(spectator);

                        if(spectators.contains(pl)) {
                            spectator.hidePlayer(pl);
                        }
                    }
                }, 2);
            }
        }

        // Doesn't teleport player if they were in the game before.
        if(teamManager.team(spectator) == null) {
            spectator.getInventory().setItem(3, new ItemBuilder(Material.EYE_OF_ENDER).setDisplayName("<green><bold>Spectate").build());
            ItemStack leave = new ItemBuilder(XMaterial.RED_BED)
                    .setDisplayName("<red>Leave Match")
                    .build();
            spectator.getInventory().setItem(7, leave);
        }

        // Update player's chat channel.
        if(JadedChat.getChannel(spectator).isDefaultChannel()) {
            if(gameType == GameType.TOURNAMENT) {
                JadedChat.setChannel(spectator, JadedChat.getChannel("TOURNAMENT"));
            }
            else {
                JadedChat.setChannel(spectator, JadedChat.getChannel("GAME"));
            }
        }

        updateRedis();
    }

    public Collection<Block> blocks() {
        return blocks.keySet();
    }

    /**
     * Gets the current state of the game.
     * @return Game state.
     */
    public GameState gameState() {
        return gameState;
    }

    /**
     * Gets the Game Type.
     * @return Game type.
     */
    public GameType gameType() {
        return gameType;
    }

    /**
     * Gets the kit the game is using.
     * @return Kit being used.
     */
    public Kit kit() {
        return kit;
    }

    /**
     * Runs when a played disconnects.
     * @param player Player who disconnected.
     */
    public void playerDisconnect(Player player) {
        if(spectators.contains(player.getUniqueId())) {
            removeSpectator(player);
            return;
        }

        ChatUtils.broadcast(world, teamManager.team(player).teamColor().chatColor() + player.getName() + " disconnected.");
        teamManager.team(player).killPlayer(player);
        player.getLocation().getWorld().strikeLightningEffect(player.getLocation());

        for(Team team : teamManager.teams()) {
            if(team.alivePlayers().size() == 0) {
                teamManager.killTeam(team);

                if(teamManager.aliveTeams().size() == 1) {
                    Team winner = teamManager.aliveTeams().get(0);
                    endRound(winner);
                    break;
                }
            }
        }

        updateRedis();
    }

    /**
     * Runs when a player is killed.
     * @param player Player who was killed.
     */
    public void playerKilled(Player player) {
        if(spectators.contains(player.getUniqueId())) {
            return;
        }

        player.getLocation().getWorld().strikeLightningEffect(player.getLocation());
        addSpectator(player);
        teamManager.team(player).killPlayer(player);
        ChatUtils.broadcast(world, teamManager.team(player).teamColor().chatColor()  + player.getName() + " &ahas died!");

        // Prevents stuff from breaking if the game is already over.
        if(gameState == GameState.END) {
            return;
        }

        for(Team team : teamManager.teams()) {
            if(team.alivePlayers().size() == 0) {
                teamManager.killTeam(team);

                if(teamManager.aliveTeams().size() == 1) {
                    Team winner = teamManager.aliveTeams().get(0);
                    endRound(winner);
                    break;
                }
            }
        }

        updateRedis();
    }

    /**
     * Runs when a player is killed.
     * @param player Player who was killed.
     * @param killer Player who killed the player.
     */
    public void playerKilled(Player player, Player killer) {
        if(spectators.contains(player)) {
            return;
        }

        player.getLocation().getWorld().strikeLightningEffect(player.getLocation());
        addSpectator(player);
        teamManager.team(player).killPlayer(player);
        ChatUtils.broadcast(world, teamManager.team(player).teamColor().chatColor()  + player.getName() + " &awas killed by " + teamManager.team(killer).teamColor().chatColor() + killer.getName() + " &a(" + GameUtils.getFormattedHealth(killer) + "&a)");

        // Prevents stuff from breaking if the game is already over.
        if(gameState == GameState.END) {
            return;
        }

        for(Team team : teamManager.teams()) {
            if(team.alivePlayers().size() == 0) {
                teamManager.killTeam(team);

                if(teamManager.aliveTeams().size() == 1) {
                    Team winner = teamManager.aliveTeams().get(0);
                    endRound(winner);
                    break;
                }
            }
        }

        updateRedis();
    }

    /**
     * Get all players that are on a team.
     * @return All game players.
     */
    public Collection<Player> players() {
        Collection<Player> players = new HashSet<>();

        for(Team team : teamManager.teams()) {
            players.addAll(team.players());
        }

        return players;
    }

    /**
     * Remove a spectator from the game.
     * @param player Spectator to remove.
     */
    public void removeSpectator(Player player) {
        spectators.remove(player.getUniqueId());

        for(Player pl : world.getPlayers()) {
            pl.showPlayer(player);
        }

        if(gameType == GameType.TOURNAMENT) {
            JadedAPI.sendToLobby(player, Minigame.TOURNAMENTS_LEGACY);
        }
        else {
            JadedAPI.sendToLobby(player, Minigame.DUELS_LEGACY);
        }

        updateRedis();
    }

    public void resetArena() {
        for(Block block : blocks.keySet()) {
            block.setType(blocks.get(block));
        }

        blocks.clear();
    }

    /**
     * Get all players currently spectating.
     * @return All current spectators.
     */
    public Collection<Player> spectators() {
        Collection<Player> players = new HashSet<>();

        for(UUID uuid : spectators) {
            Player player = Bukkit.getPlayer(uuid);

            if(player == null || !player.isOnline()) {
                continue;
            }

            players.add(player);
        }

        return players;
    }

    /**
     * Gets the game team manager.
     * @return Team manager.
     */
    public TeamManager teamManager() {
        return teamManager;
    }

    /**
     * Gets the game's timer.
     * Changes after each round.
     * @return Current game timer.
     */
    public Timer timer() {
        return timer;
    }

    public void updateRedis() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> jsonSpectators = new ArrayList<>();
            spectators.forEach(spectator -> jsonSpectators.add(spectator.toString()));

            Document document = new Document()
                    .append("uuid", uuid.toString())
                    .append("kit", kit.id())
                    .append("arena", arena.fileName())
                    .append("type", gameType.toString())
                    .append("state", gameState.toString())
                    .append("server", JadedAPI.getCurrentInstance().getName())
                    .append("spectators", jsonSpectators);

            if(gameType == GameType.TOURNAMENT) {
                document.append("matchID", matchID);
                document.append("tournamentURL", tournamentURL);
            }

            Document teamsDocument = new Document();
            for(Team team : teamManager.teams()) {
                Document teamDocument = new Document();
                if(gameType == GameType.TOURNAMENT) {
                    teamDocument.append("teamID", team.challongeID());
                }

                List<String> uuids = new ArrayList<>();
                List<String> usernames = new ArrayList<>();

                for(UUID uuid : team.uuids()) {
                    uuids.add(uuid.toString());

                    Player player = Bukkit.getPlayer(uuid);

                    if(player == null || !player.isOnline()) {
                        continue;
                    }

                    usernames.add(player.getName());
                }

                teamDocument.append("uuids", uuids);
                teamDocument.append("usernames", usernames);
                teamsDocument.append(team.teamColor().toString(), teamDocument);
            }

            document.append("teams", teamsDocument);

            // Update to redis.
            JadedAPI.getRedis().set("duels:legacy:games:" + uuid, document.toJson());
        });
    }

    /**
     * Gets the game world.
     * @return Game world.
     */
    public World world() {
        return world;
    }

    public UUID uuid() {
        return uuid;
    }

    public void addSpectator(UUID uuid) {
        spectators.add(uuid);
    }
}