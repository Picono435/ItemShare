package com.gmail.picono435.itemshare;

import com.gmail.picono435.itemshare.commands.ServerArgumentType;
import com.gmail.picono435.itemshare.config.ItemShareConfig;
import com.gmail.picono435.itemshare.commands.ItemShareCommand;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.shedaniel.architectury.event.events.CommandRegistrationEvent;
import me.shedaniel.architectury.event.events.LifecycleEvent;
import me.shedaniel.architectury.event.events.PlayerEvent;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraft.command.argument.ArgumentTypes;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemShare {

    public static ItemShareConfig itemConfig;
    public static HikariDataSource hikari;
    public static HikariConfig config = new HikariConfig();

    public static final String MOD_ID = "itemshare";

    public static void init() {
        AutoConfig.register(ItemShareConfig.class, Toml4jConfigSerializer::new);
        itemConfig = AutoConfig.getConfigHolder(ItemShareConfig.class).getConfig();

        CommandRegistrationEvent.EVENT.register((commandDispatcher, commandSelection) -> {
            initHikari();
            System.out.println("Registering ItemShare commands...");

            if(!ArgumentTypes.hasClass(ServerArgumentType.server())) {
                ArgumentTypes.register("itemserver", ServerArgumentType.class, new ConstantArgumentSerializer<>(ServerArgumentType::server));
            }
            ItemShareCommand.register(commandDispatcher);
        });

        LifecycleEvent.SERVER_STOPPING.register((listener) -> {
            hikari.close();
        });

        PlayerEvent.PLAYER_JOIN.register((listener) -> {
            if(hikari == null) return;
            try(Connection conn = hikari.getConnection()) {
                PreparedStatement stm = conn.prepareStatement("SELECT * FROM itemshare WHERE player=? AND to_server=?");
                stm.setString(1, listener.getUuidAsString());
                stm.setString(2, itemConfig.getServerName());
                ResultSet rs = stm.executeQuery();
                while(rs.next()) {
                    int id = rs.getInt("id");
                    String itemString = rs.getString("item");
                    int amount = rs.getInt("amount");
                    String nbtString = rs.getString("nbt");
                    Item item = Registry.ITEM.get(new Identifier(itemString));
                    ItemStack stack = new ItemStack(item, amount);
                    if(nbtString != null) {
                        StringNbtReader stringNbtReader = new StringNbtReader(new StringReader(nbtString));
                        stack.setTag(stringNbtReader.parseCompound());
                    }
                    listener.giveItemStack(stack);
                    PreparedStatement stmDelete = conn.prepareStatement("DELETE FROM itemshare WHERE id=?");
                    stmDelete.setInt(1, id);
                    stmDelete.executeUpdate();
                    stmDelete.close();
                    /*} else {
                        listener.sendMessage(new LiteralText("§cCould not send you an item, make sure you don't have your inventory full."), true);
                    }*/
                }
                stm.close();
            } catch (SQLException | CommandSyntaxException throwables) {
                listener.sendMessage(new LiteralText("§cCould not send you an item, please contact an Administrator."), true);
                throwables.printStackTrace();
            }
        });
    }

    private static void initHikari() {
        try {
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setJdbcUrl("jdbc:mysql://" + itemConfig.mysqlConfig.getAddress() + ":" + itemConfig.mysqlConfig.getPort() + "/" + itemConfig.mysqlConfig.getDatabaseName());
            config.setUsername(itemConfig.mysqlConfig.getUsername());
            config.setPassword(itemConfig.mysqlConfig.getPassword());
            config.addDataSourceProperty("prepStmtCacheSize", 250);
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            config.addDataSourceProperty("cachePrepStmts", true);
            config.addDataSourceProperty("useServerPrepStmts", true);
            hikari = new HikariDataSource(config);
            try(Connection conn = hikari.getConnection()) {
                PreparedStatement stm = conn.prepareStatement("CREATE TABLE IF NOT EXISTS itemshare (`id` INT NOT NULL AUTO_INCREMENT, `player` VARCHAR(255) NOT NULL, `from_server` VARCHAR(255) NOT NULL, `to_server` VARCHAR(255) NOT NULL, `item` VARCHAR(255) NOT NULL, `amount` INT NOT NULL, `nbt` VARCHAR(255), PRIMARY KEY (`id`));");
                stm.execute();
                stm.close();
                stm = conn.prepareStatement("CREATE TABLE IF NOT EXISTS itemshare_servers (`server` VARCHAR(255) NOT NULL, PRIMARY KEY (`server`));");
                stm.execute();
                stm.close();
                stm = conn.prepareStatement("REPLACE INTO itemshare_servers (`server`) VALUES (?);");
                stm.setString(1, itemConfig.getServerName());
                stm.execute();
                stm.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }


}
