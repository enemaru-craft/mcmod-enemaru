package com.enemaru.commands;

import com.enemaru.power.PowerNetwork;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

public final class EnemaruCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("enemaru")
                .then(CommandManager.literal("debug")
                        .then(CommandManager.literal("enable")
                                .executes(context -> {
                                    ServerWorld world = context.getSource().getWorld();
                                    PowerNetwork network = PowerNetwork.get(world);
                                    network.setDebug(true);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(CommandManager.literal("disable")
                                .executes(context -> {
                                    ServerWorld world = context.getSource().getWorld();
                                    PowerNetwork network = PowerNetwork.get(world);
                                    network.setDebug(false);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );
    }
}
