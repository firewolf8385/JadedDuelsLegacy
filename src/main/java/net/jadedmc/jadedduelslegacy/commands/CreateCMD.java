package net.jadedmc.jadedduelslegacy.commands;

import com.cryptomorin.xseries.XMaterial;
import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.GameType;
import net.jadedmc.jadedduelslegacy.game.kit.Kit;
import net.jadedmc.jadedduelslegacy.game.tournament.BestOf;
import net.jadedmc.jadedduelslegacy.game.tournament.EliminationType;
import net.jadedmc.jadedduelslegacy.game.tournament.TeamType;
import net.jadedmc.jadedutils.chat.ChatUtils;
import net.jadedmc.jadedutils.gui.CustomGUI;
import net.jadedmc.jadedutils.items.ItemBuilder;
import net.jadedmc.jadedutils.items.SkullBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class CreateCMD extends AbstractCommand {
    private final JadedDuelsPlugin plugin;

    public CreateCMD(JadedDuelsPlugin plugin) {
        super("create", "tournament.use", false);
        this.plugin = plugin;
    }

    public void execute(CommandSender sender, String[] args) {

        // Make sure no other tournaments are currently running.
        if(plugin.tournamentManager().getHost() != null) {
            ChatUtils.chat(sender, "&c&lError &8» &cThere is already a tournament active!");
            return;
        }

        Player player = (Player) sender;
        plugin.tournamentManager().setHost(player);
        new SetKitGUI().open(player);
    }

    private class SetKitGUI extends CustomGUI {
        public SetKitGUI() {
            super(54, "Select A Kit");
            addFiller(0,1,2,3,4,5,6,7,8,45,46,47,48,49,50,51,52,53);

            int i = 0;
            for(Kit kit : plugin.kitManager().kits()) {
                if(plugin.arenaManager().getArenas(kit, GameType.TOURNAMENT).size() == 0) {
                    continue;
                }

                setItem(i + 9, new ItemBuilder(kit.iconMaterial())
                                .setDisplayName("<green><bold>" + kit.name())
                                .addFlag(ItemFlag.HIDE_ATTRIBUTES)
                                .addLore("").addLore("<green>Click to Select!").build(),
                        (p, a) -> {
                            plugin.tournamentManager().setKit(kit);
                            new SetBracketGUI().open(p);
                        });
                i++;
            }
        }
    }

    private class SetBracketGUI extends CustomGUI {
        public SetBracketGUI() {
            super(54, "Select Bracket");
            addFiller(0,1,2,3,4,5,6,7,8,45,46,47,48,49,50,51,52,53);

            ItemStack singleElim = new SkullBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ2NWNlODNmMWFhNWI2ZTg0ZjliMjMzNTk1MTQwZDViNmJlY2ViNjJiNmQwYzY3ZDFhMWQ4MzYyNWZmZCJ9fX0=")
                    .asItemBuilder()
                    .setDisplayName("<green><bold>Single Elimination")
                    .addLore("").addLore("<green>Click to Select!").build();
            setItem(20, singleElim, (p,a) -> {
                plugin.tournamentManager().setEliminationType(EliminationType.SINGLE_ELIMINATION);
                new SetBestOfGUI().open(p);
            });

            ItemStack doubleElim = new SkullBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGQ1NGQxZjhmYmY5MWIxZTdmNTVmMWJkYjI1ZTJlMzNiYWY2ZjQ2YWQ4YWZiZTA4ZmZlNzU3ZDMwNzVlMyJ9fX0=")
                    .asItemBuilder()
                    .setDisplayName("<green><bold>Double Elimination")
                    .addLore("").addLore("<green>Click to Select!").build();
            setItem(24, doubleElim, (p,a) -> {
                plugin.tournamentManager().setEliminationType(EliminationType.DOUBLE_ELIMINATION);
                new SetBestOfGUI().open(p);
            });
        }
    }

    private class SetBestOfGUI extends CustomGUI {
        public SetBestOfGUI() {
            super(54, "Select Best Of");
            addFiller(0,1,2,3,4,5,6,7,8,45,46,47,48,49,50,51,52,53);

            ItemStack bo1 = new SkullBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ2NWNlODNmMWFhNWI2ZTg0ZjliMjMzNTk1MTQwZDViNmJlY2ViNjJiNmQwYzY3ZDFhMWQ4MzYyNWZmZCJ9fX0=")
                    .asItemBuilder()
                    .setDisplayName("<green><bold>Best of 1")
                    .addLore("").addLore("<green>Click to Select!").build();
            setItem(19, bo1, (p,a) -> {
                plugin.tournamentManager().setBestOf(BestOf.ONE);
                new TeamSizeGUI().open(p);
            });

            ItemStack bo3 = new SkullBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjFlNGVhNTliNTRjYzk5NDE2YmM5ZjYyNDU0OGRkYWMyYTM4ZWVhNmEyZGJmNmU0Y2NkODNjZWM3YWM5NjkifX19")
                    .asItemBuilder()
                    .setDisplayName("<green><bold>Best of 3")
                    .addLore("").addLore("<green>Click to Select!").build();
            setItem(21, bo3, (p,a) -> {
                plugin.tournamentManager().setBestOf(BestOf.THREE);
                new TeamSizeGUI().open(p);
            });

            ItemStack bo5 = new SkullBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODRjOGMzNzEwZGEyNTU5YTI5MWFkYzM5NjI5ZTljY2VhMzFjYTlkM2QzNTg2YmZlYTZlNmUwNjEyNGIzYyJ9fX0=")
                    .asItemBuilder()
                    .setDisplayName("<green><bold>Best of 5")
                    .addLore("").addLore("<green>Click to Select!").build();
            setItem(23, bo5, (p,a) -> {
                plugin.tournamentManager().setBestOf(BestOf.FIVE);
                new TeamSizeGUI().open(p);
            });

            ItemStack bo7 = new SkullBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjRiZGU3OWY4NGZjNWYzZjFmYmM1YmMwMTA3MTA2NmJkMjBjZDI2M2ExNjU0ZDY0ZDYwZDg0MjQ4YmE5Y2QifX19")
                    .asItemBuilder()
                    .setDisplayName("<green><bold>Best of 7")
                    .addLore("").addLore("<green>Click to Select!").build();
            setItem(25, bo7, (p,a) -> {

                plugin.tournamentManager().setBestOf(BestOf.SEVEN);
                new TeamSizeGUI().open(p);
            });
        }
    }

    private class HostPlayingGUI extends CustomGUI {
        public HostPlayingGUI() {
            super(54, "Are you playing?");

            addFiller(0,1,2,3,4,5,6,7,8,45,46,47,48,49,50,51,52,53);

            ItemStack yes = new ItemBuilder(XMaterial.GREEN_WOOL)
                    .setDisplayName("<green><bold>Yes")
                    .addLore("<green>I am playing.")
                    .build();
            setItem(20, yes, (p,a) -> {
                plugin.tournamentManager().setHostPlaying(true);
                p.closeInventory();
                plugin.tournamentManager().announceCreate();
            });

            ItemStack no = new ItemBuilder(XMaterial.RED_WOOL)
                    .setDisplayName("<red><bold>No")
                    .addLore("<red>I am not playing.")
                    .build();
            setItem(24, no, (p,a) -> {
                plugin.tournamentManager().setHostPlaying(false);
                p.closeInventory();
                plugin.tournamentManager().announceCreate();
            });
        }
    }

    private class TeamSizeGUI extends CustomGUI {
        public TeamSizeGUI() {
            super(54, "Select a Team Size");

            ItemStack one = new SkullBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmQ2NWNlODNmMWFhNWI2ZTg0ZjliMjMzNTk1MTQwZDViNmJlY2ViNjJiNmQwYzY3ZDFhMWQ4MzYyNWZmZCJ9fX0=")
                    .asItemBuilder()
                    .setDisplayName("<green><bold>1v1")
                    .build();
            setItem(20, one, (p,a) -> {
                plugin.tournamentManager().setTeamType(TeamType.ONE_V_ONE);
                new HostPlayingGUI().open(p);
            });

            ItemStack two = new SkullBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZGQ1NGQxZjhmYmY5MWIxZTdmNTVmMWJkYjI1ZTJlMzNiYWY2ZjQ2YWQ4YWZiZTA4ZmZlNzU3ZDMwNzVlMyJ9fX0=")
                    .asItemBuilder()
                    .setDisplayName("<green><bold>2v2 Random")
                    .build();
            setItem(22, two, (p,a) -> {
                plugin.tournamentManager().setTeamType(TeamType.TWO_V_TWO_RANDOM);
                new HostPlayingGUI().open(p);
            });

            ItemStack three = new SkullBuilder("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjFlNGVhNTliNTRjYzk5NDE2YmM5ZjYyNDU0OGRkYWMyYTM4ZWVhNmEyZGJmNmU0Y2NkODNjZWM3YWM5NjkifX19")
                    .asItemBuilder()
                    .setDisplayName("<green><bold>3v3 Random")
                    .build();
            setItem(24, three, (p,a) -> {
                plugin.tournamentManager().setTeamType(TeamType.THREE_V_THREE_RANDOM);
                new HostPlayingGUI().open(p);
            });
        }
    }
}