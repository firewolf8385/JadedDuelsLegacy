package net.jadedmc.jadedduelslegacy.game;

import net.jadedmc.jadedcore.JadedAPI;
import net.jadedmc.jadedduelslegacy.game.teams.Team;
import net.jadedmc.jadedduelslegacy.utils.GameUtils;
import net.jadedmc.jadedutils.DateUtils;
import net.jadedmc.jadedutils.scoreboard.CustomScoreboard;
import net.jadedmc.jadedutils.scoreboard.ScoreHelper;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameScoreboard extends CustomScoreboard {
    private final Game game;

    public GameScoreboard(Player player, Game game) {
        super(player);
        this.game = game;
        update(player);
    }

    public void update(Player player) {
        ScoreHelper helper;

        if(ScoreHelper.hasScore(player)) {
            helper = ScoreHelper.getByPlayer(player);
        }
        else {
            helper = ScoreHelper.createScore(player);
        }

        switch (game.gameType()) {
            default -> {
                List<Player> opponents = new ArrayList<>();

                Team team = game.teamManager().team(player);

                for(Team opposingTeam : game.teamManager().teams()) {
                    if(team.equals(opposingTeam)) {
                        continue;
                    }

                    opponents.addAll(opposingTeam.players());
                }

                if(game.gameType() == GameType.TOURNAMENT) {
                    helper.setTitle("&a&lTournament");
                }
                else {
                    helper.setTitle("&a&lDuels");
                }

                helper.setSlot(15, "&7" + DateUtils.currentDateToString() + " &8" + JadedAPI.getCurrentInstance().getName());
                helper.setSlot(14, "");
                helper.setSlot(13, "&aTime: &f" + game.timer().toString());
                helper.setSlot(12, "");
                helper.setSlot(11, "&aKit:");
                helper.setSlot(10, "  &f" + game.kit().name());
                helper.setSlot(9, "");

                int slot = 7;
                if(opponents.size() < 3 && opponents.size() > 0) {
                    if(opponents.size() == 1) {
                        helper.setSlot(8, "&aOpponent:");
                    }
                    else {
                        helper.setSlot(8, "&aOpponents:");
                    }

                    for(Player opponent : opponents) {
                        helper.setSlot(slot, "  " + game.teamManager().team(opponent).teamColor().chatColor()  + opponent.getName());
                        slot--;

                        if(game.teamManager().team(opponent).deadPlayers().contains(opponent)) {
                            //helper.setSlot(slot, "  &c0% &7- " + GameUtils.getFormattedPing(opponent));
                            helper.setSlot(slot, "  &c0%");
                        }
                        else {
                            //helper.setSlot(slot, "  " + GameUtils.getFormattedHealth(opponent) + " &7- " + GameUtils.getFormattedPing(opponent));
                            helper.setSlot(slot, "  " + GameUtils.getFormattedHealth(opponent));
                        }

                        slot--;
                    }
                }
                else {
                    helper.setSlot(8, "&aOpponents: &f" + opponents.size());
                }

                for(int i = 3; i < slot; i++) {
                    helper.removeSlot(i);
                }

                helper.setSlot(2, "");
                helper.setSlot(1, "&aplay.jadedmc.net");
            }
        }
    }
}