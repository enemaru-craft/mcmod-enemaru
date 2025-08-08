package com.enemaru.talkingclouds;

import com.enemaru.networking.payload.SendBubbleS2CPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class Network {
    public static void sendBubbleData(ServerPlayerEntity player, Entity entity, Text text) {
        SendBubbleS2CPayload payload = new SendBubbleS2CPayload(entity.getId(), text);
        ServerPlayNetworking.send(player, payload);
    }
}
