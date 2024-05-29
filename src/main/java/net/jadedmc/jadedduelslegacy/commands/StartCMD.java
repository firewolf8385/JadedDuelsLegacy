package net.jadedmc.jadedduelslegacy.commands;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedutils.chat.ChatUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StartCMD extends AbstractCommand {
    private final JadedDuelsPlugin plugin;

    public StartCMD(JadedDuelsPlugin plugin) {
        super("start", "tournament.use", false);

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        // Make sure the host is the one running the command.
        if(!player.getName().equalsIgnoreCase(plugin.tournamentManager().getHost())) {
            ChatUtils.chat(sender, "&cError &8» &cOnly the host can run that command!!");
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.tournamentManager().broadcastMessage("&a&lTournament &8» &aGenerating Brackets");
            plugin.tournamentManager().createTournament();
        });
    }
}