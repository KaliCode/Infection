package me.KaliCode.infection;

import net.minecraft.server.v1_12_R1.EntityZombie;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import static me.KaliCode.infection.Main.*;

public class Command implements CommandExecutor {

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {

        if (label.equalsIgnoreCase("infection")) {

            if (!sender.hasPermission("infection.admin")) {

                sender.sendMessage(ChatColor.RED + "You don't have permission for this command");
                return true;
            }

            if (args.length < 1) {

                sender.sendMessage(ChatColor.DARK_GREEN + "Commands:");

                sender.sendMessage(ChatColor.GREEN + "/infection forcestart: " +
                        ChatColor.GOLD + "Forcefully starts the game if the game isn't started.");

                sender.sendMessage(ChatColor.GREEN + "/infection forcestop: " +
                        ChatColor.GOLD + "Forcefully stops the game and kicks players.");

                sender.sendMessage(ChatColor.GREEN + "/infection setjoin: " +
                        ChatColor.GOLD + "Sets the join point when players join the game.");

                sender.sendMessage(ChatColor.GREEN + "/infection setspawn: " +
                        ChatColor.GOLD + "Adds a spawn point for when players die.");

                sender.sendMessage(ChatColor.GREEN + "/infection removespawn: " +
                        ChatColor.GOLD + "Removes a spawn point.");

                sender.sendMessage(ChatColor.GREEN + "/infection spawninfo: " +
                        ChatColor.GOLD + "Shows the specified spawn's location.");
            } else {
                if (args[0].equalsIgnoreCase("forcestart")) {

                    if (gameActive) {
                        sender.sendMessage(ChatColor.RED + "A game is already started!");
                        return true;
                    }
                    normal.clear();
                    infected.clear();
                    gameActive = true;

                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        normal.add(pl);
                    }
                    slowChooseInfected();

                    Bukkit.getScheduler().cancelAllTasks();
                    Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "Infected player chosen in " + ChatColor.GRAY +
                            waitingTime() / 20 + ChatColor.GREEN + " seconds.");

                    return true;
                }

                if (args[0].equalsIgnoreCase("forcestop")) {

                    kickPlayers(true);

                    Bukkit.getScheduler().cancelAllTasks();
                    Bukkit.getServer().broadcastMessage(ChatColor.GREEN + "The game has been forcefully stopped.");
                    return true;
                }
                if (args[0].equalsIgnoreCase("setjoin")) {

                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "You must be a player to run this command!");
                        return true;
                    }
                    Player p = (Player)sender;
                    Location loc = p.getLocation();

                    cfg.set("JoinPoint", null);
                    cfg.set("JoinPoint.world", loc.getWorld().getName());
                    cfg.set("JoinPoint.x", Double.valueOf(loc.getX()));
                    cfg.set("JoinPoint.y", Double.valueOf(loc.getY()));
                    cfg.set("JoinPoint.z", Double.valueOf(loc.getZ()));
                    configSave();

                    p.sendMessage(ChatColor.GREEN + "Join point set.");
                    return true;
                }
                if (args[0].equalsIgnoreCase("setspawn")) {

                    if (!(sender instanceof Player)) {

                        sender.sendMessage(ChatColor.RED + "You must be a player to run this command!");
                        return true;
                    }
                    if ((args.length != 2) || (!isInt(args[1]))) {

                        sender.sendMessage(ChatColor.RED + "[Usage] /infection setspawn [number]");
                        return true;
                    }
                    Player p = (Player)sender;

                    if ((getInt(args[1]) < 11) && (getInt(args[1]) >= 1)) {

                        Location loc = p.getLocation();

                        cfg.set("Spawn" + args[1], null);
                        cfg.set("Spawn" + args[1] + ".world", loc.getWorld().getName());
                        cfg.set("Spawn" + args[1] + ".x", Double.valueOf(loc.getX()));
                        cfg.set("Spawn" + args[1] + ".y", Double.valueOf(loc.getY()));
                        cfg.set("Spawn" + args[1] + ".z", Double.valueOf(loc.getZ()));
                        configSave();

                        p.sendMessage(ChatColor.GREEN + "Spawn point " + ChatColor.GRAY + args[1] + ChatColor.GREEN + " set.");
                        p.sendMessage(ChatColor.GOLD + "You must reload the plugin for changes to take effect.");
                        return true;
                    }
                    p.sendMessage(ChatColor.RED + "Spawn number must be 1-10!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("removespawn")) {

                    if ((args.length != 2) || (!isInt(args[1]))) {

                        sender.sendMessage(ChatColor.RED + "[Usage] /infection removespawn [number]");
                        return true;
                    }
                    if (cfg.contains("Spawn" + args[1])) {

                        cfg.set("Spawn" + args[1], null);
                        configSave();

                        sender.sendMessage(ChatColor.GREEN + "Spawn point " + ChatColor.GRAY + args[1] + ChatColor.GREEN + " removed.");
                        sender.sendMessage(ChatColor.GREEN + "You must reload the plugin for changes to take effect.");
                        return true;
                    }
                    sender.sendMessage(ChatColor.RED + "The specified spawn does not exist.");
                    return true;
                }
                if (args[0].equalsIgnoreCase("spawninfo")) {

                    if ((args.length != 2) || (!isInt(args[1]))) {

                        sender.sendMessage(ChatColor.DARK_GREEN + "Added spawns:");

                        for (int i = 1; i < 11; i++) {

                            if (cfg.contains("Spawn" + Integer.toString(i))) {

                                sender.sendMessage(ChatColor.GRAY + Integer.toString(i));
                            }
                        }
                        return true;
                    }
                    if (cfg.contains("Spawn" + args[1])) {

                        sender.sendMessage(ChatColor.DARK_GREEN + "Spawn point " +
                                ChatColor.GRAY + args[1] + ChatColor.DARK_GREEN + " info:");

                        sender.sendMessage(ChatColor.GREEN + "World: " + ChatColor.GOLD +
                                getSpawnSInfo("world", args[1]));

                        sender.sendMessage(ChatColor.GREEN + "X: " + ChatColor.GOLD +
                                getSpawnSInfo("x", args[1]));

                        sender.sendMessage(ChatColor.GREEN + "Y: " + ChatColor.GOLD +
                                getSpawnSInfo("y", args[1]));

                        sender.sendMessage(ChatColor.GREEN + "Z: " + ChatColor.GOLD +
                                getSpawnSInfo("z", args[1]));

                        return true;
                    }
                    sender.sendMessage(ChatColor.RED + "The chosen spawn does not exist!");
                    return true;
                }
                if (args[0].equalsIgnoreCase("checkteam")) {

                    if (sender instanceof Player) {

                        Player player = (Player) sender;

                        if (infected.contains(player)) {
                            sender.sendMessage(ChatColor.GREEN + "You are on the Infected team.");
                            return true;

                        } else if (normal.contains(player)) {
                            sender.sendMessage(ChatColor.BOLD + "You are on the Survivors team.");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}