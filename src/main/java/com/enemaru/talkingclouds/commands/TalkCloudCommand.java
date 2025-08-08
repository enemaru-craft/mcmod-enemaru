package com.enemaru.talkingclouds.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import com.enemaru.talkingclouds.Network;

public final class TalkCloudCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("talkcloud")
                .requires(Permissions.require("talkingclouds.command.talkcloud", 1))
                .then(CommandManager.argument("entity", EntityArgumentType.entity())
                        .then(CommandManager.argument("jsonText", TextArgumentType.text(registryAccess))
                                .executes(context -> {
                                    var entity = EntityArgumentType.getEntity(context, "entity");
                                    var jsonText = TextArgumentType.getTextArgument(context, "jsonText");
                                    var text = Texts.parse(context.getSource(), jsonText, entity, 0);

                                    TalkCloudCommand.sendBubble(entity, text);

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .then(CommandManager.argument("formattedText", MessageArgumentType.message())
                                .executes(context -> {
                                    var entity = EntityArgumentType.getEntity(context, "entity");
                                    var text = MessageArgumentType.getMessage(context, "formattedText");

                                    TalkCloudCommand.sendBubble(entity, text);

                                    return Command.SINGLE_SUCCESS;
                                }))));
    }

    private static void sendBubble(Entity entity, Text text) {
        var receivers = entity.getWorld().getPlayers();
        for (var player : receivers) {
            Network.sendBubbleData((ServerPlayerEntity) player, entity, text);
        }
    }
}
