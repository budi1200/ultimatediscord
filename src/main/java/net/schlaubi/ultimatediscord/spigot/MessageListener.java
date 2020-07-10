package net.schlaubi.ultimatediscord.spigot;

import com.sun.org.apache.xerces.internal.xs.StringList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.milkbowl.vault.permission.Permission;
import net.schlaubi.ultimatediscord.util.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MessageListener extends ListenerAdapter {

    private Main plugin;
    private static HashMap<String, String> users = CommandDiscord.users;

    public MessageListener(Main plugin) {
        this.plugin = plugin;
    }

    private static String getUser(String code) {
        for(String key : users.keySet()) {
            String value = users.get(key);
            if(value.equalsIgnoreCase(code)) {
                return key;
            }
        }
        return null;
    }

    public interface IsFirstLinkCallback {
        void onQueryDone(boolean result);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        FileConfiguration cfg = Main.getConfiguration(); // Get config

        if(event.isFromType(ChannelType.TEXT) && !event.getAuthor().isBot()){
            Guild guild = event.getGuild();

            // TODO: Lock to channel option?
            MessageChannel channel = event.getChannel();

            Message msgObject = event.getMessage();
            String message = msgObject.getContentDisplay();
            String[] args = message.split(" ");

            // Verify command handler
            if(args[0].equalsIgnoreCase("!verify")){

                if(users.containsValue(args[1])){
                    Member member = event.getMember();
                    String mcUser = getUser(args[1]);
                    Role defaultRole = guild.getRoleById(cfg.getString("Roles.defaultrole"));

                    try{
                        guild.addRoleToMember(member, defaultRole).queue();
                        member.modifyNickname(mcUser).queue();

                        isFirstLinkHelper(mcUser, result -> {
                            if(result){
                                List<String> firstLinkCommands = cfg.getStringList("Discord.firstLinkCommands");

                                createUserHelper(mcUser, event.getAuthor().getId());

                                commandHandler(firstLinkCommands, mcUser);
                            }else{
                                updateUserHelper(event.getAuthor().getId());
                            }
                        });
                    }catch (Exception e){

                        channel.sendMessage("Error linking accounts").queue();
                        e.printStackTrace();

                    }finally {

                        channel.sendMessage(cfg.getString("Messages.success").replace("%discord%", event.getAuthor().getName())).queue();

                        Player p = Bukkit.getPlayer(getUser(args[1]));
                        p.sendMessage(cfg.getString("Messages.linked").replace("&", "§"));

                        // Delete the verify message
                        msgObject.delete().queue();
                    }

                    users.remove(mcUser);

                }else{
                    // Delete the message
                    msgObject.delete().queue();
                    channel.sendMessage(cfg.getString("Messages.invalidcode")).queue();
                }
                // Unlink message handler
            }else if(args[0].equalsIgnoreCase("!unlink")){
                if(users.containsValue(args[1])){
                    Member member = event.getMember();
                    Role defaultRole = guild.getRoleById(cfg.getString("Roles.defaultrole"));

                    try{
                        guild.removeRoleFromMember(member, defaultRole).queue();

                        channel.sendMessage(cfg.getString("Messages.successUnlink").replace("%discord%", event.getAuthor().getName())).queue();

                        // Delete the verify message
                        msgObject.delete().queue();

                        Player p = Bukkit.getPlayer(getUser(args[1]));

                        unlinkUserHelper(p);

                        p.sendMessage(cfg.getString("Messages.unlinked").replace("&", "§"));
                        users.remove(getUser(args[1]));
                    }catch (Exception e){
                        channel.sendMessage("Error unlinking accounts").queue();
                    }

                }else{
                    // Delete the message
                    msgObject.delete().queue();
                    channel.sendMessage(cfg.getString("Messages.invalidcode")).queue();
                }
            }
        }
    }

    public void unlinkUserHelper(Player p){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            MySQL.unlinkUser(p);
        });
    }

    public void updateUserHelper(String authorId){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            MySQL.updateUser(authorId);
        });
    }

    public void createUserHelper(String mcUser, String authorId){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            MySQL.createUser(mcUser, authorId);
        });
    }

    public void isFirstLinkHelper(String mcUser, final IsFirstLinkCallback callback){
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean result = false;

            result = MySQL.isFirstLink(Bukkit.getPlayer(mcUser));
            // go back to the tick loop
            boolean finalResult = result;
            Bukkit.getScheduler().runTask(plugin, () -> {
                // call the callback with the result
                callback.onQueryDone(finalResult);
            });
        });
    }

    public void commandHandler(List<String> commands, String playerName){
        ConsoleCommandSender consoleSender = Bukkit.getConsoleSender();

        for(String command : commands){
            Bukkit.getScheduler().callSyncMethod( this.plugin, () -> Bukkit.dispatchCommand(consoleSender, command.replaceAll("%player%", playerName)));
        }
    }
}
