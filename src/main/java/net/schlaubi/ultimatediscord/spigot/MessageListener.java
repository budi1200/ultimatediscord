package net.schlaubi.ultimatediscord.spigot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.milkbowl.vault.permission.Permission;
import net.schlaubi.ultimatediscord.util.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class MessageListener extends ListenerAdapter {

    private static HashMap<String, String> users = CommandDiscord.users;

    private static String getUser(String code) {
        for(String key : users.keySet()) {
            String value = users.get(key);
            if(value.equalsIgnoreCase(code)) {
                return key;
            }
        }
        return null;
    }


    @Override
    public void onMessageReceived(MessageReceivedEvent event){
        FileConfiguration cfg = Main.getConfiguration();
        JDA jda = event.getJDA();

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
                    Permission perms = Main.getPermissions();
                    Role defaultRole = guild.getRoleById(cfg.getString("Roles.defaultrole"));

                    assert defaultRole != null;
                    guild.addRoleToMember(member, defaultRole).queue();

                    channel.sendMessage(cfg.getString("Messages.success").replace("%discord%", event.getAuthor().getName())).queue();

                    // Delete the verify message
                    msgObject.delete().queue();

                    MySQL.createUser(getUser(args[1]), event.getAuthor().getId());
                    users.remove(getUser(args[1]));
                }else{
                    // Delete the message
                    msgObject.delete().queue();
                    channel.sendMessage(cfg.getString("Messages.invalidcode")).queue();
                }
            }else if(args[0].equalsIgnoreCase("!unlink")){
                if(users.containsValue(args[1])){
                    Member member = event.getMember();
                    Permission perms = Main.getPermissions();
                    Role defaultRole = guild.getRoleById(cfg.getString("Roles.defaultrole"));

                    assert defaultRole != null;
                    guild.removeRoleFromMember(member, defaultRole).queue();

                    channel.sendMessage(cfg.getString("Messages.successUnlink").replace("%discord%", event.getAuthor().getName())).queue();

                    // Delete the verify message
                    msgObject.delete().queue();

                    Player p = Bukkit.getPlayer(getUser(args[1]));
                    MySQL.unlinkUser(p);
                    p.sendMessage(cfg.getString("Messages.unlinked").replace("&", "§"));
                    users.remove(getUser(args[1]));
                }else{
                    // Delete the message
                    msgObject.delete().queue();
                    channel.sendMessage(cfg.getString("Messages.invalidcode")).queue();
                }
            }
        }

    }
    /*public void onMessageReceived(MessageReceivedEvent event) {
        FileConfiguration cfg = Main.getConfiguration();
        if(event.isFromType(ChannelType.PRIVATE)){
            String message = event.getMessage().getContentDisplay();
            String[] args = message.split(" ");
            JDA jda = event.getJDA();
            if(args[0].equalsIgnoreCase("!verify")) {
                if (users.containsValue(args[1])) {
                    Permission perms = Main.getPermissions();
                    Guild guild = Main.jda.getGuilds().get(0);
                    Role defaultrole = guild.getRoleById(cfg.getString("Roles.defaultrole"));
                    Role role = guild.getRoleById(cfg.getString("Roles.group." + perms.getPrimaryGroup(Bukkit.getPlayer(getUser(args[1])))));
                    guild.addRoleToMember(guild.getMember(event.getAuthor()), role).queue();
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            guild.addRoleToMember(guild.getMember(event.getAuthor()), defaultrole).queue();
                        }
                    },1000);
                    event.getPrivateChannel().sendMessage(cfg.getString("Messages.success").replace("%discord%", event.getAuthor().getName())).queue();
                    MySQL.createUser(getUser(args[1]), event.getAuthor().getId());
                    users.remove(getUser(args[1]));
                } else {
                    event.getPrivateChannel().sendMessage(cfg.getString("Messages.invalidcode")).queue();
                }
            } else if(args[0].equalsIgnoreCase("!roles")){
                StringBuilder sb = new StringBuilder();
                for(Role r : jda.getGuilds().get(0).getRoles()){
                    sb.append("[R: " + r.getName() + "(" + r.getId() + ")");
                }
                event.getPrivateChannel().sendMessage(sb.toString()).queue();
            }
        }
    }*/

    private void verifyCommandHandler(){

    }
}
