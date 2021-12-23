package com.gmail.picono435.itemshare.fabric.hook;

import com.gmail.picono435.itemshare.config.ItemShareConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(ItemShareConfig.class, parent).get();
    }

}
