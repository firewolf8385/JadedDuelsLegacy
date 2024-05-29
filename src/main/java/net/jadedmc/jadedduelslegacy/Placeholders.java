package net.jadedmc.jadedduelslegacy;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.teams.Team;
import net.jadedmc.jadedduelslegacy.utils.GameUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * This class will be registered through the register-method in the
 * plugins onEnable-method.
 */
class Placeholders extends PlaceholderExpansion {
    private final JadedDuelsPlugin plugin;

    /**
     * Since we register the expansion inside our own plugin, we
     * can simply use this method here to get an instance of our
     * plugin.
     *
     * @param plugin
     *        The instance of our plugin.
     */
    public Placeholders(final JadedDuelsPlugin plugin){
        this.plugin = plugin;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }

    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }

    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public @NotNull String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public @NotNull String getIdentifier(){
        return "duels";
    }

    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convenience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public @NotNull String getVersion(){
        return plugin.getDescription().getVersion();
    }


    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        if(identifier.equals("prefix")) {
            Game game = plugin.gameManager().game(player);

            if(game == null) {
                return PlaceholderAPI.setPlaceholders(player, "%jadedcore_rank_chat_prefix_legacy%&7");
            }

            if(game.spectators().contains(player)) {
                return "&7[SPEC] ";
            }

            Team team = game.teamManager().team(player);
            return team.teamColor().chatColor() + "[" + team.teamColor().abbreviation() + "] ";
        }

        if(identifier.equals("team_prefix")) {
            Game game = plugin.gameManager().game(player);

            if(game == null) {
                return null;
            }

            if(game.spectators().contains(player)) {
                return "&7[SPEC]";
            }

            Team team = game.teamManager().team(player);
            return team.teamColor().chatColor() + "[" + team.teamColor().displayName() + "]";
        }

        if(identifier.equals("health")) {
            return GameUtils.getFormattedHealth(player);
        }


        switch (identifier) {
            case "game_team" -> {
                Game game = plugin.gameManager().game(player);

                if(game == null) {
                    return "";
                }

                Team team = game.teamManager().team(player);

                if(team == null) {
                    return "team99";
                }

                return "team" + team.id();
            }
        }

        return null;
    }
}