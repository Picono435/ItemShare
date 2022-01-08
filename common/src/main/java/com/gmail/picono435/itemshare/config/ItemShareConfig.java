package com.gmail.picono435.itemshare.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Config(name = "itemshare")
public class ItemShareConfig implements ConfigData {

    String serverName = "myServerName";

    public String getServerName() {
        return serverName;
    }

    boolean isWhitelist = false;

    public boolean isWhitelist() {
        return isWhitelist;
    }

    List<String> whitelist = Collections.singletonList("minecraft:barrier");

    public List<String> getWhitelist() {
        return whitelist;
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
