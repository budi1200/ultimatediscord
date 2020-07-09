package net.schlaubi.ultimatediscord.util;

import net.schlaubi.ultimatediscord.spigot.Main;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import javax.sql.DataSource;
import java.sql.*;

public class MySQL {

    private static Connection connection;
    private static GenericObjectPool gPool = null;

    public static void connect(){
        FileConfiguration cfg = Main.getConfiguration();
        String host = cfg.getString("MySQL.host");
        Integer port = cfg.getInt("MySQL.port");
        String user = cfg.getString("MySQL.user");
        String database = cfg.getString("MySQL.database");
        String password = cfg.getString("MySQL.password");


        try
        {
            connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&autoReconnectForPools=true&interactiveClient=true&characterEncoding=UTF-8", user, password);
            Bukkit.getConsoleSender().sendMessage("§a§l[UltimateDiscord]MySQL connection success");
        }
        catch (SQLException e)
        {
            Bukkit.getConsoleSender().sendMessage("§4§l[UltimateDiscord]MySQL connection failed");
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public DataSource setUpPool() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");

        FileConfiguration cfg = Main.getConfiguration();
        String host = cfg.getString("MySQL.host");
        int port = cfg.getInt("MySQL.port");
        String user = cfg.getString("MySQL.user");
        String database = cfg.getString("MySQL.database");
        String password = cfg.getString("MySQL.password");

        String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&autoReconnectForPools=true&interactiveClient=true&characterEncoding=UTF-8";

        // Creates an Instance of GenericObjectPool That Holds Our Pool of Connections Object!
        gPool = new GenericObjectPool();
        gPool.setMaxActive(5);

        // Creates a ConnectionFactory Object Which Will Be Use by the Pool to Create the Connection Object!
        ConnectionFactory cf = new DriverManagerConnectionFactory(dbUrl, user, password);

        // Creates a PoolableConnectionFactory That Will Wraps the Connection Object Created by the ConnectionFactory to Add Object Pooling Functionality!
        PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, gPool, null, null, false, true);
        return new PoolingDataSource(gPool);
    }

    public GenericObjectPool getConnectionPool() {
        return gPool;
    }

    // This Method Is Used To Print The Connection Pool Status
    private void printDbStatus() {
        System.out.println("Max.: " + getConnectionPool().getMaxActive() + "; Active: " + getConnectionPool().getNumActive() + "; Idle: " + getConnectionPool().getNumIdle());
    }

    private static boolean isConnected(){
        return connection != null;
    }

    public static void disconnect(){
        if(!isConnected()){
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void createDatabase()
    {
        try
        {
            if (connection.isClosed()) {
                connect();
            }
            PreparedStatement ps = connection.prepareStatement("CREATE TABLE IF NOT EXISTS ultimatediscord( `id` INT NOT NULL AUTO_INCREMENT , `uuid` TEXT NOT NULL , `discordid` TEXT, `previouslyLinked` INT NOT NULL, PRIMARY KEY (`id`)) ENGINE = InnoDB;");
            ps.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static boolean isUserLinked(Player player)
    {
        try
        {
            if (connection.isClosed()) {
                connect();
            }
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid =?");
            ps.setString(1, player.getName());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return (!rs.getString("discordid").equals("null"));
            }else{
                return false;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isFirstLink(Player player){
        try
        {
            if (connection.isClosed()) {
                connect();
            }
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid =?");
            ps.setString(1, player.getName());
            ResultSet rs = ps.executeQuery();
            if(rs.next()){
                return (rs.getInt("previouslyLinked") == 0);
            }else{
                return true;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static void createUser(String player, String identity)
    {
        try
        {
            if (connection.isClosed()) {
                connect();
            }

            PreparedStatement ps = connection.prepareStatement("INSERT INTO ultimatediscord(`uuid`,`discordid`, `previouslyLinked`) VALUES (?, ?, 1)");
            ps.setString(1, player);
            ps.setString(2, identity);
            ps.execute();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public static void updateUser(String identity)
    {
        try
        {
            if (connection.isClosed()) {
                connect();
            }

            PreparedStatement ps = connection.prepareStatement("UPDATE ultimatediscord SET `discordid`=?, `previouslyLinked`=1");
            ps.setString(1, identity);
            ps.execute();

        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }


    public static String getValue(Player player, String type)
    {
        try
        {
            if (connection.isClosed()) {
                connect();
            }
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid = ?");
            ps.setString(1, player.getName());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(type);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static String getValue(String identity, String type)
    {
        try
        {
            if (connection.isClosed()) {
                connect();
            }
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM ultimatediscord WHERE discordid = ?");
            ps.setString(1, identity);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(type);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void unlinkUser(Player player)
    {
        try
        {
            if (connection.isClosed()) {
                connect();
            }
            PreparedStatement ps = connection.prepareStatement("UPDATE ultimatediscord SET `discordid`='null', `previouslyLinked`=1 WHERE uuid=?");
            ps.setString(1, player.getName());
            ps.execute();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

}
