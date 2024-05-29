package net.jadedmc.jadedduelslegacy.queue;

import net.jadedmc.jadedcore.JadedAPI;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedutils.DateUtils;
import net.jadedmc.jadedutils.scoreboard.CustomScoreboard;
import net.jadedmc.jadedutils.scoreboard.ScoreHelper;
import org.bukkit.entity.Player;

public class QueueScoreboard extends CustomScoreboard {
    private final JadedDuelsPlugin plugin;
    private final Player player;
    public QueueScoreboard(JadedDuelsPlugin plugin, Player player) {
        super(player);
        this.plugin = plugin;
        this.player = player;
        update(player);
    }

    @Override
    public void update(Player player) {
        ScoreHelper helper;

        if(ScoreHelper.hasScore(player)) {
            helper = ScoreHelper.getByPlayer(player);
        }
        else {
            helper = ScoreHelper.createScore(player);
        }

        helper.setTitle("&a&lDuels");
        helper.setSlot(15, "&7" + DateUtils.currentDateToString() + " &8" + JadedAPI.getCurrentInstance().getName());
        helper.setSlot(14, "");
        helper.setSlot(13, "&aQueue:");
        helper.setSlot(12, "  &aKit: &f" + plugin.queueManager().getKit(player).name());
        helper.setSlot(11, "  &aTime: &f" + plugin.queueManager().getTimer(player).toString());
        helper.removeSlot(10);
        helper.removeSlot(9);
        helper.removeSlot(8);
        helper.removeSlot(7);
        helper.removeSlot(6);
        helper.removeSlot(5);
        helper.removeSlot(4);
        helper.removeSlot(3);
        helper.setSlot(2, "");
        helper.setSlot(1, "&aplay.jadedmc.net");
    }
}