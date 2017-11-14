package me.KaliCode.infection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;

public class Title {

    private Constructor<?> titleConstructor; //instance of the  title constructor
    private Object enumTitle; //instance of the enum object class
    private Object chat; //instance of the chat component
    public Object packet; //instance of the packet

        public void sendPacket(Player player, Object packet) { //this sends the title packet to a player

            try {

                Object handle = player.getClass().getMethod("getHandle").invoke(player);

                Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
                playerConnection.getClass().getMethod("sendPacket", getNMSClass("Packet")).invoke(playerConnection, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    private Class<?> getNMSClass(String name) { //used to get an NMS class
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

            try {
                return Class.forName("net.minecraft.server." + version + "." + name);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
    }

    public void setThingsUp(String titleMessage) { //used to set up the title packet
        try {
            enumTitle = getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0]
                    .getDeclaredField("TITLE").get(null);

            chat = getNMSClass("IChatBaseComponent").getDeclaredClasses()[0]
                    .getMethod("a", String.class).invoke(null, titleMessage);

            titleConstructor = getNMSClass("PacketPlayOutTitle").getConstructor(getNMSClass("PacketPlayOutTitle").getDeclaredClasses()[0],
                    getNMSClass("IChatBaseComponent"), int.class, int.class, int.class);

            packet = titleConstructor.newInstance(enumTitle, chat, 20, 40, 20);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
