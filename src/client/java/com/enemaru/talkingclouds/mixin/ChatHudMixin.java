package com.enemaru.talkingclouds.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.enemaru.talkingclouds.api.BubblesContainer;
import com.enemaru.talkingclouds.config.ConfigManager;

@Mixin(MessageHandler.class)
public abstract class ChatHudMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onChatMessage", at = @At("HEAD"))
    private void onChatMessageInject(SignedMessage message, GameProfile sender, MessageType.Parameters params, CallbackInfo ci) {
        var showPlayerBubbles = ConfigManager.isChatBubblesEnabled() && this.client.world != null;
        if (!showPlayerBubbles) {
            return;
        }

        var text = message.getContent();
        var player = this.client.world.getPlayerByUuid(sender.getId());

        if (player == null || (player.isMainPlayer() && !ConfigManager.isSelfBubblesEnabled())) {
            return;
        }

        BubblesContainer.of(player).addBubble(text);
    }
}
