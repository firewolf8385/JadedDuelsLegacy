package net.jadedmc.jadedduelslegacy.commands;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.arena.Arena;
import net.jadedmc.jadedutils.chat.ChatUtils;
import org.bukkit.command.CommandSender;

/**
 * This class runs the /arenas command, which displays all currently available arenas.
 */
public class ArenasCMD extends AbstractCommand {
    private final JadedDuelsPlugin plugin;

    /**
     * Creates the command.
     * @param plugin Instance of the plugin.
     */
    public ArenasCMD(JadedDuelsPlugin plugin) {
        super("arenas", "duels.admin", true);
        this.plugin = plugin;
    }

    /**
     * Executes the command.
     * @param sender The Command Sender.
     * @param args Arguments of the command.
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        ChatUtils.chat(sender, "<green><bold>Duels</bold> <dark_gray>» <green>Currently Loaded Arenas:");

        // Display all active arenas.
        for(Arena arena : plugin.arenaManager().getArenas()) {
            ChatUtils.chat(sender, "  <dark_gray>➤ <gray>" + arena.fileName()) ;
        }
    }
}