package net.jadedmc.jadedduelslegacy.listeners;

import net.jadedmc.jadedduelslegacy.JadedDuelsPlugin;
import net.jadedmc.jadedduelslegacy.game.Game;
import net.jadedmc.jadedutils.MathUtils;
import net.jadedmc.jadedutils.chat.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {
    private final JadedDuelsPlugin plugin;

    public EntityDamageByEntityListener(final JadedDuelsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEvent(EntityDamageByEntityEvent event) {
        // Cancel and return if damage is a spectator.
        if(event.getDamager() instanceof Player player) {
            Game game = plugin.gameManager().game(player);
            if(game != null) {
                if(game.spectators().contains(player)) {
                    event.setCancelled(true);
                    return;
                }

                if(event.getEntity() instanceof Player) {
                    Player target = (Player) event.getEntity();
                    if(game.teamManager().team(player).equals(game.teamManager().team(target)) && !player.equals(target)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        // Exit if not a player.
        if(!(event.getEntity() instanceof Player player)) {
            return;
        }

        Game game = plugin.gameManager().game(player);

        // Exit if player is not in a game.
        if(game == null) {
            event.setCancelled(true);
            return;
        }

        // Checks if the kit should take damage
        if(!game.kit().takeDamage()) {
            event.setCancelled(true);
            return;
        }

        // Check if players should do damage.
        // TODO: Do Damage Settings
        /*
        if(!game.kit().hasDoDamage()) {
            event.setDamage(0);
            return;
        }
         */


        // Check if the kit uses boxing damage.
        if(game.kit().boxingDamage()) {
            event.setDamage(0.2);
        }

        if(game.kit().maxDamage() >= 0 && (event.getDamage() > game.kit().maxDamage())) {
            event.setDamage(game.kit().maxDamage());
        }

        switch (event.getDamager().getType()) {
            case ENDER_CRYSTAL -> {
                if(player.getHealth() < event.getFinalDamage()) {
                    event.setCancelled(true);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> game.playerKilled(player), 1);
                    return;
                }
            }

            case PLAYER -> {
                Player damager = (Player) event.getDamager();

                if(event.getFinalDamage() >= player.getHealth()) {
                    //damager.sendActionBar(ChatUtils.translate(game.teamManager().team(player).teamColor().chatColor() + player.getName() + "&a's Health: &c0%"));
                    game.playerKilled(player, damager);
                }
                else {
                    //damager.sendActionBar(ChatUtils.translate(game.teamManager().team(player).teamColor().chatColor() + player.getName() + "&a's Health: " + GameUtils.getFormattedHealth(player)));
                }
            }

            case ARROW -> {
                Arrow arrow = (Arrow) event.getDamager();

                // Makes sure a player shot the arrow.
                if(!(arrow.getShooter() instanceof Player shooter)) {
                    return;
                }

                if(game.teamManager().team(player).equals(game.teamManager().team(shooter)) && !player.equals(shooter)) {
                    event.setCancelled(true);
                    return;
                }

                if(game.teamManager().team(player).equals(game.teamManager().team(shooter)) && !player.equals(shooter)) {
                    event.setCancelled(true);
                    return;
                }

                // Applies ranged damage if enabled.
                if(game.kit().rangedDamage()) {
                    rangedDamage(event, player, shooter);
                }

                if(player.getHealth() <= event.getFinalDamage()) {
                    event.setCancelled(true);
                    Bukkit.getScheduler().runTaskLater(plugin, () -> game.playerKilled(player, shooter), 1);
                }
                else {
                    ChatUtils.chat(shooter, game.teamManager().team(player).teamColor().chatColor() + player.getName() + " &ahas " + getHealthPercent((player.getHealth() - event.getFinalDamage()))  + " &aremaining.");
                }
            }

            case EGG, SNOWBALL -> {}

            default -> {
                if(player.getHealth() > event.getFinalDamage()) {
                    return;
                }

                event.setCancelled(true);
                Bukkit.getScheduler().runTaskLater(plugin, () -> game.playerKilled(player), 1);
            }
        }
    }

    /**
     * Get the health of a player based in percent and formatted.
     * @param damage Health.
     * @return formatted health.
     */
    private String getHealthPercent(Double damage) {
        int percent = MathUtils.percent(damage, 20.0);
        ChatColor color;

        if(percent < 0) {
            percent = 0;
        }

        if(percent == 0) {
            color = ChatColor.DARK_RED;
        }
        if(percent < 25) {
            color = ChatColor.RED;
        }
        else if(percent < 50) {
            color = ChatColor.GOLD;
        }
        else if(percent < 75) {
            color = ChatColor.YELLOW;
        }
        else {
            color = ChatColor.GREEN;
        }

        return "" + color + percent + "%";
    }

    /**
     * Applies ranged damage to the arrow.
     * @param event EntityDamageByEntityEvent
     * @param player Player getting hit.
     * @param shooter Shooter of the arrow.
     */
    private void rangedDamage(EntityDamageByEntityEvent event, Player player, Player shooter) {
        Location shooterloc = shooter.getLocation();
        double dis = player.getLocation().distance(shooterloc);
        if (dis > 40.0D) {
            event.setDamage(7.0D);
        } else if (dis > 30.0D && dis <= 40.0D) {
            event.setDamage(6.0D);
        } else if (dis > 20.0D && dis <= 30.0D) {
            if (event.getDamage() > 6.0D) {
                event.setDamage(5.8D);
            } else {
                event.setDamage(4.0D);
            }
        } else if (dis > 17.0D && dis <= 20.0D) {
            if (event.getDamage() > 6.0D) {
                event.setDamage(5.0D);
            } else if (event.getDamage() <= 6.0D && event.getDamage() >= 2.0D) {
                event.setDamage(3.0D);
            } else if (event.getDamage() < 2.0D) {
                event.setDamage(1.0D);
            }
        } else if (dis > 14.0D && dis <= 17.0D) {
            if (event.getDamage() > 6.0D) {
                event.setDamage(4.0D);
            } else if (event.getDamage() <= 6.0D && event.getDamage() >= 2.0D) {
                event.setDamage(2.0D);
            } else if (event.getDamage() < 2.0D) {
                event.setDamage(0.6D);
            }
        } else if (dis > 8.0D && dis <= 14.0D) {
            if (event.getDamage() > 6.0D) {
                event.setDamage(3.0D);
            } else if (event.getDamage() <= 6.0D && event.getDamage() >= 2.0D) {
                event.setDamage(1.5D);
            } else if (event.getDamage() < 2.0D) {
                event.setDamage(0.5D);
            }
        } else if (dis > 4.0D && dis <= 8.0D) {
            if (event.getDamage() > 6.0D) {
                event.setDamage(2.0D);
            } else if (event.getDamage() <= 6.0D && event.getDamage() >= 2.0D) {
                event.setDamage(1.0D);
            } else if (event.getDamage() < 2.0D) {
                event.setDamage(0.2D);
            }
        } else if (dis > 2.0D && dis <= 4.0D) {
            if (event.getDamage() > 6.0D) {
                event.setDamage(1.0D);
            } else if (event.getDamage() <= 6.0D && event.getDamage() >= 2.0D) {
                event.setDamage(0.4D);
            } else if (event.getDamage() < 2.0D) {
                event.setDamage(0.1D);
            }
        } else if (dis >= 0.0D && dis <= 2.0D) {
            if (event.getDamage() > 6.0D) {
                event.setDamage(0.4D);
            } else if (event.getDamage() <= 6.0D && event.getDamage() >= 2.0D) {
                event.setDamage(0.2D);
            } else if (event.getDamage() < 2.0D) {
                event.setDamage(0.1D);
            }
        }
    }
}