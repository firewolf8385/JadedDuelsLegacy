package net.jadedmc.jadedduelslegacy.game.tournament;

import at.stefangeyer.challonge.Challonge;
import at.stefangeyer.challonge.exception.DataAccessException;
import at.stefangeyer.challonge.model.Credentials;
import at.stefangeyer.challonge.model.Match;
import at.stefangeyer.challonge.model.Participant;
import at.stefangeyer.challonge.model.Tournament;
import at.stefangeyer.challonge.model.enumeration.MatchState;
import at.stefangeyer.challonge.model.enumeration.TournamentType;
import at.stefangeyer.challonge.model.query.MatchQuery;
import at.stefangeyer.challonge.model.query.ParticipantQuery;
import at.stefangeyer.challonge.model.query.TournamentQuery;
import at.stefangeyer.challonge.rest.RestClient;
import at.stefangeyer.challonge.rest.retrofit.RetrofitRestClient;
import at.stefangeyer.challonge.serializer.Serializer;
import at.stefangeyer.challonge.serializer.gson.GsonSerializer;
import net.jadedmc.jadedcore.JadedAPI;
import net.jadedmc.jadedcore.minigames.Minigame;
import net.jadedmc.jadedcore.networking.player.NetworkPlayer;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.GameType;
import net.jadedmc.jadedduelslegacy.game.arena.Arena;
import net.jadedmc.jadedduelslegacy.game.kit.Kit;
import net.jadedmc.jadedduelslegacy.utils.MapUtils;
import net.jadedmc.jadedutils.chat.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class TournamentManager {
    private final JadedDuelsPlugin plugin;
    private final Challonge challonge;
    private Tournament tournament = null;
    private String host = null;
    private TeamType teamType = TeamType.NONE;
    private EliminationType eliminationType = EliminationType.NONE;
    private Kit kit = null;
    private final List<TournamentTeam> teams = new ArrayList<>();
    boolean  hostPlaying = true;
    private int taskID;
    private BestOf bestOf = BestOf.NONE;
    private String url = null;

    public TournamentManager(final JadedDuelsPlugin plugin) {
        this.plugin = plugin;

        // Connects to Challonge
        Credentials credentials = new Credentials(plugin.settingsManager().getConfig().getString("challonge.username"), plugin.settingsManager().getConfig().getString("challonge.api-key"));
        Serializer serializer = new GsonSerializer();
        RestClient restClient = new RetrofitRestClient();
        challonge = new Challonge(credentials, serializer, restClient);
    }

    public void broadcastMessage(String message) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> JadedAPI.getRedis().publish("tournament", "broadcast " + message));
    }

    public Challonge getChallonge() {
        return challonge;
    }

    public EliminationType getEliminationType() {
        return eliminationType;
    }

    public String getHost() {
        return host;
    }

    public Kit getKit() {
        return kit;
    }

    public String getURL() {
        return url;
    }

    public TeamType getTeamType() {
        return teamType;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setupTournament(String host, Kit kit, TeamType teamType, EliminationType eliminationType, BestOf bestOf) {
        this.host = host;
        this.kit = kit;
        this.teamType = teamType;
        this.eliminationType = eliminationType;
        this.bestOf = bestOf;
    }

    public void createTournament() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Create the tournament.
                {
                    TournamentQuery.TournamentQueryBuilder builder = TournamentQuery.builder();
                    builder.name(host + "\'s " + kit.name() + " Tournament")
                            .gameName("Minecraft")
                            .description(kit.name() + " tournament on JadedMC. Join us at play.jadedmc.net")
                            .holdThirdPlaceMatch(true);

                    // Sets the tournament type of the tournament.
                    switch (eliminationType) {
                        case SINGLE_ELIMINATION -> builder.tournamentType(TournamentType.SINGLE_ELIMINATION);
                        case DOUBLE_ELIMINATION -> builder.tournamentType(TournamentType.DOUBLE_ELIMINATION);
                    }
                    tournament = challonge.createTournament(builder.build());
                    url = tournament.getUrl();
                }

                // Add players to the tournament.
                {
                    List<ParticipantQuery> queries = new ArrayList<>();
                    List<NetworkPlayer> waitingPlayers = new ArrayList<>(JadedAPI.getPlayers(Minigame.TOURNAMENTS_LEGACY).values());
                    Collections.shuffle(waitingPlayers);

                    // Remove the host from the waiting players if they are not playing.
                    if(!hostPlaying) {
                        waitingPlayers.removeIf(player -> host.equalsIgnoreCase(player.getName()));
                    }

                    // Create teams.
                    switch (teamType) {
                        case ONE_V_ONE -> waitingPlayers.forEach(this::createTeam);

                        case TWO_V_TWO_RANDOM -> {

                            while (waitingPlayers.size() != 0) {
                                if (waitingPlayers.size() >= 2) {
                                    NetworkPlayer one = waitingPlayers.get(0);
                                    NetworkPlayer two = waitingPlayers.get(1);

                                    waitingPlayers.remove(one);
                                    waitingPlayers.remove(two);

                                    List<NetworkPlayer> team = new ArrayList<>();
                                    team.add(one);
                                    team.add(two);
                                    createTeam(team);
                                }
                                else {
                                    NetworkPlayer one = waitingPlayers.get(0);

                                    waitingPlayers.remove(one);
                                    createTeam(one);
                                }
                            }
                        }

                        case THREE_V_THREE_RANDOM -> {
                            while (waitingPlayers.size() != 0) {

                                if (waitingPlayers.size() >= 3) {
                                    NetworkPlayer one = waitingPlayers.get(0);
                                    NetworkPlayer two = waitingPlayers.get(1);
                                    NetworkPlayer three = waitingPlayers.get(2);

                                    waitingPlayers.remove(one);
                                    waitingPlayers.remove(two);
                                    waitingPlayers.remove(three);

                                    List<NetworkPlayer> team = new ArrayList<>();
                                    team.add(one);
                                    team.add(two);
                                    team.add(three);
                                    createTeam(team);
                                }
                                else if(waitingPlayers.size() == 2) {
                                    NetworkPlayer one = waitingPlayers.get(0);
                                    NetworkPlayer two = waitingPlayers.get(1);

                                    waitingPlayers.remove(one);
                                    waitingPlayers.remove(two);

                                    List<NetworkPlayer> team = new ArrayList<>();
                                    team.add(one);
                                    team.add(two);
                                    createTeam(team);
                                }
                                else {
                                    NetworkPlayer one = waitingPlayers.get(0);

                                    waitingPlayers.remove(one);
                                    createTeam(one);
                                }
                            }
                        }
                    }

                    // Create challonge participant queries.
                    teams.forEach(team -> queries.add(ParticipantQuery.builder().name(team.name()).build()));

                    // Update challonge participant ids.
                    List<Participant> participants = challonge.bulkAddParticipants(tournament, queries);
                    for(Participant participant : participants) {
                        team(participant.getName()).challongeID(participant.getId());
                    }
                }
            }
            catch (DataAccessException exception) {
                ChatUtils.broadcast("&c&lError &8» &cSomething went wrong while creating the tournament! Check console for details.");
                exception.printStackTrace();
                return;
            }

            // Start the tournament.
            JadedAPI.getRedis().publish("tournament", "start " + url);
            startTournament();
        });
    }

    public TournamentTeam createTeam(NetworkPlayer player) {
        TournamentTeam team = new TournamentTeam();
        team.addPlayer(player);
        teams.add(team);
        return team;
    }

    public TournamentTeam createTeam(List<NetworkPlayer> players) {
        TournamentTeam team = new TournamentTeam();
        players.forEach(team::addPlayer);
        teams.add(team);
        return team;
    }

    public TournamentTeam team(Long challongeID) {
        for(TournamentTeam team : teams) {
            if(team.challongeID().equals(challongeID)) {
                return team;
            }
        }

        return null;
    }

    public TournamentTeam team(String name) {
        for(TournamentTeam team : teams) {
            if(team.name().equals(name)) {
                return team;
            }
        }

        return null;
    }

    public void startTournament() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            // Attempts to start the tournament through challonge.
            boolean started = false;
            while(!started) {
                try {
                    challonge.startTournament(tournament);
                    started = true;
                }
                catch (DataAccessException exception) {
                    exception.printStackTrace();
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException exception2) {
                        exception2.printStackTrace();
                    }
                }
            }

            // Broadcast start message.
            String stringBuilder = "<dark_gray><st>+-----------------------***-----------------------+</st><newline>" +
                    ChatUtils.centerText("&a&l" + host + "'s Tournament") + "</bold><newline>" +
                    "<newline>" +
                    ChatUtils.centerText("<green>Kit: <white>" + kit.name()) + "<newline>" +
                    ChatUtils.centerText("<green>Format: <white>" + eliminationType.getName()) + "<newline>" +
                    ChatUtils.centerText("&aTeams: &f" + teamType.displayName() + " &7(" + bestOf.getName() + "&7)") + "<newline>" +
                    "<newline>" +
                    ChatUtils.centerText("<green>Bracket: <white><click:open_url:'https://www.challonge.com/" + tournament.getUrl() + "'>https://challonge.com/" + tournament.getUrl() + "</click>") + "<newline>" +
                    "<newline>" +
                    "<dark_gray><st>+-----------------------***-----------------------+</st>";
            broadcastMessage(stringBuilder);
        });

        taskID = plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Collection<NetworkPlayer> potentialPlayers = new ArrayList<>();
            potentialPlayers.addAll(JadedAPI.getPlayers(Minigame.TOURNAMENTS_MODERN).values());
            potentialPlayers.addAll(JadedAPI.getPlayers(Minigame.TOURNAMENTS_LEGACY).values());

            int games = 0;
            boolean matchesObtained = false;
            while(!matchesObtained) {
                try {

                    // Gets all matches that aren't complete.
                    List<Match> matches = new ArrayList<>();
                    for(Match match : challonge.getMatches(tournament)) {
                        if (match.getState() == MatchState.COMPLETE) {
                            continue;
                        }
                        matches.add(match);
                    }

                    // Ends the event if there are no matches left.
                    if(matches.size() == 0) {
                        stopTournament();
                        return;
                    }

                    for(Match match : matches) {
                        // Makes sure the match hasn't already been started.
                        if (match.getUnderwayAt() != null) {
                            continue;
                        }

                        // Makes sure match has 2 waiting players.
                        if (match.getPlayer1Id() == null || match.getPlayer2Id() == null) {
                            continue;
                        }

                        TournamentTeam team1 = team(match.getPlayer1Id());
                        TournamentTeam team2 = team(match.getPlayer2Id());
                        List<UUID> team1Players = new ArrayList<>();
                        List<UUID> team2Players = new ArrayList<>();

                        for (UUID player : team1.players()) {
                            for (NetworkPlayer networkPlayer : potentialPlayers) {
                                if (networkPlayer.getUniqueId().equals(player)) {
                                    team1Players.add(player);
                                    break;
                                }
                            }
                        }

                        for (UUID player : team2.players()) {
                            for (NetworkPlayer networkPlayer : potentialPlayers) {
                                if (networkPlayer.getUniqueId().equals(player)) {
                                    team2Players.add(player);
                                    break;
                                }
                            }
                        }

                        // Check that both teams are online.
                        if (team1.players().size() == 0) {
                            match.setForfeited(true);
                            match.setWinnerId(team2.challongeID());

                            MatchQuery query = MatchQuery.builder()
                                    .winnerId(team2.challongeID())
                                    .scoresCsv("0-" + bestOf.neededWins())
                                    .build();
                            challonge.updateMatch(match, query);
                            continue;
                        } else if (team2.players().size() == 0) {
                            match.setForfeited(true);
                            match.setWinnerId(team1.challongeID());

                            MatchQuery query = MatchQuery.builder()
                                    .winnerId(team1.challongeID())
                                    .scoresCsv(bestOf.neededWins() + "-0")
                                    .build();
                            challonge.updateMatch(match, query);
                            continue;
                        }

                        // Tell challonge that the match is underway.
                        boolean sent = false;
                        while (!sent) {
                            try {
                                challonge.markMatchAsUnderway(match);
                                sent = true;
                                games++;
                                Thread.sleep(1000);
                            }
                            catch (DataAccessException | InterruptedException exception) {
                                exception.printStackTrace();
                            }
                        }

                        // Find a random arena.
                        List<Arena> arenas = new ArrayList<>(plugin.arenaManager().getArenas(kit, GameType.TOURNAMENT));
                        Collections.shuffle(arenas);

                        // Start the game
                        plugin.gameManager().createGame(arenas.get(0), kit, GameType.TOURNAMENT, bestOf.neededWins(), tournament.getUrl(), match.getId(), team1.challongeID(), team2.challongeID(), team1Players, team2Players);
                    }

                    // Mark that all matches have been obtained.
                    matchesObtained = true;
                }
                catch (DataAccessException exception) {
                    exception.printStackTrace();

                    // Wait before trying again.
                    try {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        }, 200, 200).getTaskId();
    }

    public void stopTournament() {
        // Cancels the repeating task.
        Bukkit.getScheduler().cancelTask(taskID);

        // Finalizes the tournament
        boolean finished = false;
        while(!finished) {
            try {
                challonge.finalizeTournament(tournament);
                finished = true;
            }
            catch (DataAccessException exception) {
                exception.printStackTrace();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Loads all participants in the tournament.
        Map<Participant, Integer> results = new HashMap<>();
        List<Participant> participants = new ArrayList<>();
        boolean sent = false;
        while(!sent) {
            try {
                participants.addAll(challonge.getParticipants(tournament));
                sent = true;
            }
            catch (DataAccessException exception) {
                exception.printStackTrace();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Stores all participant's final rank.
        for(Participant participant : participants) {
            results.put(participant, participant.getFinalRank());
        }

        // Sorts the results to get final rankings.
        Map<Participant, Integer> rankings = MapUtils.sortByValue(results);
        List<Participant> top = new ArrayList<>(rankings.keySet());

        // Display the end message to all players in the tournament.
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<dark_gray><st>+-----------------------***-----------------------+</st><newline>");
        stringBuilder.append(ChatUtils.centerText("&a&lTournament")).append("</bold><newline>");
        stringBuilder.append(ChatUtils.centerText("&aKit: &f" + kit.name())).append("<newline>");
        stringBuilder.append("<newline>");
        stringBuilder.append(ChatUtils.centerText("&6&l1st: &f" + top.get(0).getName())).append("<newline>");
        stringBuilder.append(ChatUtils.centerText("&f&l2nd: &f" + top.get(1).getName())).append("<newline>");

        if(top.size() >= 3) {
            stringBuilder.append(ChatUtils.centerText("&c&l3rd: &f" + top.get(2).getName())).append("<newline>");
        }
        else {
            stringBuilder.append(ChatUtils.centerText("&c&l3rd: &fNone")).append("<newline>");
        }

        stringBuilder.append("<newline>");
        stringBuilder.append(ChatUtils.centerText("<green>Bracket: <white><click:open_url:'https://www.challonge.com/" + tournament.getUrl() + "'>https://challonge.com/" + tournament.getUrl() + "</click>")).append("<newline>");
        stringBuilder.append("<dark_gray><st>+-----------------------***-----------------------+</st>");

        broadcastMessage(stringBuilder.toString());

        JadedAPI.getRedis().publish("tournament", "clear");
    }

    public void reset() {
        this.host = null;
        this.kit = null;
        this.teamType = TeamType.NONE;
        this.eliminationType = EliminationType.NONE;
        this.bestOf = BestOf.NONE;
        teams.clear();
    }

    public void setHost(Player host) {
        this.host = host.getName();
    }

    public void setBestOf(BestOf bestOf) {
        this.bestOf = bestOf;
    }

    public void setTeamType(TeamType teamType) {
        this.teamType = teamType;
    }

    public void setEliminationType(EliminationType eliminationType) {
        this.eliminationType = eliminationType;
    }

    public void setHostPlaying(boolean hostPlaying) {
        this.hostPlaying = hostPlaying;
    }

    public void setKit(Kit kit) {
        this.kit = kit;
    }

    public void announceCreate() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {

            String host = this.host;
            String kit = this.kit.id();
            String teamType = this.teamType.toString();
            String eliminationType = this.eliminationType.toString();
            String bestOf = this.bestOf.toString();

            JadedAPI.getRedis().publish("tournament", "create " + host + " " + kit + " " + teamType + " " + eliminationType + " " + bestOf);
        });
    }
}