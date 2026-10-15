package com.enemaru;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enemaru.block.ModBlocks;
import com.enemaru.blockentity.ModBlockEntities;
import com.enemaru.commands.EnemaruCommand;
import com.enemaru.commands.SessionCommand;
import com.enemaru.commands.TrainCommand;
import com.enemaru.item.ModItems;
import com.enemaru.lighting.LightingManager;
import com.enemaru.lighting.commands.LightingCommand;
import com.enemaru.networking.payload.EquipmentPercentC2SPayload;
import com.enemaru.networking.payload.EquipmentRequestC2SPayload;
import com.enemaru.networking.payload.SendBubbleS2CPayload;
import com.enemaru.networking.payload.ThermalUpdateC2SPayload;
import com.enemaru.power.PowerNetwork;
import com.enemaru.screenhandler.ControlPanelScreenHandler;
import com.enemaru.talkingclouds.commands.TalkCloudCommand;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class Enemaru implements ModInitializer {
    public static final String MOD_ID = "enemaru";

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
        ModItems.initialize();
        ModBlocks.initialize();
        ModBlockEntities.initialize();

        registerPayloadTypes();
        registerServerReceivers();
        registerCommands();
        registerLifecycleEvents();

        LOGGER.info("Hello Fabric world!");
    }

    private void registerPayloadTypes() {
        PayloadTypeRegistry.playS2C().register(SendBubbleS2CPayload.ID, SendBubbleS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EquipmentRequestC2SPayload.ID, EquipmentRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(EquipmentPercentC2SPayload.ID, EquipmentPercentC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ThermalUpdateC2SPayload.ID, ThermalUpdateC2SPayload.CODEC);
    }

    private void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(EquipmentRequestC2SPayload.ID, (payload, context) -> {
            ServerWorld world = context.player().getServerWorld();
            PowerNetwork network = PowerNetwork.get(world);

            network.sendWorldState(payload.equipment(), payload.enable()?100:0, world);
        });

        ServerPlayNetworking.registerGlobalReceiver(EquipmentPercentC2SPayload.ID, (payload, context) -> {
            ServerWorld world = context.player().getServerWorld();
            PowerNetwork network = PowerNetwork.get(world);

            network.sendWorldState(payload.equipment(), payload.percent(), world);
        });

        ServerPlayNetworking.registerGlobalReceiver(ThermalUpdateC2SPayload.ID, (payload, context) -> {
            ServerWorld world = context.player().getServerWorld();
            PowerNetwork network = PowerNetwork.get(world);
            network.setThermalPower(payload.power());
        });
    }

    private void registerCommands() {
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

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            LightingCommand.register(dispatcher, registryAccess);
        });
    }

    private void registerLifecycleEvents() {
        // 各サーバーワールドの毎tick終了時に、電力連携と照明をこの順で更新する
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world instanceof ServerWorld sw) {
                PowerNetwork.get(sw).tick(sw);
                // LightingManagerのtick処理
                LightingManager.get(sw).tick();
            }
        });

        // チャンクロード時にライティング再計算
        ServerChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            if (world instanceof ServerWorld sw) {
                LightingManager.get(sw).onChunkLoad(chunk.getPos());
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            giveControlPanelIfMissing(player);

            var network = PowerNetwork.get(player.getServerWorld());
            network.enableShouldUpdateTexts();
            network.enableForceLightUpdate();

            initializePlayerOnJoin(player, server);
        });
    }

    private void giveControlPanelIfMissing(ServerPlayerEntity player) {
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
    }

    private void initializePlayerOnJoin(ServerPlayerEntity player, MinecraftServer server) {
        player.getServerWorld().setWeather(1000000, 0, false, false);
        server.getCommandManager().executeWithPrefix(player.getCommandSource().withLevel(4), "/gamemode adventure");
        server.getCommandManager().executeWithPrefix(player.getCommandSource().withLevel(4), "/effect give @s resistance 999999 255 true");
        server.getCommandManager().executeWithPrefix(player.getCommandSource().withLevel(4), "/effect give @s saturation 999999 255 true");
        player.getAbilities().allowFlying = true;
        player.sendAbilitiesUpdate();
    }
}
