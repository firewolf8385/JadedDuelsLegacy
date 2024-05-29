package net.jadedmc.jadedduelslegacy.game.kit;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedduelslegacy.game.GameScoreboard;
import net.jadedmc.jadedutils.scoreboard.CustomScoreboard;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;

import java.util.*;

/**
 * Stores all information about a kit.
 */
public class Kit {
    private final JadedDuelsPlugin plugin;

    // Kit metadata
    private final String name;
    private final String id;
    private Material iconMaterial = Material.WOOD_SWORD;

    // Maps
    private final Map<Integer, ItemStack> items = new HashMap<>();
    private final List<PotionEffect> potionEffects = new ArrayList<>();
    private final List<Material> breakableBlocks = new ArrayList<>();

    // Settings
    private GameMode gameMode = GameMode.ADVENTURE;
    private double maxHealth = 20.0;
    private double startingHealth = 20.0;
    private int startingHunger = 20;
    private float startingSaturation = 10;
    private boolean hunger = true;
    private boolean exitVehicle = true;
    private int voidLevel = 0;
    private boolean naturalRegeneration = true;
    private boolean boxingDamage = false;
    private boolean build = false;
    private boolean takeDamage = true;
    private boolean rangedDamage = false;
    private double maxDamage = -1;
    private boolean waterKills = false;
    private boolean dropItems = true;

    /**
     * Create a kit.
     * @param name Name of the kit.
     */
    public Kit(final JadedDuelsPlugin plugin, final String id, final String name) {
        this.plugin = plugin;
        this.id = id;
        this.name = name;

        breakableBlocks.add(Material.FIRE);
    }

    /**
     * Add a material to the breakable blocks list.
     * @param material Material the players should be able to break.
     */
    public void addBreakableBlock(Material material) {
        this.breakableBlocks.add(material);
    }

    /**
     * Add an item to the kit.
     * @param slot Slot item is in.
     * @param item Item to add.
     */
    public void addItem(int slot, ItemStack item) {
        items.put(slot, item);
    }

    /**
     * Add a potion effect to the kit.
     * @param effect Potion effect to add.
     */
    public void addPotionEffect(PotionEffect effect) {
        potionEffects.add(effect);
    }

