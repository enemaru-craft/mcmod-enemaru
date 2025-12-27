package com.enemaru.lighting.commands;

import com.enemaru.lighting.LightingManager;
import com.enemaru.lighting.WorldLightPolicy;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * ライティングシステムの制御コマンド。
 *
 * /lighting policy <channel> <percent> [seed] - チャンネルのポリシー設定
 * /lighting status - 現在の状態表示
 * /lighting recalculate <channel> - チャンネルの再計算
 */
public class LightingCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("lighting")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("policy")
                        .then(CommandManager.argument("channel", IntegerArgumentType.integer(0, 15))
                                .then(CommandManager.argument("percent", IntegerArgumentType.integer(0, 100))
                                        .executes(context -> executeSetPolicy(
                                                context,
                                                IntegerArgumentType.getInteger(context, "channel"),
                                                IntegerArgumentType.getInteger(context, "percent"),
                                                0L
                                        ))
                                        .then(CommandManager.argument("seed", IntegerArgumentType.integer())
                                                .executes(context -> executeSetPolicy(
                                                        context,
                                                        IntegerArgumentType.getInteger(context, "channel"),
                                                        IntegerArgumentType.getInteger(context, "percent"),
                                                        IntegerArgumentType.getInteger(context, "seed")
                                                ))
                                        )
                                )
                        )
                )
                .then(CommandManager.literal("status")
                        .executes(LightingCommand::executeStatus)
                )
                .then(CommandManager.literal("recalculate")
                        .then(CommandManager.argument("channel", IntegerArgumentType.integer(0, 15))
                                .executes(context -> executeRecalculate(
                                        context,
                                        IntegerArgumentType.getInteger(context, "channel")
                                ))
                        )
                )
        );
    }

    private static int executeSetPolicy(CommandContext<ServerCommandSource> context, int channel, int percent, long seed) {
        ServerWorld world = context.getSource().getWorld();
        WorldLightPolicy policy = WorldLightPolicy.get(world);

        // パーセントを0-10000スケールに変換
        int percent0to10000 = percent * 100;

        policy.setPercent(channel, percent0to10000);
        policy.setPatternSeed(channel, seed);

        context.getSource().sendFeedback(
                () -> Text.literal(String.format("§aチャンネル %d のポリシーを設定: %d%% (seed: %d)",
                        channel, percent, seed)),
                true
        );

        // 即座に再計算
        LightingManager.get(world).recalculateChannel(channel);

        context.getSource().sendFeedback(
                () -> Text.literal("§7再計算を開始しました..."),
                false
        );

        return 1;
    }

    private static int executeStatus(CommandContext<ServerCommandSource> context) {
        ServerWorld world = context.getSource().getWorld();
        WorldLightPolicy policy = WorldLightPolicy.get(world);
        LightingManager manager = LightingManager.get(world);

        context.getSource().sendFeedback(
                () -> Text.literal("§e=== ライティングシステム状態 ==="),
                false
        );

        for (int channel = 0; channel < 16; channel++) {
            int percent = policy.getPercent(channel);
            long seed = policy.getPatternSeed(channel);

            final int ch = channel;
            final int pct = percent / 100;
            final long sd = seed;

            context.getSource().sendFeedback(
                    () -> Text.literal(String.format("§7チャンネル %d: %d%% (seed: %d)", ch, pct, sd)),
                    false
            );
        }

        int pending = manager.getPendingUpdates();
        context.getSource().sendFeedback(
                () -> Text.literal(String.format("§7保留中の更新: %d", pending)),
                false
        );

        return 1;
    }

    private static int executeRecalculate(CommandContext<ServerCommandSource> context, int channel) {
        ServerWorld world = context.getSource().getWorld();
        LightingManager manager = LightingManager.get(world);

        manager.recalculateChannel(channel);

        context.getSource().sendFeedback(
                () -> Text.literal(String.format("§aチャンネル %d の再計算を開始しました", channel)),
                true
        );

        return 1;
    }
}

