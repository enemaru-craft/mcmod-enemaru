package com.enemaru.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public final class TrainCommand {
    private static final double SPACING = 14.0; // 両間隔

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("train")
                .then(CommandManager.literal("spawn")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerPlayerEntity player = source.getPlayerOrThrow();
                            MinecraftServer server = player.getServer();

                            Vec3d base = player.getPos();
                            float yawCmd = MathHelper.wrapDegrees(player.getYaw());

                            summonTrain(base, yawCmd, server, source);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(CommandManager.literal("run")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerPlayerEntity player = source.getPlayerOrThrow();
                            MinecraftServer server = player.getServer();
                            runTrain(server, source);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(CommandManager.literal("stop")
                        .executes(context -> {
                            ServerCommandSource source = context.getSource();
                            ServerPlayerEntity player = source.getPlayerOrThrow();
                            MinecraftServer server = player.getServer();
                            stopTrain(server, source);
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
    public static void summonTrain(Vec3d base, float yaw, MinecraftServer server, ServerCommandSource source){
        Vec3d dir = dirFromCmdYaw(yaw);
        // 各両のスポーン座標
        Vec3d p0 = base;                               // 最後尾
        Vec3d p1 = base.add(dir.multiply(SPACING));    // 中間（客車）
        Vec3d p2 = base.add(dir.multiply(SPACING * 2)); // 先頭（動力車）

        run(server, source, summonLoco(p2, yaw));
        run(server, source, summonTail(p0, yaw + 180f));
        run(server, source, summonPassenger(p1, yaw));
    }

    public static void runTrain(MinecraftServer server, ServerCommandSource source){
        run(server, source, "execute if entity @e[type=minecraft:armor_stand,tag=rptrain_loco,tag=rptrain_linker] run scoreboard players set @e[type=minecraft:armor_stand,tag=rptrain_loco,tag=rptrain_linker] rptrain_tgtspeed 30");
    }

    public static void stopTrain(MinecraftServer server, ServerCommandSource source){
        run(server, source, "execute if entity @e[type=minecraft:armor_stand,tag=rptrain_loco,tag=rptrain_linker] run scoreboard players set @e[type=minecraft:armor_stand,tag=rptrain_loco,tag=rptrain_linker] rptrain_tgtspeed 0");
    }

    private static void run(MinecraftServer server, ServerCommandSource src, String cmdNoSlash) {
        server.getCommandManager().executeWithPrefix(
                src.withWorld(src.getWorld()),
                cmdNoSlash
        );
    }

    private static String summonLoco(Vec3d p, float yawDeg) {
        return "summon minecraft:turtle " + coords(p) +
                " {Rotation:["+fmtYaw(yawDeg)+"f,0f],NoGravity:1b,Silent:1b,Invulnerable:1b,CustomNameVisible:0b,Tags:[\"rptrain_trainspawn\"],NoAI:1b," +
                "CustomName:'{\"text\":\"rptrain_spawn_moderntramloco\"}',DeathLootTable:\"trains115_gshn28:empty\"," +
                "ActiveEffects:[{Id:14b,Amplifier:1b,Duration:9999,ShowParticles:0b}]}";
    }

    private static String summonPassenger(Vec3d p, float yawDeg) {
        return "summon minecraft:turtle " + coords(p) +
                " {Rotation:["+fmtYaw(yawDeg)+"f,0f],NoGravity:1b,Silent:1b,Invulnerable:1b,CustomNameVisible:0b,Tags:[\"rptrain_trainspawn\"],NoAI:1b," +
                "CustomName:'{\"text\":\"rptrain_spawn_moderntrampasscar\"}',DeathLootTable:\"trains115_gshn28:empty\"," +
                "ActiveEffects:[{Id:14b,Amplifier:1b,Duration:9999,ShowParticles:0b}]}";
    }

    private static String summonTail(Vec3d p, float yawDeg) {
        return "summon minecraft:turtle " + coords(p) +
                " {Rotation:["+fmtYaw(yawDeg)+"f,0f],NoGravity:1b,Silent:1b,Invulnerable:1b,CustomNameVisible:0b,Tags:[\"rptrain_trainspawn\"],NoAI:1b," +
                "CustomName:'{\"text\":\"rptrain_spawn_moderntramfakeloco\"}',DeathLootTable:\"trains115_gshn28:empty\"," +
                "ActiveEffects:[{Id:14b,Amplifier:1b,Duration:9999,ShowParticles:0b}]}";
    }

    private static String coords(Vec3d p) {
        return fmt(p.x) + " " + fmt(p.y) + " " + fmt(p.z);
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.3f", v);
    }

    private static String fmtYaw(float yaw) {
        return String.format(java.util.Locale.ROOT, "%.1f", wrapYaw(yaw));
    }

    private static float wrapYaw(float yaw) {
        return MathHelper.wrapDegrees(yaw);
    }

    private static Vec3d dirFromCmdYaw(float yawDeg) {
        double r = Math.toRadians(yawDeg);
        double x = -Math.sin(r); // yaw=0 → x=0
        double z =  Math.cos(r); // yaw=0 → z=+1（=+Z）
        return new Vec3d(x, 0, z).normalize();
    }
}
