package com.enemaru;

import com.enemaru.screen.ControlPanelScreen;
import com.enemaru.talkingclouds.NetworkClient;
import com.enemaru.talkingclouds.config.TalkingcloudsConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class EnemaruClient implements ClientModInitializer {
    public static TalkingcloudsConfig CONFIG;
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        HandledScreens.register(Enemaru.PANEL_SCREEN_HANDLER, ControlPanelScreen::new);

        AutoConfig.register(TalkingcloudsConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(TalkingcloudsConfig.class).getConfig();

        NetworkClient.registerClientHandlers();


	}
}