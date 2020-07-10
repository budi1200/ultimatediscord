package net.schlaubi.ultimatediscord;

import net.dv8tion.jda.api.JDA;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.schlaubi.ultimatediscord.util.MySQL;
import org.bukkit.entity.Player;

public class UltimateDiscordAPI {

    public static boolean isVerified(Player player){
        return MySQL.isUserLinked(player);
    }

    public static String getUserName(String discordid){
        return MySQL.getValue(discordid, "uuid");
    }

    public static String getDiscordId(Player player){
        return MySQL.getValue(player, "discordid");
    }

    public static JDA getSpigotJDA(){
        return net.schlaubi.ultimatediscord.spigot.Main.jda;
    }
}
