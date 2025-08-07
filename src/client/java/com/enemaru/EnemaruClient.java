package com.enemaru;

import com.enemaru.screen.ControlPanelScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

public class EnemaruClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
        HandledScreens.register(Enemaru.PANEL_SCREEN_HANDLER, ControlPanelScreen::new);
	}
}