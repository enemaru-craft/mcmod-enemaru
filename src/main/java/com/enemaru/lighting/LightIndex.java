package com.enemaru.lighting;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * WorldLightIndexへの簡易アクセス用ヘルパー。
 */
public class LightIndex {

    public static void register(World world, int channel, BlockPos pos) {
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) return;
        WorldLightIndex.get(serverWorld).register(channel, pos);
    }

    public static void unregister(World world, int channel, BlockPos pos) {
        if (world.isClient || !(world instanceof ServerWorld serverWorld)) return;
        WorldLightIndex.get(serverWorld).unregister(channel, pos);
    }
}

