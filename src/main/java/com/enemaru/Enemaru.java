package com.enemaru;

import com.enemaru.block.ModBlocks;
import com.enemaru.blockentity.ModBlockEntities;
import com.enemaru.blockentity.StreetLightBlockEntity;
import com.enemaru.item.ModItems;
import com.enemaru.networking.payload.SetStreetLightsC2SPayload;
import com.enemaru.power.PowerNetwork;
import com.enemaru.screenhandler.ControlPanelScreenHandler;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Enemaru implements ModInitializer {
    public static final String MOD_ID = "enemaru";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final ScreenHandlerType<ControlPanelScreenHandler> PANEL_SCREEN_HANDLER =
            Registry.register(
                    Registries.SCREEN_HANDLER,
                    Identifier.of(Enemaru.MOD_ID, "control_panel"),
                    new ScreenHandlerType<>(
                            ControlPanelScreenHandler::new,
                            FeatureSet.empty()
                    )
            );

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        ModItems.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();

        PayloadTypeRegistry.playC2S().register(SetStreetLightsC2SPayload.ID, SetStreetLightsC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SetStreetLightsC2SPayload.ID, (payload, context) -> {
            boolean enabled = payload.value();
            // PowerNetworkのsetStreetlightsEnabledメソッドを呼び出して
            // サーバー側の街灯の有効/無効を更新
            ServerWorld world = context.player().getServerWorld();
            PowerNetwork network = PowerNetwork.get(world);
            network.setStreetlightsEnabled(enabled);
        });

        // サーバーのワールド毎ティック（20ティック＝1秒ごと）に
        // PowerNetwork.tick(...) を呼び出す
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world instanceof ServerWorld sw) {
                PowerNetwork.get(sw).tick(sw);
            }
        });
        // サーバー側でブロックエンティティが「ロード」されるたびに呼ばれる
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((be, world) -> {
            if (!(world instanceof ServerWorld sw)) return;
            if (be instanceof StreetLightBlockEntity sle) {
                // 登録して最新のフラグを反映
                PowerNetwork net = PowerNetwork.get(sw);
                net.registerStreetLight(sle);
                sle.updatePowered(net.isStreetlightsEnabled());
            }
        });

        // ブロックエンティティが「アンロード」されるとき
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, world) -> {
            if (!(world instanceof ServerWorld sw)) return;
            if (be instanceof StreetLightBlockEntity sle) {
                PowerNetwork.get(sw).unregisterStreetLight(sle);
            }
        });
        LOGGER.info("Hello Fabric world!");
    }
}