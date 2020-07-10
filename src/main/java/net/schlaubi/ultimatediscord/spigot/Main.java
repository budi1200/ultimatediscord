package net.schlaubi.ultimatediscord.spigot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.milkbowl.vault.permission.Permission;
import net.schlaubi.ultimatediscord.util.MySQL;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.io.File;

public class Main extends JavaPlugin {

    public static JDA jda;
    public static Main instance;
    private static Permission perms;

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        startBot();
        try {
            MySQL.setUpPool();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MySQL.createDatabase();
        setupPermissions();
        this.getCommand("discord").setExecutor(new CommandDiscord(this));
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
    }

    private void startBot() {
        FileConfiguration cfg = getConfiguration();

        try
        {
            // Setup the bot
            JDA jda = JDABuilder.createDefault(cfg.getString("Discord.token")) // The token of the account that is logging in.
                    .addEventListeners(new MessageListener(this))   // An instance of a class that will handle events.
                    .setAutoReconnect(true)
                    .setActivity(Activity.watching(cfg.getString("Discord.game")))
                    .build();
            jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.
            System.out.println("Finished Building JDA!");
        }
        catch (LoginException | InterruptedException e)
        {
            getLogger().severe("§4§l[UltimateDiscord] Invalid discord token");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
    }

    private void loadConfig() {
        File f = new File("plugins/UltimateDiscord", "config.yml");
        if(!f.exists())
            saveDefaultConfig();
    }

    public static FileConfiguration getConfiguration(){
        File f = new File("plugins/UltimateDiscord", "config.yml");
        return YamlConfiguration.loadConfiguration(f);
    }

    public static Permission getPermissions(){
        return perms;
    }
}
