package com.gmail.picono435.itemshare.fabric;

import com.gmail.picono435.itemshare.ItemShare;
import net.fabricmc.api.ModInitializer;

public class ItemShareFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ItemShare.init();
    }
}
