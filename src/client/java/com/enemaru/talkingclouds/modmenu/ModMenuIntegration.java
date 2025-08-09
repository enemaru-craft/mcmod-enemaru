package com.enemaru.talkingclouds.modmenu;

import com.enemaru.talkingclouds.config.TalkingcloudsConfig;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;

public class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> AutoConfig.getConfigScreen(TalkingcloudsConfig.class, parent).get();
    }
}
