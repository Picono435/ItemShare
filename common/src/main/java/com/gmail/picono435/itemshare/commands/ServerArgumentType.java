package com.gmail.picono435.itemshare.commands;

import com.gmail.picono435.itemshare.ItemShare;
import com.gmail.picono435.itemshare.api.ItemServer;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.serialize.ArgumentSerializer;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerArgumentType implements ArgumentType<ItemServer> {
    private static final Collection<String> EXAMPLES;
    private static final DynamicCommandExceptionType INVALID_SERVER_EXCEPTION;
    private List<String> serverList = new ArrayList<>();

    @Override
    public ItemServer parse(StringReader reader) throws CommandSyntaxException {
        if(serverList.isEmpty()) {
            try(Connection conn = ItemShare.hikari.getConnection()) {
                PreparedStatement stm = conn.prepareStatement("SELECT * FROM itemshare_servers");
                ResultSet rs = stm.executeQuery();
                while(rs.next()) {
                    String name = rs.getString("server");
                    serverList.add(name);
                }
                stm.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        String name = reader.readString();
        if(serverList.contains(name)) {
            return new ItemServer(name);
        } else {
            throw INVALID_SERVER_EXCEPTION.create(name);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if(serverList.isEmpty()) {
            try(Connection conn = ItemShare.hikari.getConnection()) {
                PreparedStatement stm = conn.prepareStatement("SELECT * FROM itemshare_servers");
                ResultSet rs = stm.executeQuery();
                while(rs.next()) {
                    String name = rs.getString("server");
                    serverList.add(name);
                }
                stm.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        return context.getSource() instanceof CommandSource ? CommandSource.suggestMatching(serverList.toArray(new String[0]), builder) : Suggestions.empty();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public static ServerArgumentType server() {
        return new ServerArgumentType();
    }

    public static ItemServer getItemShare(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        ItemServer itemServer = (ItemServer)context.getArgument(name, ItemServer.class);
        try(Connection conn = ItemShare.hikari.getConnection()) {
            PreparedStatement stm = conn.prepareStatement("SELECT * FROM itemshare_servers WHERE server=?");
            stm.setString(1, itemServer.getName());
            ResultSet rs = stm.executeQuery();
            if(rs.next()) {
                stm.close();
                return itemServer;
            } else {
                stm.close();
                return itemServer;
                //throw INVALID_SERVER_EXCEPTION.create(itemServer);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            throw INVALID_SERVER_EXCEPTION.create(itemServer);
        }
    }

    static {
        /*EXAMPLES = (Collection) Stream.of(World.OVERWORLD, World.NETHER).map((key) -> {
            return key.getValue().toString();
        }).collect(Collectors.toList());*/
        EXAMPLES = Collections.emptyList();
        INVALID_SERVER_EXCEPTION = new DynamicCommandExceptionType((id) -> {
            return new TranslatableText("argument.itemshare.text.invalid", new Object[]{id});
        });
    }
}
