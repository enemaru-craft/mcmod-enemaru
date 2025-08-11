package com.enemaru.talkingclouds;

import com.enemaru.networking.payload.SendBubbleS2CPayload;
import com.enemaru.talkingclouds.api.BubblesContainer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;

public class NetworkClient {
    @Environment(EnvType.CLIENT)
    public static void registerClientHandlers() {
        ClientPlayNetworking.registerGlobalReceiver(SendBubbleS2CPayload.ID, (payload, context) -> {
            var entityId = payload.entityId();
            var text = payload.text();
            var isPersistent = payload.isPersistent();
            var isReset = payload.isReset();

            context.client().execute(() -> {
                var instance = MinecraftClient.getInstance();
                if (instance.world == null) return;

                var entity = instance.world.getEntityById(entityId);
                if (entity == null) return;
                if(isReset){
                    BubblesContainer.of(entity).resetAll();
                }else{
                    BubblesContainer.of(entity).addBubble(text, true, isPersistent);
                }
            });
        });
    }
}
