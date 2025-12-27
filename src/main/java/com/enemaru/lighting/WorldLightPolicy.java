package com.enemaru.lighting;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

/**
 * ワールドごとの照明ポリシー。
 * 各チャンネルの点灯割合とパターンシードを保存。
 */
public class WorldLightPolicy extends PersistentState {
    private static final String DATA_NAME = "enemaru_light_policy";
    private static final int MAX_CHANNELS = 16;

    // チャンネルごとの点灯割合 (0-10000) 100.00%
    private final int[] percent0to10000 = new int[MAX_CHANNELS];

    // チャンネルごとのパターンシード（点灯パターン固定/シャッフル用）
    private final long[] patternSeed = new long[MAX_CHANNELS];

    public WorldLightPolicy() {
        super();
        // デフォルト: 全チャンネル100%点灯、シード0
        for (int i = 0; i < MAX_CHANNELS; i++) {
            percent0to10000[i] = 10000;
            patternSeed[i] = 0L;
        }
    }

    public static WorldLightPolicy get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new Type<>(WorldLightPolicy::new, WorldLightPolicy::fromNbt, null),
                DATA_NAME
        );
    }

    /**
     * 点灯割合を設定 (0-10000)
     */
    public void setPercent(int channel, int percent) {
        if (channel < 0 || channel >= MAX_CHANNELS) return;
        if (percent < 0) percent = 0;
        if (percent > 10000) percent = 10000;

        percent0to10000[channel] = percent;
        markDirty();
    }

    /**
     * 点灯割合を取得 (0-10000)
     */
    public int getPercent(int channel) {
        if (channel < 0 || channel >= MAX_CHANNELS) return 10000;
        return percent0to10000[channel];
    }

    /**
     * パターンシードを設定
     */
    public void setPatternSeed(int channel, long seed) {
        if (channel < 0 || channel >= MAX_CHANNELS) return;
        patternSeed[channel] = seed;
        markDirty();
    }

    /**
     * パターンシードを取得
     */
    public long getPatternSeed(int channel) {
        if (channel < 0 || channel >= MAX_CHANNELS) return 0L;
        return patternSeed[channel];
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putIntArray("percents", percent0to10000);
        nbt.putLongArray("seeds", patternSeed);
        return nbt;
    }

    public static WorldLightPolicy fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        WorldLightPolicy policy = new WorldLightPolicy();

        int[] percents = nbt.getIntArray("percents");
        if (percents.length == MAX_CHANNELS) {
            System.arraycopy(percents, 0, policy.percent0to10000, 0, MAX_CHANNELS);
        }

        long[] seeds = nbt.getLongArray("seeds");
        if (seeds.length == MAX_CHANNELS) {
            System.arraycopy(seeds, 0, policy.patternSeed, 0, MAX_CHANNELS);
        }

        return policy;
    }
}

