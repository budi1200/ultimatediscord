package net.schlaubi.ultimatediscord.util;

import net.schlaubi.ultimatediscord.spigot.Main;
import okhttp3.ConnectionPool;
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

    // TODO: Everything probably could be optimized //

    @SuppressWarnings("unused")
    public static DataSource setUpPool() throws Exception {
        Class.forName("com.mysql.jdbc.Driver");

        FileConfiguration cfg = Main.getConfiguration();
        String host = cfg.getString("MySQL.host");
        int port = cfg.getInt("MySQL.port");
        String user = cfg.getString("MySQL.user");
        String database = cfg.getString("MySQL.database");
        String password = cfg.getString("MySQL.password");

        String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&autoReconnectForPools=true&interactiveClient=true&characterEncoding=UTF-8";

        // Creates an Instance of GenericObjectPool That Holds Our Pool of Connections Object!
        //private static Connection connection;
        GenericObjectPool gPool = new GenericObjectPool();
        gPool.setMaxActive(5);

        // Creates a ConnectionFactory Object Which Will Be Use by the Pool to Create the Connection Object!
        ConnectionFactory cf = new DriverManagerConnectionFactory(dbUrl, user, password);

        // Creates a PoolableConnectionFactory That Will Wraps the Connection Object Created by the ConnectionFactory to Add Object Pooling Functionality!
        PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, gPool, null, null, false, true);
        return new PoolingDataSource(gPool);
    }

    public static void createDatabase()
    {
        Connection connObj = null;
        PreparedStatement ps = null;

        try {
            DataSource dataSource = setUpPool();

            // Performing Database Operation!
            connObj = dataSource.getConnection();

            ps = connObj.prepareStatement("CREATE TABLE IF NOT EXISTS ultimatediscord( `id` INT NOT NULL AUTO_INCREMENT , `uuid` TEXT NOT NULL , `discordid` TEXT, `previouslyLinked` INT NOT NULL, PRIMARY KEY (`id`)) ENGINE = InnoDB;");
            ps.execute();
        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        } finally {
            try {
                // Closing PreparedStatement Object
                if(ps != null) {
                    ps.close();
                }
                // Closing Connection Object
                if(connObj != null) {
                    connObj.close();
                }
            } catch(Exception sqlException) {
                sqlException.printStackTrace();
            }
        }
    }

    public static boolean isUserLinked(Player player)
    {
        Connection connObj = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean returnVal = false;
        try {
            DataSource dataSource = setUpPool();

            // Performing Database Operation!
            connObj = dataSource.getConnection();

            ps = connObj.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid =?");
            ps.setString(1, player.getName());
            rs = ps.executeQuery();
            if(rs.next()){
                returnVal = (!rs.getString("discordid").equals("null"));
            }
        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        } finally {
            cleanUp(connObj, ps, rs);
        }
        return returnVal;
    }

    public static boolean isFirstLink(Player player){
        Connection connObj = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean returnVal = false;

        try {
            DataSource dataSource = setUpPool();

            // Performing Database Operation!
            connObj = dataSource.getConnection();

            ps = connObj.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid =?");
            ps.setString(1, player.getName());
            rs = ps.executeQuery();

            if(rs.next()){
                returnVal = (rs.getInt("previouslyLinked") == 0);
            }else{
                returnVal = true;
            }
        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        } finally {
            cleanUp(connObj, ps, rs);
        }
        return returnVal;
    }

    public static void createUser(String player, String identity)
    {
        Connection connObj = null;
        PreparedStatement ps = null;

        try {
            DataSource dataSource = setUpPool();

            // Performing Database Operation!
            connObj = dataSource.getConnection();

            ps = connObj.prepareStatement("INSERT INTO ultimatediscord(`uuid`,`discordid`, `previouslyLinked`) VALUES (?, ?, 1)");
            ps.setString(1, player);
            ps.setString(2, identity);
            ps.execute();

        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        } finally {
            try {
                // Closing PreparedStatement Object
                if(ps != null) {
                    ps.close();
                }
                // Closing Connection Object
                if(connObj != null) {
                    connObj.close();
                }
            } catch(Exception sqlException) {
                sqlException.printStackTrace();
            }
        }
    }

    public static void updateUser(String identity)
    {
        Connection connObj = null;
        PreparedStatement ps = null;

        try {
            DataSource dataSource = setUpPool();

            // Performing Database Operation!
            connObj = dataSource.getConnection();

            ps = connObj.prepareStatement("UPDATE ultimatediscord SET `discordid`=?, `previouslyLinked`=1");
            ps.setString(1, identity);
            ps.execute();

        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        } finally {
            try {
                // Closing PreparedStatement Object
                if(ps != null) {
                    ps.close();
                }
                // Closing Connection Object
                if(connObj != null) {
                    connObj.close();
                }
            } catch(Exception sqlException) {
                sqlException.printStackTrace();
            }
        }
    }


    public static String getValue(Player player, String type)
    {
        Connection connObj = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String returnVal = null;

        try {
            DataSource dataSource = setUpPool();

            // Performing Database Operation!
            connObj = dataSource.getConnection();

            ps = connObj.prepareStatement("SELECT * FROM ultimatediscord WHERE uuid = ?");
            ps.setString(1, player.getName());
            rs = ps.executeQuery();
            if (rs.next()) {
                returnVal = rs.getString(type);
            }
        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        } finally {
            cleanUp(connObj, ps, rs);
        }
        return returnVal;
    }

    public static String getValue(String identity, String type)
    {
        Connection connObj = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String returnVal = null;

        try {
            DataSource dataSource = setUpPool();

            // Performing Database Operation!
            connObj = dataSource.getConnection();

            ps = connObj.prepareStatement("SELECT * FROM ultimatediscord WHERE discordid = ?");
            ps.setString(1, identity);
            rs = ps.executeQuery();
            if (rs.next()) {
                returnVal = rs.getString(type);
            }
        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        } finally {
            cleanUp(connObj, ps, rs);
        }
        return returnVal;
    }

    private static void cleanUp(Connection connObj, PreparedStatement ps, ResultSet rs) {
        try {
            // Closing ResultSet Object
            if(rs != null) {
                rs.close();
            }
            // Closing PreparedStatement Object
            if(ps != null) {
                ps.close();
            }
            // Closing Connection Object
            if(connObj != null) {
                connObj.close();
            }
        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        }
    }

    public static void unlinkUser(Player player)
    {
        Connection connObj = null;
        PreparedStatement ps = null;

        try {
            DataSource dataSource = setUpPool();

            // Performing Database Operation!
            connObj = dataSource.getConnection();

            ps = connObj.prepareStatement("UPDATE ultimatediscord SET `discordid`='null', `previouslyLinked`=1 WHERE uuid=?");
            ps.setString(1, player.getName());
            ps.execute();

        } catch(Exception sqlException) {
            sqlException.printStackTrace();
        } finally {
            try {
                // Closing PreparedStatement Object
                if(ps != null) {
                    ps.close();
                }
                // Closing Connection Object
                if(connObj != null) {
                    connObj.close();
                }
            } catch(Exception sqlException) {
                sqlException.printStackTrace();
            }
        }
    }
}
