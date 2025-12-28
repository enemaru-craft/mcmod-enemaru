package com.enemaru.lighting;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;

/**
 * ライティングシステムの統合管理クラス。
 * ワールドごとにインスタンスを保持し、更新キュー・イベント処理を統括。
 */
public class LightingManager {
    private static final Map<ServerWorld, LightingManager> INSTANCES = new HashMap<>();

    private final ServerWorld world;
    private final LightingUpdateQueue updateQueue;

    private LightingManager(ServerWorld world) {
        this.world = world;
        this.updateQueue = new LightingUpdateQueue(world);
    }

    public static LightingManager get(ServerWorld world) {
        return INSTANCES.computeIfAbsent(world, LightingManager::new);
    }

    public static void remove(ServerWorld world) {
        INSTANCES.remove(world);
    }

    /**
     * 毎tick呼ばれる処理。更新キューを処理。
     */
    public void tick() {
        updateQueue.processTick();
    }

    /**
     * チャンクロード時の処理。
     * 該当チャンクのライトを再計算してキューに追加。
     */
    public void onChunkLoad(ChunkPos chunkPos) {
        WorldLightIndex index = WorldLightIndex.get(world);
        WorldLightPolicy policy = WorldLightPolicy.get(world);
        long worldSeed = world.getSeed();

        // 全チャンネルについて処理
        for (int channel : index.getChannels()) {
            LongSet positions = index.getPositions(channel, chunkPos);
            if (positions.isEmpty()) continue;

            int percent = policy.getPercent(channel);
            long patternSeed = policy.getPatternSeed(channel);

            // 目標レベルを計算
            Map<Long, Integer> targetLevels = ChunkLightingPlanner.planChunkLighting(
                    positions, percent, patternSeed, worldSeed, channel
            );

            // 更新キューに追加
            for (Map.Entry<Long, Integer> entry : targetLevels.entrySet()) {
                updateQueue.enqueue(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 特定チャンネルのポリシー変更時、全チャンクを再計算。
     */
    public void recalculateChannel(int channel) {
        WorldLightIndex index = WorldLightIndex.get(world);
        WorldLightPolicy policy = WorldLightPolicy.get(world);
        long worldSeed = world.getSeed();

        int percent = policy.getPercent(channel);
        long patternSeed = policy.getPatternSeed(channel);

        // チャンネルの全チャンクを再計算
        for (long chunkLong : index.getChunks(channel)) {
            ChunkPos chunkPos = new ChunkPos(chunkLong);

            // チャンクがロードされている場合のみ処理
            if (!world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
                continue;
            }

            LongSet positions = index.getPositions(channel, chunkPos);
            if (positions.isEmpty()) continue;

            Map<Long, Integer> targetLevels = ChunkLightingPlanner.planChunkLighting(
                    positions, percent, patternSeed, worldSeed, channel
            );

            for (Map.Entry<Long, Integer> entry : targetLevels.entrySet()) {
                updateQueue.enqueue(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 現在ロード中のチャンクをスキャンして、管理対象ライトをインデックスに登録。
     * 既存ワールド移行用。
     */
    public int scanLoadedChunks() {
        // TODO: 実装（コマンドから呼ばれる）
        return 0;
    }

    /**
     * 単一ブロックの点灯状態を即座に更新。
     * ブロック配置時やチャンネル変更時に使用。
     */
    public void updateSingleLight(int channel, long posLong) {
        WorldLightIndex index = WorldLightIndex.get(world);
        WorldLightPolicy policy = WorldLightPolicy.get(world);
        long worldSeed = world.getSeed();

        // posLongからBlockPosを復元してChunkPosを取得
        net.minecraft.util.math.BlockPos pos = net.minecraft.util.math.BlockPos.fromLong(posLong);
        ChunkPos chunkPos = new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4);

        // チャンクがロードされていない場合は何もしない
        if (!world.isChunkLoaded(chunkPos.x, chunkPos.z)) {
            return;
        }

        // 同じチャンク内の全ライトを取得
        LongSet positions = index.getPositions(channel, chunkPos);
        if (positions.isEmpty()) {
            // インデックスが空の場合（まだ登録されていない可能性）
            return;
        }

        int percent = policy.getPercent(channel);
        long patternSeed = policy.getPatternSeed(channel);

        // 目標レベルを計算
        Map<Long, Integer> targetLevels = ChunkLightingPlanner.planChunkLighting(
                positions, percent, patternSeed, worldSeed, channel
        );

        // 該当ブロックの目標レベルを取得
        Integer targetLevel = targetLevels.get(posLong);
        if (targetLevel != null) {
            // 即座に適用（キューをスキップ）
            net.minecraft.block.BlockState state = world.getBlockState(pos);
            if (state.getBlock() instanceof AbstractManagedLightBlock) {
                int currentLevel = state.get(AbstractManagedLightBlock.LEVEL);
                if (currentLevel != targetLevel) {
                    world.setBlockState(pos, state.with(AbstractManagedLightBlock.LEVEL, targetLevel), 3);
                }
            }
        }
    }

    public int getPendingUpdates() {
        return updateQueue.getPendingCount();
    }
}

