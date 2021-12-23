package com.gmail.picono435.itemshare.api;

import com.gmail.picono435.itemshare.ItemShare;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ItemShareAPI {

    public static boolean saveStackInDatabase(ItemStack stack, UUID uuid, ItemServer toServer) {
        Map<String, Object> result = serializeStack(stack);
        try(Connection conn = ItemShare.hikari.getConnection()) {
            PreparedStatement stm = conn.prepareStatement("INSERT INTO itemshare (player,from_server,to_server,item,amount,nbt) VALUES (?,?,?,?,?,?)");
            stm.setString(1, uuid.toString());
            stm.setString(2, ItemShare.itemConfig.getServerName());
            stm.setString(3, toServer.getName());
            stm.setString(4, (String) result.get("id"));
            stm.setInt(5, (Integer) result.get("amount"));
            if(result.containsKey("nbt")) {
                stm.setString(6, (String) result.get("nbt"));
            } else {
                stm.setString(6, null);
            }
            stm.execute();
            stm.close();
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }
    }

    public static Map<String, Object> serializeStack(ItemStack stack) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();

        result.put("id", Registry.ITEM.getId(stack.getItem()).toString());

        result.put("amount", stack.getCount());

        if (stack.hasTag()) {
            //StringNbtWriter nbtWriter = new StringNbtWriter();
            result.put("nbt", stack.getTag().toString());
        }

        return result;
    }

}
