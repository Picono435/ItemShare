package com.gmail.picono435.itemshare.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "itemshare")
public class ItemShareConfig implements ConfigData {

    String serverName = "myServerName";

    public String getServerName() {
        return serverName;
    }

    @ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
    public MySQLConfig mysqlConfig = new MySQLConfig();

    public static class MySQLConfig {
        String address = "localhost";
        int port = 3306;
        String databaseName = "server";
        String username = "myuser";
        String password = "";

        public String getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }
    }
}
