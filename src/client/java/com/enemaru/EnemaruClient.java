package com.enemaru;

import com.enemaru.blockentity.ModBlockEntities;
import com.enemaru.renderer.blockentity.CounterBlockEntityRenderer;
import com.enemaru.screen.ControlPanelScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;

import static com.enemaru.Enemaru.PANEL_SCREEN_HANDLER;

public class EnemaruClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		BlockEntityRendererFactories.register(ModBlockEntities.COUNTER_BLOCK_ENTITY, CounterBlockEntityRenderer::new);

        HandledScreens.register(Enemaru.PANEL_SCREEN_HANDLER, ControlPanelScreen::new);
	}
}