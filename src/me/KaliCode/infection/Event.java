package me.KaliCode.infection;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import static me.KaliCode.infection.Main.*;

public class Event implements Listener {

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent e) { //this denies players if the game has already started

        if (Bukkit.getOnlinePlayers().size() == maximumPlayers()) {

            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_FULL, ChatColor.RED.toString() +
                    ChatColor.BOLD + "Game has already started!");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) { //this sets up players and starts the game when minimum players has been met

        Player p = e.getPlayer();
        normal.add(p);

        Inventory inv = p.getInventory();

        for (Player pl : Bukkit.getOnlinePlayers()) {
            updateScoreboard(pl, afterPat0);
        }

        e.setJoinMessage(ChatColor.GRAY + p.getName() + ChatColor.WHITE + " has joined. " + ChatColor.GRAY +
                Bukkit.getOnlinePlayers().size() + ChatColor.WHITE + "/" + ChatColor.DARK_GRAY +
                Integer.toString(maximumPlayers()));


        p.setInvulnerable(true);
        p.removePotionEffect(PotionEffectType.SPEED);
        p.setGameMode(GameMode.SURVIVAL);

        inv.clear();

        if (cfg.get("JoinPoint") != null) {

            World w = Bukkit.getWorld(cfg.getString("JoinPoint.world"));
            Double x = Double.valueOf(cfg.getDouble("JoinPoint.x"));
            Double y = Double.valueOf(cfg.getDouble("JoinPoint.y"));
            Double z = Double.valueOf(cfg.getDouble("JoinPoint.z"));
            Location joinpoint = new Location(w, x.doubleValue(), y.doubleValue(), z.doubleValue());

            p.teleport(joinpoint);
        }
        if ((Bukkit.getOnlinePlayers().size() == minimumPlayers()) && (!gameActive)) {

            slowChooseInfected();

            for (Player player : Bukkit.getOnlinePlayers()) {

                barWaitTime.addPlayer(player);
            }
            barWaitTime.setVisible(true);
            updateBossBar(barWaitTime);
            gameActive = true;

            Bukkit.getServer().broadcastMessage(ChatColor.GREEN.toString() + ChatColor.BOLD +
                    "Infected player chosen in " + ChatColor.GRAY + ChatColor.BOLD +
                    waitingTime() / 20 + ChatColor.GREEN + ChatColor.BOLD + " seconds.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { //this corrects teams and potion effects when a player leaves

        Player p = e.getPlayer();
        for (Player pl : Bukkit.getOnlinePlayers()) {
            updateScoreboard(pl, afterPat0);
        }
        if (normal.contains(p)) {
            normal.remove(p);

        } else if (infected.contains(p)) {

            infected.remove(p);
            p.removePotionEffect(PotionEffectType.SPEED);

            if ((infected.size() < 1) && (gameActive)) {

                fastChooseInfected();
                Bukkit.getServer().broadcastMessage(ChatColor.RED.toString() + ChatColor.BOLD + "No more infected remain in-game! Choosing new patient zero in 5 seconds!");
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) { //this manages team vs team hits and team vs enemy hits

        if (!gameActive) {
            e.setCancelled(true);

        } else if (((e.getEntity() instanceof Player)) &&
                ((e.getDamager() instanceof Player))) {

            Player p = (Player) e.getEntity();
            Player d = (Player) e.getDamager();

            if ((normal.contains(p)) && (normal.contains(d))) {
                e.setCancelled(true);
            }
            if ((normal.contains(p)) && (infected.contains(d))) {

                e.setDamage(p.getHealth() - .5);
                p.setHealth(20);

                normal.remove(p);
                infected.add(p);
                for (Player pl : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(pl, afterPat0);
                }
                if(iSpawns.size() > 0)
                    p.teleport(ra());

                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_METAL_BREAK, 5.0F, 5.0F);
                p.getInventory().clear();

                Bukkit.broadcastMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + p.getName() + ChatColor.GREEN + ChatColor.BOLD + " has been infected!");

                if (infected.size() == Bukkit.getOnlinePlayers().size()) {

                    gameActive = false;

                    Title title = new Title();
                    title.setThingsUp("{\"text\": \"Infected win!\", \"color\":\"green\", \"bold\":\"true\"}");

                    for (Player player : Bukkit.getOnlinePlayers()) {

                        title.sendPacket(player, title.packet);
                    }
                    Bukkit.getServer().broadcastMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "All players were infected, infected win.");
                    kickPlayers(true);
                }
            } else if ((infected.contains(p)) && (normal.contains(d))) {

                if (p.getHealth() <= 2.0) {

                    e.setDamage(p.getHealth() - .5);
                    p.setHealth(20);

                    if(iSpawns.size() > 0)
                        p.teleport(ra());

                    p.getWorld().playSound(p.getLocation(), Sound.BLOCK_METAL_BREAK, 5.0F, 5.0F);
                } else {

                    e.setDamage(2.0D);
                }
            }
            if ((infected.contains(p)) && (infected.contains(d))) {
                e.setCancelled(true);
            }
        } else if (((e.getEntity() instanceof Player)) &&
                ((e.getDamager() instanceof Arrow))) {

            Player p = (Player)e.getEntity();
            Arrow arrow = (Arrow)e.getDamager();
            Player d = (Player)arrow.getShooter();

            if ((normal.contains(d)) && (infected.contains(p))) {

                Inventory inv = d.getInventory();
                ItemStack addArrow = new ItemStack(Material.ARROW, 1);

                e.setDamage(p.getHealth() - .5);
                p.setHealth(20);

                if(iSpawns.size() > 0)
                    p.teleport(ra());

                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_METAL_BREAK, 5.0F, 5.0F);
                p.getInventory().clear();

                if (!inv.contains(Material.ARROW, 10))
                    inv.addItem(addArrow);
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) { //this cancels all damage except for entity vs entity damage

        if (!e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK))
            e.setCancelled(true);
    }

    @EventHandler
    public void onTileDrop(PlayerDropItemEvent e) { //this disallows tile drops
        e.setCancelled(true);
    }

    @EventHandler
    public void onTilePickup(PlayerPickupItemEvent e) { //this disallows tile pickups
        e.setCancelled(true);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) { //this disallows mob spawns
        e.setCancelled(true);
    }

    @EventHandler
    public void onArrowHit(ProjectileHitEvent e) { //this removes an arrow after it has hit its target

        if(e.getEntity().getType() == EntityType.ARROW)
            e.getEntity().remove();
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) { //this manages and formats the chat

        Player p = e.getPlayer();

        if(infected.contains(p)) {

            e.setFormat(ChatColor.DARK_GRAY + "[" + ChatColor.RED + "Infected" +
                    ChatColor.DARK_GRAY + "] " + ChatColor.RESET + e.getMessage());

        } else {

            e.setFormat(ChatColor.DARK_GRAY + "[" + ChatColor.GREEN + "Survivor" +
                    ChatColor.DARK_GRAY + "] " + ChatColor.RESET + e.getMessage());
        }
    }
}
