package com.enemaru.lighting;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.Queue;

/**
 * ライトの状態更新をキューイングし、分割して適用する。
 * TPSスパイクを防ぐため、1tick当たりの更新数を制限。
 */
public class LightingUpdateQueue {
    private static final int MAX_UPDATES_PER_TICK = 256; // 1tickあたりの最大更新数

    private final Queue<LightUpdate> queue = new LinkedList<>();
    private final ServerWorld world;

    public LightingUpdateQueue(ServerWorld world) {
        this.world = world;
    }

    /**
     * 更新をキューに追加。
     */
    public void enqueue(long posLong, int targetLevel) {
        queue.offer(new LightUpdate(posLong, targetLevel));
    }

    /**
     * キューを処理（毎tick呼ばれる想定）。
     */
    public void processTick() {
        int processed = 0;

        while (!queue.isEmpty() && processed < MAX_UPDATES_PER_TICK) {
            LightUpdate update = queue.poll();
            if (update == null) break;

            applyUpdate(update);
            processed++;
        }
    }

    /**
     * 単一の更新を適用。
     * 現在のLEVELと目標LEVELが異なる場合のみ更新。
     */
    private void applyUpdate(LightUpdate update) {
        BlockPos pos = BlockPos.fromLong(update.posLong);

        // チャンクがロードされているか確認
        if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) {
            return;
        }

        BlockState state = world.getBlockState(pos);

        // 管理対象ライトかチェック
        if (!(state.getBlock() instanceof AbstractManagedLightBlock)) {
            return;
        }

        // 現在のレベル
        int currentLevel = state.get(AbstractManagedLightBlock.LEVEL);

        // 目標レベルと同じなら更新不要
        if (currentLevel == update.targetLevel) {
            return;
        }

        // レベル更新
        world.setBlockState(pos, state.with(AbstractManagedLightBlock.LEVEL, update.targetLevel), 3);
    }

    /**
     * キューに残っている更新数を取得。
     */
    public int getPendingCount() {
        return queue.size();
    }

    /**
     * キューをクリア。
     */
    public void clear() {
        queue.clear();
    }

    private static class LightUpdate {
        final long posLong;
        final int targetLevel;

        LightUpdate(long posLong, int targetLevel) {
            this.posLong = posLong;
            this.targetLevel = targetLevel;
        }
    }
}

