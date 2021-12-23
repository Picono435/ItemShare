package com.gmail.picono435.itemshare.commands;

import com.gmail.picono435.itemshare.api.ItemServer;
import com.gmail.picono435.itemshare.api.ItemShareAPI;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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
                player.sendMessage(Text.of("§cYou cannot send AIR for other server"), true);
                return 1;
            }
            if(ItemShareAPI.saveStackInDatabase(mainHand, player.getUuid(), toServer)) {
                player.sendMessage(Text.of("§aYou sent the item successfully to the server."), true);
                player.getMainHandStack().setCount(0);
            } else {
                player.sendMessage(Text.of("§cWe were not able to send the item to the server. Try again later."), true);
            }
            return 1;
        } catch(Exception ex) {
            ex.printStackTrace();
            return 1;
        }
    }

}
