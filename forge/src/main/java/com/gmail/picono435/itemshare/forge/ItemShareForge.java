package com.gmail.picono435.itemshare.forge;

import me.shedaniel.architectury.platform.forge.EventBuses;
import com.gmail.picono435.itemshare.ItemShare;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ItemShare.MOD_ID)
public class ItemShareForge {

    public ItemShareForge() {
        EventBuses.registerModEventBus(ItemShare.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ItemShare.init();
    }

}