    /**
     * Apply a kit to a player.
     * @param player Player to apply kit to.
     */
    public void apply(Game game, Player player) {
        // Clear inventory.
        player.closeInventory();
        player.getInventory().clear();
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        // Clear potion effects.
        player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));

        // Give items
        for(int slot : items.keySet()) {
            ItemStack item = items.get(slot);
            ItemMeta meta = item.getItemMeta();

            if(meta instanceof LeatherArmorMeta dyable) {
                dyable.setColor(game.teamManager().team(player).teamColor().leatherColor());
                item.setItemMeta(meta);
            }

            player.getInventory().setItem(slot, items.get(slot));
        }

        // Set game mode/health/hunger/saturation.
        player.setGameMode(gameMode);
        player.setMaxHealth(maxHealth);

        if(startingHealth > maxHealth) {
            startingHealth = maxHealth;
        }

        player.setHealth(startingHealth);
        player.setFoodLevel(startingHunger);
        player.setSaturation(startingSaturation);

        // Apply potion effects to the kit.
        for(PotionEffect effect : potionEffects) {
            player.addPotionEffect(effect);
        }

        // Run kit-specific kit apply code.
        onKitApply(plugin.gameManager().game(player), player);
    }

    /**
     * Get all materials the players should be able to break.
     * @return Breakable materials.
     */
    public Collection<Material> breakableBlocks() {
        return this.breakableBlocks;
    }

    /**
     * Get if the kit should use boxing damage.
     * @return Whether the kit should use boxing damage.
     */
    public boolean boxingDamage() {
        return boxingDamage;
    }

    /**
     * Set if the kit should use boxing damage.
     * @param boxingDamage Whether the kit should use boxing damage.
     */
    public void boxingDamage(boolean boxingDamage) {
        this.boxingDamage = boxingDamage;
    }

    /**
     * Get if the kit should allow breaking the terrain.
     * @return Whether players should be able to break terrain.
     */
    public boolean build() {
        return build;
    }

    /**
     * Set if the kit should allow breaking the terrain.
     * @param build Whether the kit allows building.
     */
    public void build(boolean build) {
        this.build = build;
    }

    /**
     * Get if items should be dropped from blocks.
     * @return Whether items drop from blocks.
     */
    public boolean dropItems() {
        return dropItems;
    }

    /**
     * Change if items drop from blocks.
     * @param dropItems Whether items should drop from blocks.
     */
    public void dropItems(boolean dropItems) {
        this.dropItems = dropItems;
    }

    /**
     * Get if the player should be allowed to exit their vehicles.
     * @return If the player can exit their vehicles.
     */
    public boolean exitVehicle() {
        return exitVehicle;
    }

    /**
     * Change if players should be allowed to exit their vehicles.
     * @param exitVehicle If the player can exit their vehicles.
     */
    public void exitVehicle(boolean exitVehicle) {
        this.exitVehicle = exitVehicle;
    }

    /**
     * Check if the kit should have hunger.
     * @return Whether the kit has hunger.
     */
    public boolean hunger() {
        return hunger;
    }

    /**
     * Change if the kit should have hunger.
     * @param hunger Whether the kit has hunger.
     */
    public void hunger(boolean hunger) {
        this.hunger = hunger;
    }

    /**
     * Get the Icon Material of the kit,
     * @return Icon Material.
     */
    public Material iconMaterial() {
        return iconMaterial;
    }

    /**
     * Change the Icon material of the kit.
     * @param iconMaterial New Icon material.
     */
    public void iconMaterial(Material iconMaterial) {
        this.iconMaterial = iconMaterial;
    }

    /**
     * Get the id of the kit.
     * @return Kit id.
     */
    public String id() {
        return id;
    }

    /**
     * Get the game mode players should be put in.
     * @return Gamemode to put players in.
     */
    public GameMode gameMode() {
        return gameMode;
    }

    /**
     * Change what gamemode the kit uses.
     * @param gameMode New gamemode to put players in.
     */
    public void gameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    /**
     * Get the max health of the kit.
     * @return Kit's max health.
     */
    public double maxHealth() {
        return maxHealth;
    }

    /**
     * Change the max health of the kit.
     * @param maxHealth New max health.
     */
    public void maxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
    }

    /**
     * Get the maximum amount of damage a player can do.
     * @return Max damage.
     */
    public double maxDamage() {
        return maxDamage;
    }

    /**
     * Change the maximum amount of damage a player can do.
     * @param maxDamage New max damage.
     */
    public void maxDamage(double maxDamage) {
        this.maxDamage = maxDamage;
    }

    /**
     * Get the name of the kit.
     * @return Kit name.
     */
    public String name() {
        return name;
    }

    /**
     * Get if the kit should have natural regeneration.
     * @return Whether the kit should have natural regeneration.
     */
    public boolean naturalRegeneration() {
        return naturalRegeneration;
    }

    /**
     * Change if the kit should have natural regeneration.
     * @param naturalRegeneration Whether the kit should have natural regeneration.
     */
    public void naturalRegeneration(boolean naturalRegeneration) {
        this.naturalRegeneration = naturalRegeneration;
    }

    /**
     * Called when a block is broken in game.
     * @param game Current Game
     * @param event BlockBreakEvent
     */
    public void onBlockBreak(Game game, BlockBreakEvent event) {}

    /**
     * Called when a block is placed in game.
     * @param game Current Game
     * @param event BlockPlaceEvent
     */
    public void onBlockPlace(Game game, BlockPlaceEvent event) {}

    /**
     * Called when the player interacts with a named item.
     * @param game Game the player is in.
     * @param player Player interacting with the item.
     * @param item Name of the item.
     */
    public void onNamedItemClick(Game game, Player player, String item) {}

    /**
     * Called when a player interacts with something.
     * @param game Game the player is in.
     * @param event PlayerInteractEvent.
     */
    public void onPlayerInteract(Game game, PlayerInteractEvent event) {}

    /**
     * Called when a playe rleaves a game.
     * @param game Game the player is leaving.
     * @param player Player leaving the game.
     */
    public void onGamePlayerLeave(Game game, Player player) {}

    /**
     * Called when a player toggles flight.
     * @param game Game the player is toggling flight in.
     * @param event PlayerToggleFlightEvent.
     */
    public void onPlayerToggleFlight(Game game, PlayerToggleFlightEvent event) {}

    /**
     * Called when a player toggles sneak.
     * @param game Game the player is in.
     * @param event PlayerToggleSneakEvent.
     */
    public void onPlayerToggleSneak(Game game, PlayerToggleSneakEvent event) {}

    /**
     * Called when a projectile hits something.
     * @param game Game the projectile hits in.
     * @param event ProjectileHitEvent.
     */
    public void onProjectileHit(Game game, ProjectileHitEvent event) {}

    /**
     * Called when a player launches a projectile.
     * @param player Player launching the projectile.
     * @param game Game to launch.
     * @param event ProjectileLaunchEvent.
     */
    public void onProjectileLaunch(Player player, Game game, ProjectileLaunchEvent event) {}

    /**
     * Called when a kit is applied.
     * @param game Current Game.
     * @param player Player kit is being applied to.
     */
    public void onKitApply(Game game, Player player) {}

    /**
     * Get if the kit uses ranged damage.
     * @return Whether the kit uses ranged damage.
     */
    public boolean rangedDamage() {
        return rangedDamage;
    }

    /**
     * Set the if the kit should use ranged damage.
     * @param rangedDamage Whether the kit uses ranged damage.
     */
    public void rangedDamage(boolean rangedDamage) {
        this.rangedDamage = rangedDamage;
    }

    /**
     * Get the scoreboard of the kit.
     * Defaults to GameScoreboard
     * @param game Game to get scoreboard of.
     * @param player Player to get scoreboard for.
     * @return Scoreboard.
     */
    public CustomScoreboard scoreboard(Game game, Player player) {
        return new GameScoreboard(player, game);
    }

    /**
     * Get the starting health of the kit.
     * @return Starting health.
     */
    public double startingHealth() {
        return startingHealth;
    }

    /**
     * Change the starting health of the kit.
     * @param startingHealth New starting health.
     */
    public void startingHealth(double startingHealth) {
        this.startingHealth = startingHealth;
    }

    /**
     * Get the starting hunger of the kit.
     * @return New starting hunger.
     */
    public int startingHunger() {
        return startingHunger;
    }

    /**
     * Change the starting hunger
     * @param startingHunger New starting health.
     */
    public void startingHunger(int startingHunger) {
        this.startingHunger = startingHunger;
    }

    /**
     * Get the kit's starting saturation.
     * @return Kit's starting saturation.
     */
    public float startingSaturation() {
        return startingSaturation;
    }

    /**
     * Change the kit's starting saturation.
     * @param startingSaturation New starting saturation.
     */
    public void startingSaturation(float startingSaturation) {
        this.startingSaturation = startingSaturation;
    }

    /**
     * Set if players should be able to take damage.
     * @return Whether the kit has damage.
     */
    public boolean takeDamage() {
        return takeDamage;
    }

    /**
     * Set if the kit should have damage.
     * @param takeDamage Whether the kit has damage.
     */
    public void takeDamage(boolean takeDamage) {
        this.takeDamage = takeDamage;
    }

    /**
     * Get the kit's void level.
     * @return Void level.
     */
    public int voidLevel() {
        return voidLevel;
    }

    /**
     * Change the kit's void level.
     * @param voidLevel New void level.
     */
    public void voidLevel(int voidLevel) {
        this.voidLevel = voidLevel;
    }

    public void onRoundStart(Game game, Player player) {}

    public void onRoundEnd(Game game, Player player) {}

    /**
     * Get if water should kill players.
     * @return Whether water should kill players.
     */
    public boolean waterKills() {
        return waterKills;
    }

    /**
     * Change if water should kill.
     * @param waterKills Whether water should kill.
     */
    public void waterKills(boolean waterKills) {
        this.waterKills = waterKills;
    }
}