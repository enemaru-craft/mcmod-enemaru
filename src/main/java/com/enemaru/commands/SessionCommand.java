package com.enemaru.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import com.enemaru.power.PowerNetwork;
import com.enemaru.screenhandler.ControlPanelScreenHandler;
import net.minecraft.server.world.ServerWorld;

public final class SessionCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("session")
                .then(CommandManager.literal("setID")
                        .then(CommandManager.argument("id", IntegerArgumentType.integer())
                                .executes(context -> {
                                    int id = IntegerArgumentType.getInteger(context, "id");
                                    // セッションIDを設定する処理をここに追加
                                    ServerWorld world = context.getSource().getWorld();
                                    PowerNetwork network = PowerNetwork.get(world);
                                    network.setSessionId(id);
                                    // グラフの履歴をクリア
                                    ControlPanelScreenHandler.clearEnergyHistory();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
        );
    }
}
