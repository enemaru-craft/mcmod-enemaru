package com.enemaru;

import com.enemaru.block.ModBlocks;
import com.enemaru.blockentity.*;
import com.enemaru.commands.EnemaruCommand;
import com.enemaru.commands.SessionCommand;
import com.enemaru.commands.TrainCommand;
import com.enemaru.item.ModItems;
import com.enemaru.networking.payload.SendBubbleS2CPayload;
import com.enemaru.networking.payload.EquipmentRequestC2SPayload;
import com.enemaru.networking.payload.ThermalUpdateC2SPayload;
import com.enemaru.power.PowerNetwork;
import com.enemaru.screenhandler.ControlPanelScreenHandler;
import com.enemaru.talkingclouds.commands.TalkCloudCommand;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
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

        // Payloadを登録
        PayloadTypeRegistry.playS2C().register(SendBubbleS2CPayload.ID, SendBubbleS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EquipmentRequestC2SPayload.ID, EquipmentRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ThermalUpdateC2SPayload.ID, ThermalUpdateC2SPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(EquipmentRequestC2SPayload.ID, (payload, context) -> {
            ServerWorld world = context.player().getServerWorld();
            PowerNetwork network = PowerNetwork.get(world);

            network.sendWorldState(payload.equipment(), payload.enable(), world);
        });

        ServerPlayNetworking.registerGlobalReceiver(ThermalUpdateC2SPayload.ID, (payload, context) -> {
            ServerWorld world = context.player().getServerWorld();
            PowerNetwork network = PowerNetwork.get(world);
            network.setThermalPower(payload.power());
        });


        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TalkCloudCommand.register(dispatcher, registryAccess);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            TrainCommand.register(dispatcher, registryAccess);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SessionCommand.register(dispatcher, registryAccess);
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            EnemaruCommand.register(dispatcher, registryAccess);
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
            PowerNetwork net = PowerNetwork.get(sw);

            if (be instanceof StreetLightBlockEntity sle) {
                net.registerStreetLight(sle);
            } else if (be instanceof SeaLanternLampBlockEntity sleLantern) {
                net.registerSeaLantern(sleLantern);
            } else if (be instanceof GlowstoneLampBlockEntity glow) {
                net.registerGlowstone(glow);
            } else if (be instanceof EndRodLampBlockEntity endRod) {
                net.registerEndRodLamp(endRod);
            } else if (be instanceof StationEndRodBlockEntity stationEndRod) {
                net.registerStationEndRod(stationEndRod);
            }
        });


        // ブロックエンティティが「アンロード」されるとき
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((be, world) -> {
            if (!(world instanceof ServerWorld sw)) return;
            PowerNetwork net = PowerNetwork.get(sw);

            if (be instanceof StreetLightBlockEntity sle) {
                net.unregisterStreetLight(sle);
            } else if (be instanceof SeaLanternLampBlockEntity sleLantern) {
                net.unregisterSeaLantern(sleLantern);
            } else if (be instanceof GlowstoneLampBlockEntity glow) {
                net.unregisterGlowstone(glow);
            } else if (be instanceof EndRodLampBlockEntity endRod) {
                net.unregisterEndRodLamp(endRod);
            } else if (be instanceof StationEndRodBlockEntity stationEndRod) {
                net.unregisterStationEndRod(stationEndRod);
            }
        });

        // プレイヤーがサーバーに参加したときにコントロールパネルを配布する
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            Item controlPanel = Registries.ITEM.get(Identifier.of(MOD_ID, "control_panel"));

            boolean alreadyHas = false;
            for (int i = 0; i < player.getInventory().size(); i++) {
                ItemStack s = player.getInventory().getStack(i);
                if (!s.isEmpty() && s.getItem() == controlPanel) {
                    alreadyHas = true;
                    break;
                }
            }
            if(!alreadyHas){
                ItemStack stack = new ItemStack(controlPanel, 1);
                player.giveItemStack(stack);
            }
        });

        LOGGER.info("Hello Fabric world!");
    }
}