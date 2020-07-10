package net.schlaubi.ultimatediscord.spigot;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
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

    private Main plugin;
    public static HashMap<String, String> users = new HashMap<>();

    public CommandDiscord(Main plugin) {
        this.plugin = plugin;
    }

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

    // Needed for async
    public interface IsUserLinkedCallback {
        void onQueryDone(boolean result);
    }

    // Command handler
    @Override
    public boolean onCommand(CommandSender sender, Command name, String lable, String[] args) {
        // Check if player is sending the command
        if(sender instanceof Player){
            FileConfiguration cfg = Main.getConfiguration(); // Get config
            Player player = (Player) sender;

            // Show discord link if no arguments
            if(args.length == 0){
                player.sendMessage(cfg.getString("Messages.discordlink").replace("&", "§"));
                return false;

                // Show help if more than 1 argument
            }else if(args.length > 1){
                player.sendMessage(cfg.getString("Messages.help").replace("%nl", "\n").replace("&", "§"));
            }

            // Reload command handler
            if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("discord.reload")) {
                    player.sendMessage("§7[§Discord§7]§a Settings reloaded");
                }
                // Verify command handler
            } else if (args[0].equalsIgnoreCase("verify")) {

                // Check if code exists
                if (users.containsKey(player.getName())) {
                    player.sendMessage(cfg.getString("Messages.running").replace("&", "§").replace("%code%", users.get(player.getName())));
                    return true;
                }

                // Check if user is already linked
                isUserLinkedHelper(player, result -> {
                    if (result) {
                        player.sendMessage(cfg.getString("Messages.verified").replace("&", "§"));

                    } else {
                        // Setup verify code

                        // Code generator
                        users.put(player.getName(), generateString());

                        // Tell the player
                        String code = users.get(player.getName());
                        String chatMessageString = cfg.getString("Messages.verify").replace("&", "§").replace("%code%", code);
                        TextComponent message = new TextComponent(chatMessageString);

                        // Make the message click on copy
                        message.setClickEvent( new ClickEvent( ClickEvent.Action.COPY_TO_CLIPBOARD, "!verify " + code ) );
                        message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( cfg.getString("Messages.clickToCopy") ).create() ) );

                        player.spigot().sendMessage(message);

                        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                            if (users.containsKey(player.getName())) {
                                users.remove(player.getName());
                            }
                        }, 60 * 1000);
                    }
                });
                // Unlink command handler
            } else if (args[0].equalsIgnoreCase("unlink")) {

                // Check if user is *not* linked
                isUserLinkedHelper(player, result -> {
                    if (!result) {
                        player.sendMessage(cfg.getString("Messages.notverified").replace("&", "§"));

                        // Setup unlink process
                    } else {
                        // Generate code
                        users.put(player.getName(), generateString());

                        // Inform the player
                        String code = users.get(player.getName());
                        String chatMessageString = cfg.getString("Messages.unverify").replace("&", "§").replace("%code%", code);
                        TextComponent message = new TextComponent(chatMessageString);

                        // Make the message click on copy
                        message.setClickEvent( new ClickEvent( ClickEvent.Action.COPY_TO_CLIPBOARD, "!unlink " + code ) );
                        message.setHoverEvent( new HoverEvent( HoverEvent.Action.SHOW_TEXT, new ComponentBuilder( cfg.getString("Messages.clickToCopy") ).create() ) );

                        player.spigot().sendMessage(message);

                        Bukkit.getScheduler().runTaskLater(Main.instance, () -> {
                            if (users.containsKey(player.getName())) {
                                users.remove(player.getName());
                            }
                        }, 60 * 1000);
                    }
                });

                // Update command handler - probably broken
            } else if (args[0].equalsIgnoreCase("update")) {
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

            // Show help on invalid argument
        } else {
            Bukkit.getConsoleSender().sendMessage("§4§l[UltimateDiscord] You must be a player to run this command");
        }

        return false;
    }

    public void isUserLinkedHelper(Player player, final IsUserLinkedCallback callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean result;

            result = MySQL.isUserLinked(player);
            // go back to the tick loop
            boolean finalResult = result;
            Bukkit.getScheduler().runTask(plugin, () -> {
                // call the callback with the result
                callback.onQueryDone(finalResult);
            });
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command name, String lable, String[] args) {
        String[] subcommands = {"reload", "verify", "unlink"/*, "update"*/};

        if(args.length == 0){
            return  Arrays.asList(subcommands);
        }

        List<String> matches = new ArrayList<>();

        if(args.length == 1){
            for (String subcommand : subcommands){
                if(subcommand.startsWith(args[0]))
                    matches.add(subcommand);
            }
        }

        return matches;
    }
}
