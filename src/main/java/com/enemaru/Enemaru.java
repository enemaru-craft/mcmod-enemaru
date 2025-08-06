package com.enemaru;

import com.enemaru.block.ModBlocks;
import com.enemaru.blockentity.ModBlockEntities;
import com.enemaru.item.ModItems;
import com.enemaru.power.PowerNetwork;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Enemaru implements ModInitializer {
	public static final String MOD_ID = "enemaru";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModItems.initialize();
		ModBlocks.initialize();
		ModBlockEntities.initialize();
		// サーバーのワールド毎ティック（20ティック＝1秒ごと）に
		// PowerNetwork.tick(...) を呼び出す
		ServerTickEvents.END_WORLD_TICK.register(world -> {
			if (world instanceof ServerWorld sw) {
				PowerNetwork.get(sw).tick(sw);
			}
		});
		LOGGER.info("Hello Fabric world!");
	}
}