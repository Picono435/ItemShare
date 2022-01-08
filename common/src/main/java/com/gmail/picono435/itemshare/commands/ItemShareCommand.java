package com.gmail.picono435.itemshare.commands;

import com.gmail.picono435.itemshare.ItemShare;
import com.gmail.picono435.itemshare.api.ItemServer;
import com.gmail.picono435.itemshare.api.ItemShareAPI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;

public class ItemShareCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("itemshare")
                        .then(
                                CommandManager.argument("to", ServerArgumentType.server())
                                        .executes(context ->
                                        runCommand(context.getSource().getPlayer(), context)
                                )
                        ));
    }

    private static int runCommand(ServerPlayerEntity player, CommandContext<ServerCommandSource> context) {
        try {
            ItemServer toServer = ServerArgumentType.getItemShare(context, "to");
            ItemStack mainHand = player.getMainHandStack();
            if(mainHand.getCount() == 0) {
                player.sendMessage(new LiteralText("§cYou cannot send AIR for other server"), true);
                return 1;
            }
            if(ItemShare.itemConfig.isWhitelist()) {
                if(!ItemShare.itemConfig.getWhitelist().contains(Registry.ITEM.getId(mainHand.getItem()).toString())) {
                    player.sendMessage(new LiteralText("§cYou cannot send this item to the selected server."), true);
                    return 1;
                }
            } else {
                if(ItemShare.itemConfig.getWhitelist().contains(Registry.ITEM.getId(mainHand.getItem()).toString())) {
                    player.sendMessage(new LiteralText("§cYou cannot send this item to the selected server."), true);
                    return 1;
                }
            }
            if(ItemShareAPI.saveStackInDatabase(mainHand, player.getUuid(), toServer)) {
                player.sendMessage(new LiteralText("§aYou sent the item successfully to the server."), true);
                player.getMainHandStack().setCount(0);
            } else {
                player.sendMessage(new LiteralText("§cWe were not able to send the item to the server. Try again later."), true);
            }
            return 1;
        } catch(Exception ex) {
            ex.printStackTrace();
            return 1;
        }
    }

}
