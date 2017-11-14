package me.KaliCode.infection;

import org.bukkit.*;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Main extends JavaPlugin {

    public void onEnable() { //just setting up the plugin here
        updateNumber = 1.0D;
        ofThree = 1.0D;

        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getServer().getPluginManager().registerEvents(new Event(), this);
        getCommand("infection").setExecutor(new Command());
        getLogger().info("Infection enabled!");

        gameActive = false;
        afterPat0 = false;

        configCreate(this);
        configCopyDef();
        configSave();

        getRaSpawn();
        setUpWorld(configWorld());

        timeleft = 0;
        progSet = .999 / (waitingTime() / 20);

        barWaitTime = Bukkit.createBossBar(ChatColor.GREEN.toString() + ChatColor.BOLD +
                "Infected player chosen in " + ChatColor.GRAY + ChatColor.BOLD +
                Integer.toString(waitingTime() / 20) + ChatColor.GREEN + ChatColor.BOLD +
                " seconds.", BarColor.WHITE, BarStyle.SOLID);

        barWaitTime.setProgress(0.0);
        normal.clear();

        if (Bukkit.getOnlinePlayers().size() != 0) {
            Bukkit.getServer().broadcastMessage(ChatColor.RED + "Players cannot be online during a reload!");
            kickPlayers(false);
        }
    }

    public void onDisable() { //nothing important here
        getLogger().info("Infection disabled!");
    }

    public static ArrayList<Player> normal = new ArrayList<>(); //this is how I manage the survivor team

    public static ArrayList<Player> infected = new ArrayList<>(); //this is how I manage the infected team

    public static ArrayList<Location> iSpawns = new ArrayList<>(); //this is used to spawn you to a random spawn point

    public static YamlConfiguration cfg; //instance of the config.yml

    private static File cFile; //instance of the file for the config.yml

    public static Main plugin; //instance of the plugin

    public static BossBar barWaitTime; //instance of the bossbar

    public static boolean gameActive; //this is used to see if a game is active or not

    public static boolean afterPat0; //this is used to see if a game has already chosen patient zero

    public static double updateNumber; //this updates the time on the bossbar

    public static double ofThree; //this is used to regulate the bossbar timer

    public static double progSet; //this sets the progress on the bossbar

    public static int id1; //this is an id for one of the schedulers

    private static World configWorld() { //this return the world set in the config.yml

        if(!cfg.contains("WorldName")) {

            cfg.set("WorldName", "world");
            configSave();

            World w = Bukkit.getServer().getWorld(cfg.getString("WorldName"));
            return w;

        } else {

            World w1 = Bukkit.getServer().getWorld(cfg.getString("WorldName"));
            return w1;
        }
    }

    public static Location ra() { //this returns a random spawn from the ispawns list
        return (Location) iSpawns.get(new Random().nextInt(iSpawns.size()));
    }

    public static String getSpawnSInfo(String type, String number) { //this returns spawn information as a string

        if (type.equalsIgnoreCase("world"))
            return cfg.getString("Spawn" + number + ".world");

        if (type.equalsIgnoreCase("x"))
            return cfg.getString("Spawn" + number + ".x");

        if (type.equalsIgnoreCase("y"))
            return cfg.getString("Spawn" + number + ".y");

        if (type.equalsIgnoreCase("z"))
            return cfg.getString("Spawn" + number + ".z");

        return null;
    }

    public static Double getSpawnDInfo(String type, String number) { //this returns spawn information as a double
        if (type.equalsIgnoreCase("x"))
            return Double.valueOf(cfg.getDouble("Spawn" + number + ".x"));

        if (type.equalsIgnoreCase("y"))
            return Double.valueOf(cfg.getDouble("Spawn" + number + ".y"));

        if (type.equalsIgnoreCase("z"))
            return Double.valueOf(cfg.getDouble("Spawn" + number + ".z"));

        return null;
    }

    public static int minimumPlayers() { //this returns the minimum amount of players as set in the config.yml
        return cfg.getInt("MinimumPlayers");
    }

    public static int maximumPlayers() { //this returns the maximum amount of players as set in the config.yml
        return cfg.getInt("MaximumPlayers");
    }

    public static int waitingTime() { //this returns the waiting time as set in the config.yml
        return cfg.getInt("WaitingTime");
    }

    public static int survivalTime() { //this returns the survival time as set in the config.yml
        return cfg.getInt("SurvivalTime");
    }

    public static boolean isInt(String s) { //this checks if a string is an integer or not

        try {
            Integer.parseInt(s);
        }
        catch (NumberFormatException nfe) {

            nfe.printStackTrace();
            return false;
        }
        return true;
    }

    public static int getInt(String s) { // this gets an integer from a string
        return Integer.parseInt(s);
    }

    private void configCreate(Main pluginX) {

        plugin = pluginX;
        cFile = new File(plugin.getDataFolder(), "config.yml");

        cfg = YamlConfiguration.loadConfiguration(cFile);
        cfg.options().header("Time is read in ticks, 1 second = 20 ticks.");
    }

    private static void getRaSpawn() { //this adds spawns to ispawns list

        iSpawns.clear();

        for (int i = 1; i < 11; i++) {

            if (cfg.contains("Spawn" + Integer.toString(i))) {

                World w = Bukkit.getWorld(getSpawnSInfo("world", Integer.toString(i)));
                Double x = getSpawnDInfo("x", Integer.toString(i));
                Double y = getSpawnDInfo("y", Integer.toString(i));
                Double z = getSpawnDInfo("z", Integer.toString(i));
                Location zLoc = new Location(w, x.doubleValue(), y.doubleValue(), z.doubleValue());

                iSpawns.add(zLoc);
            }
        }
    }

    public static void configSave() { //this saves the config.yml

        try {
            cfg.save(cFile);
        }
        catch (IOException i) {
            i.printStackTrace();
        }
    }

    private void configCopyDef() { //this copies the default values of the config.yml

        try {

            Reader defConfigStream = new InputStreamReader(getResource("config.yml"), "UTF8");
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);

            cfg.options().copyDefaults(true);
            cfg.setDefaults(defConfig);

        } catch (UnsupportedEncodingException u) {
            u.printStackTrace();
        }
    }

    public static void slowChooseInfected() { //this puts the game into the survival phase in time defined by the config.yml

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            public void run() {

                if(iSpawns.isEmpty()) {

                    Bukkit.getServer().broadcastMessage(ChatColor.RED.toString() +
                            ChatColor.BOLD + "Game canceled, no spawns have been added!");

                    kickPlayers(true);
                } else {

                    if (normal.size() > 0) {

                        gameActive = true;
                        afterPat0 = true;

                        Player patient0 = (Player) normal.get(new Random().nextInt(normal.size()));

                        normal.remove(patient0);
                        infected.add(patient0);

                        for (Player pl : Bukkit.getOnlinePlayers()) {
                            updateScoreboard(pl, afterPat0);
                        }
                        countTimeLeft();
                        updateParticles();

                        patient0.setInvulnerable(false);
                        patient0.getInventory().clear();
                        patient0.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 1, true, false));

                        ItemStack bow = new ItemStack(Material.BOW);
                        ItemStack arrow = new ItemStack(Material.ARROW, 10);

                        for (Player player : normal) {

                            Inventory inv = player.getInventory();
                            inv.setItem(0, bow);
                            inv.setItem(1, arrow);

                            player.setInvulnerable(false);
                        }
                        Title title = new Title();
                        title.setThingsUp("{\"text\": \"You are Infected!\", \"color\":\"green\", \"bold\":\"true\"}");
                        title.sendPacket(patient0, title.packet);
                        Bukkit.getServer().broadcastMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + patient0.getName() +
                                ChatColor.GREEN + ChatColor.BOLD + " is patient zero.");

                        gameTimer();

                    } else {

                        Bukkit.getServer().broadcastMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Not enough players online; Ending game!");
                        kickPlayers(true);
                    }
                }
            }
        }, waitingTime());
    }

    public static void fastChooseInfected() { //this is used when all infected players leave the game and the game needs to choose a new one

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            public void run() {

                if (gameActive) {

                    if (normal.size() > 0) {

                        Player patient0 = (Player)normal.get(new Random().nextInt(normal.size()));
                        patient0.getInventory().clear();
                        patient0.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000,
                                1, true, false));

                        normal.remove(patient0);
                        infected.add(patient0);

                        Bukkit.getServer().broadcastMessage(ChatColor.GRAY.toString() + ChatColor.BOLD + patient0.getName() +
                                ChatColor.GREEN + ChatColor.BOLD + " is now infected!");

                    } else {
                        Bukkit.getServer().broadcastMessage(ChatColor.RED.toString() + ChatColor.BOLD + "Not enough players online; Ending game!");

                        kickPlayers(true);
                    }
                }
            }
        }, 100L);
    }

    public static void gameTimer() { //this times the game after the survival phase has begone

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            public void run() {

                if (gameActive) {

                    gameActive = false;

                    if (normal.size() > 0) {

                        Title title1 = new Title();
                        title1.setThingsUp("{\"text\": \"Survivors win!\", \"color\":\"green\", \"bold\":\"true\"}");

                        for (Player player : Bukkit.getOnlinePlayers()) {

                            title1.sendPacket(player, title1.packet);
                        }
                        Bukkit.getServer().broadcastMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "Not all players were infected in time, survivors win.");
                        kickPlayers(true);

                    } else {

                        Title title2 = new Title();
                        title2.setThingsUp("{\"text\": \"Infected win!\", \"color\":\"green\", \"bold\":\"true\"}");

                        for (Player player : Bukkit.getOnlinePlayers()) {

                            title2.sendPacket(player, title2.packet);
                        }
                        Bukkit.getServer().broadcastMessage(ChatColor.GREEN.toString() + ChatColor.BOLD + "All players were infected, infected win.");
                        kickPlayers(true);
                    }
                }
            }
        }, survivalTime());
    }

    private static int timeleft; //instance of the amount of integer time left in the game

    private static void countTimeLeft() { //this sets the time left every second

        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {

                if ((survivalTime() / 20) >= timeleft) {

                    for (Player p : Bukkit.getOnlinePlayers()) {

                        updateScoreboard(p, afterPat0);
                    }
                    timeleft++;
                }
            }
        }, 0, 20);
    }

    public static void kickPlayers(Boolean withreload) { //this kicks players out of the game to the lobby server

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

            public void run() {

                gameActive = false;
                normal.clear();
                infected.clear();

                for (Player online : Bukkit.getOnlinePlayers()) {

                    ByteArrayOutputStream b = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(b);

                    try {

                        out.writeUTF("Connect");
                        out.writeUTF(cfg.getString("LobbyServerName"));

                    } catch (IOException i) {
                        i.printStackTrace();
                    }
                    online.sendPluginMessage(Bukkit.getServer().getPluginManager()
                            .getPlugin("Infection"), "BungeeCord", b.toByteArray());
                }

                if(withreload)
                    Bukkit.getServer().reload();
            }
        }, 100L);
    }

    public static void updateBossBar(BossBar bar) { //this maintains and updates the bossbar

        id1 = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            public void run() {

                if (updateNumber <= waitingTime() / 20) {

                    if (progSet < 1.0D) {

                        bar.setProgress(progSet);
                        progSet += .999 / (waitingTime() / 20);
                    }
                    bar.setTitle(ChatColor.GREEN.toString() + ChatColor.BOLD + "Infected player chosen in " + ChatColor.GRAY + ChatColor.BOLD +
                            Integer.toString(waitingTime() / 20 - (int)updateNumber) + ChatColor.GREEN + ChatColor.BOLD + " seconds.");

                    updateNumber += 1.0D;

                } else {

                    bar.setVisible(false);
                    bar.removeAll();
                    Bukkit.getScheduler().cancelTask(id1);
                }
            }
        }, 20L, 20L);
    }

    public static void updateScoreboard(Player p, Boolean afterChoose) { //this maintains and updates the scoreboard

        ScoreboardManager manager = Bukkit.getServer().getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective obj = board.registerNewObjective("Board", "Board");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team space = board.registerNewTeam("Space");
        space.addEntry("");
        space.addEntry(" ");
        space.addEntry("  ");
        space.addEntry("   ");

        Team all = board.registerNewTeam("All");
        Team inf = board.registerNewTeam("Inf");
        Team norm = board.registerNewTeam("Norm");
        Team time = board.registerNewTeam("Time");
        if (afterChoose) {
            inf.addEntry(ChatColor.GREEN + "Infected: " + ChatColor.GRAY + infected.size());

            norm.addEntry(ChatColor.GREEN + "Survivors: " + ChatColor.GRAY + normal.size());

            obj.getScore("").setScore(7);

            time.addEntry(ChatColor.GREEN + "Time left: " + ChatColor.GRAY +
                    convertToMinutes((survivalTime() / 20) - timeleft));

            obj.getScore(ChatColor.GREEN + "Time left: " + ChatColor.GRAY +
                    convertToMinutes((survivalTime() / 20) - timeleft)).setScore(6);

            obj.getScore("   ").setScore(5);
            obj.getScore(ChatColor.GREEN + "Infected: " + ChatColor.GRAY + infected.size()).setScore(4);
            obj.getScore("  ").setScore(3);
            obj.getScore(ChatColor.GREEN + "Survivors: " + ChatColor.GRAY + normal.size()).setScore(2);
            obj.getScore(" ").setScore(1);

        } else {

            all.addEntry(ChatColor.GREEN + "Online: " + ChatColor.GRAY + Bukkit.getOnlinePlayers().size());

            obj.getScore("").setScore(7);
            obj.getScore(ChatColor.GREEN + "Online: " + ChatColor.GRAY + Bukkit.getOnlinePlayers().size()).setScore(6);
            obj.getScore(" ").setScore(5);
        }
        obj.setDisplayName(ChatColor.DARK_GREEN.toString() + ChatColor.BOLD + "Infection");
        p.setScoreboard(board);
    }

    private static void updateParticles() { //this maintains particles for all infected players

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

            @Override
            public void run() {

                for(Player p : infected) {
                    Bukkit.getWorld(configWorld().getName()).spawnParticle(Particle.SPELL_WITCH, locPlus(p), 10);
                }
            }
        },0, 5);
    }

    private static Location locPlus(Player player) { //this is used to put the particles at a specific location

        Location l = new Location(player.getWorld(), player.getLocation().getX(),
                player.getLocation().getY() + 1, player.getLocation().getZ());

        return l;
    }

    private static String convertToMinutes(int integer) { //this converts the timeleft to numbers decreasing as minutes

        int secondsLeftOver = integer % 60;
        double dminutes = integer / 60;
        int minutes = (int) Math.floor(dminutes);

        if(secondsLeftOver < 10) {

            String newSecondsLeftover = "0" + Integer.toString(secondsLeftOver);
            return Integer.toString(minutes) +  ":" + newSecondsLeftover;
        }
        return Integer.toString(minutes) + ":" + secondsLeftOver;
    }

    public static void setUpWorld(World world) { //this sets up the world to ensure correct game-play

        world = Bukkit.getServer().getWorld(world.getName());
        world.setDifficulty(Difficulty.PEACEFUL);

        Bukkit.getServer().setDefaultGameMode(GameMode.SURVIVAL);
    }
}
