package net.schlaubi.ultimatediscord.spigot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.schlaubi.ultimatediscord.util.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandDiscord implements CommandExecutor, TabExecutor {

    public static HashMap<String, String> users = new HashMap<>();

    private String generateString(){
        String CHARS = "abcdefghijklmnopqrstuvwxyz1234567980";
        StringBuilder random = new StringBuilder();
        Random rnd = new Random();
        while(random.length() < 5){
            int index = (int) (rnd.nextFloat() * CHARS.length());
            random.append(CHARS.charAt(index));
        }
        return random.toString();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command name, String lable, String[] args) {
        if(sender instanceof Player){
            FileConfiguration cfg = Main.getConfiguration();
            Player player = (Player) sender;
            if(args.length > 0) {
                    // Reload command handler
                if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("discord.reload")) {
                        player.sendMessage("§7[§Discord§7]§a Settings reloaded");
                    }
                    // Verify command handler
                } else if (args[0].equalsIgnoreCase("verify")) {
                    if (users.containsKey(player.getName())) {
                        player.sendMessage(cfg.getString("Messages.running").replace("&", "§").replace("%code%", users.get(player.getName())));
                    } else if (MySQL.isUserLinked(player)) {
                        player.sendMessage(cfg.getString("Messages.verified").replace("&", "§"));
                    } else {
                        users.put(player.getName(), generateString());
                        player.sendMessage(cfg.getString("Messages.verify").replace("&", "§").replace("%code%", users.get(player.getName())));
                        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                            if (users.containsKey(player.getName())) {
                                users.remove(player.getName());
                            }
                        }, 60 * 1000);
                    }
                    // Unlink command handler
                } else if (args[0].equalsIgnoreCase("unlink")) {
                    if (!MySQL.isUserLinked(player)) {
                        player.sendMessage(cfg.getString("Messages.notverified").replace("&", "§"));
                    } else {
                        users.put(player.getName(), generateString());
                        player.sendMessage(cfg.getString("Messages.unverify").replace("&", "§").replace("%code%", users.get(player.getName())));
                        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                            if (users.containsKey(player.getName())) {
                                users.remove(player.getName());
                            }
                        }, 60 * 1000);
                    }
                } else if(args[0].equalsIgnoreCase("update")){
                    if (!MySQL.isUserLinked(player)) {
                        player.sendMessage(cfg.getString("Messages.notverified").replace("&", "§"));
                    } else {
                        Guild guild = Main.jda.getGuilds().get(0);
                        Member member = guild.getMemberById(MySQL.getValue(player, "discordid"));
                        Role role = guild.getRoleById(cfg.getString("Roles.group." + Main.getPermissions().getPrimaryGroup(player)));
                        guild.addRoleToMember(member, role).queue();
                        player.sendMessage(cfg.getString("Messages.updated").replace("&", "§"));
                    }
                }
            } else {
                player.sendMessage(cfg.getString("Messages.help").replace("%nl", "\n").replace("&", "§"));
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("§4§l[UltimateDiscord] You must be a player to run this command");
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command name, String lable, String[] args) {
        String[] subcommands = {"reload", "verify", "unlink", "update"};
        if(args.length > 1 || args.length == 0){
            return  Arrays.asList(subcommands);
        }
        if(args.length > 0){
            List<String> matches = new ArrayList<>();
            for (String subcommand : subcommands){
                if(subcommand.startsWith(args[0]))
                    matches.add(subcommand);
            }
            return matches;
        }
        return null;
    }
}
