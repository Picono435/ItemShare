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
    private static final Collection<String> EXAMPLES = new ArrayList<>();
    private static final DynamicCommandExceptionType INVALID_SERVER_EXCEPTION;

    @Override
    public ItemServer parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readString();
        if(EXAMPLES.contains(name)) {
            System.out.println("YEY");
            return new ItemServer(name);
        } else {
            System.out.println("SADJE");
            throw INVALID_SERVER_EXCEPTION.create(name);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        System.out.println(context.getSource() instanceof CommandSource);
        System.out.println(EXAMPLES);
        System.out.println(CommandSource.suggestMatching(EXAMPLES, builder));
        return context.getSource() instanceof CommandSource ? CommandSource.suggestMatching(EXAMPLES, builder) : Suggestions.empty();
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
        try(Connection conn = ItemShare.hikari.getConnection()) {
            PreparedStatement stm = conn.prepareStatement("SELECT * FROM itemshare_servers");
            ResultSet rs = stm.executeQuery();
            while(rs.next()) {
                String name = rs.getString("server");
                EXAMPLES.add(name);
            }
            stm.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        /*EXAMPLES = (Collection) Stream.of(World.OVERWORLD, World.NETHER).map((key) -> {
            return key.getValue().toString();
        }).collect(Collectors.toList());*/
        //EXAMPLES = Collections.emptyList();
        INVALID_SERVER_EXCEPTION = new DynamicCommandExceptionType((id) -> {
            return new TranslatableText("argument.itemshare.text.invalid", new Object[]{id});
        });
    }
}
