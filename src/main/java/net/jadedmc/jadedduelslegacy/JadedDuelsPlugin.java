package net.jadedmc.jadedduelslegacy;

import net.jadedmc.jadedchat.JadedChat;
import net.jadedmc.jadedchat.features.channels.channel.ChatChannel;
import net.jadedmc.jadedchat.features.channels.channel.ChatChannelBuilder;
import net.jadedmc.jadedchat.features.channels.fomat.ChatFormatBuilder;
import net.jadedmc.jadedcore.JadedAPI;
import net.jadedmc.jadedduelslegacy.commands.AbstractCommand;
import net.jadedmc.jadedduelslegacy.game.GameManager;
import net.jadedmc.jadedduelslegacy.game.arena.ArenaManager;
import net.jadedmc.jadedduelslegacy.game.kit.KitManager;
import net.jadedmc.jadedduelslegacy.game.tournament.TournamentManager;
import net.jadedmc.jadedduelslegacy.listeners.*;
import net.jadedmc.jadedduelslegacy.queue.QueueManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class JadedDuelsPlugin extends JavaPlugin {
    private ArenaManager arenaManager;
    private GameManager gameManager;
    private KitManager kitManager;
    private SettingsManager settingsManager;
    private TournamentManager tournamentManager;
    private QueueManager queueManager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        settingsManager = new SettingsManager(this);
        kitManager = new KitManager(this);

        // Load arenas.
        arenaManager = new ArenaManager(this);
        arenaManager.loadArenas();

        gameManager = new GameManager(this);
        queueManager = new QueueManager(this);
        tournamentManager = new TournamentManager(this);

        AbstractCommand.registerCommands(this);

        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockExplodeListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockFormListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockFromToListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new ChannelMessageSendListener(this), this);
        getServer().getPluginManager().registerEvents(new ChannelSwitchListener(this), this);
        getServer().getPluginManager().registerEvents(new EntitySpawnListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(this), this);
        //getServer().getPluginManager().registerEvents(new EntityExplodeListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityRegainHealthListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new FoodLevelChangeListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDropItemListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerEggThrowListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerToggleFlightListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerToggleSneakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        //getServer().getPluginManager().registerEvents(new ProjectileHitListener(this), this);
        getServer().getPluginManager().registerEvents(new ProjectileLaunchListener(this), this);
        getServer().getPluginManager().registerEvents(new RedisMessageListener(this), this);
        //getServer().getPluginManager().registerEvents(new TNTPrimeListener(this), this);
        getServer().getPluginManager().registerEvents(new VehicleDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new VehicleExitListener(this), this);

        getServer().getPluginManager().registerEvents(new JadedJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new LobbyJoinListener(this), this);

        // Create Chat Channels
        if(!JadedChat.channelExists("GAME")) {
            ChatChannel gameChannel = new ChatChannelBuilder("GAME")
                    .setDisplayName("<green>GAME</green>")
                    .useDiscordSRV(true)
                    .addChatFormat(new ChatFormatBuilder("default")
                            .addSection("channel", "<green>[GAME]</green> ")
                            .addSection("team", "%duels_team_prefix% ")
                            .addSection("prefix", "%jadedcore_rank_chat_prefix%")
                            .addSection("player", "<gray>%player_name%")
                            .addSection("seperator", "<dark_gray> » ")
                            .addSection("message", "<gray><message>")
                            .build())
                    .build();
            gameChannel.saveToFile("game.yml");
            JadedChat.loadChannel(gameChannel);
        }

        if(!JadedChat.channelExists("TEAM")) {
            ChatChannel gameChannel = new ChatChannelBuilder("TEAM")
                    .setDisplayName("<white>TEAM</white>")
                    .addAlias("T")
                    .addAlias("TC")
                    .addChatFormat(new ChatFormatBuilder("default")
                            .addSection("channel", "<white>[TEAM]</white> ")
                            .addSection("team", "%duels_team_prefix% ")
                            .addSection("prefix", "%jadedcore_rank_chat_prefix%")
                            .addSection("player", "<gray>%player_name%")
                            .addSection("seperator", "<dark_gray> » ")
                            .addSection("message", "<gray><message>")
                            .build())
                    .build();
            gameChannel.saveToFile("team.yml");
            JadedChat.loadChannel(gameChannel);
        }

        if(!JadedChat.channelExists("TOURNAMENT")) {
            ChatChannel tournamentChannel = new ChatChannelBuilder("TOURNAMENT")
                    .setDisplayName("<red>Tournament</red>")
                    .addChatFormat(new ChatFormatBuilder("default")
                            .addSection("prefix", "%jadedcore_rank_chat_prefix%")
                            .addSection("player", "<gray>%player_name%")
                            .addSection("seperator", "<dark_gray> » ")
                            .addSection("message", "<gray><message>")
                            .build())
                    .build();
            tournamentChannel.saveToFile("tournament.yml");
            JadedChat.loadChannel(tournamentChannel);
        }

        // Registers the game creation channel.
        JadedAPI.getRedis().subscribe("duels_legacy", "tournament");

        // Register placeholders
        new Placeholders(this).register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    /**
     * Gets the Arena Manager.
     * @return ArenaManager.
     */
    public ArenaManager arenaManager() {
        return arenaManager;
    }

    /**
     * Gets the Game Manager.
     * @return GameManager.
     */
    public GameManager gameManager() {
        return gameManager;
    }

    /**
     * Gets the Kit Manager.
     * @return KitManager.
     */
    public KitManager kitManager() {
        return kitManager;
    }

    public QueueManager queueManager() {
        return queueManager;
    }

    /**
     * Gets the Settings Manager.
     * @return SettingsManager.
     */
    public SettingsManager settingsManager() {
        return settingsManager;
    }

    public TournamentManager tournamentManager() {
        return tournamentManager;
    }
}
