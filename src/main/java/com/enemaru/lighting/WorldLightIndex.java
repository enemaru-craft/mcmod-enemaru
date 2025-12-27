package com.enemaru.lighting;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;

import java.util.*;

/**
 * ワールドごとのライト座標インデックス。
 * channel -> chunkLong -> set(posLong) の3層構造で管理。
 */
public class WorldLightIndex extends PersistentState {
    private static final String DATA_NAME = "enemaru_light_index";

    // channel -> (chunkLong -> set(posLong))
    private final Int2ObjectMap<Long2ObjectMap<LongSet>> index = new Int2ObjectOpenHashMap<>();

    public WorldLightIndex() {
        super();
    }

    public static WorldLightIndex get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new Type<>(WorldLightIndex::new, WorldLightIndex::fromNbt, null),
                DATA_NAME
        );
    }

    /**
     * ライトを登録する。
     */
    public void register(int channel, BlockPos pos) {
        long posLong = pos.asLong();
        long chunkLong = ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4);

        index.computeIfAbsent(channel, k -> new Long2ObjectOpenHashMap<>())
              .computeIfAbsent(chunkLong, k -> new LongOpenHashSet())
              .add(posLong);

        markDirty();
    }

    /**
     * ライトを削除する。
     */
    public void unregister(int channel, BlockPos pos) {
        long posLong = pos.asLong();
        long chunkLong = ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4);

        Long2ObjectMap<LongSet> channelData = index.get(channel);
        if (channelData == null) return;

        LongSet posSet = channelData.get(chunkLong);
        if (posSet == null) return;

        posSet.remove(posLong);

        // 空になったら削除
        if (posSet.isEmpty()) {
            channelData.remove(chunkLong);
            if (channelData.isEmpty()) {
                index.remove(channel);
            }
        }

        markDirty();
    }

    /**
     * 指定チャンネル・チャンクのライト座標セットを取得。
     */
    public LongSet getPositions(int channel, ChunkPos chunkPos) {
        long chunkLong = chunkPos.toLong();
        Long2ObjectMap<LongSet> channelData = index.get(channel);
        if (channelData == null) return new LongOpenHashSet();

        LongSet positions = channelData.get(chunkLong);
        return positions != null ? positions : new LongOpenHashSet();
    }

    /**
     * 指定チャンネルの全チャンク座標を取得。
     */
    public Set<Long> getChunks(int channel) {
        Long2ObjectMap<LongSet> channelData = index.get(channel);
        if (channelData == null) return Collections.emptySet();
        return channelData.keySet();
    }

    /**
     * 全チャンネルを取得。
     */
    public Set<Integer> getChannels() {
        return index.keySet();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList channelList = new NbtList();

        for (Int2ObjectMap.Entry<Long2ObjectMap<LongSet>> channelEntry : index.int2ObjectEntrySet()) {
            int channel = channelEntry.getIntKey();
            Long2ObjectMap<LongSet> chunks = channelEntry.getValue();

            NbtCompound channelNbt = new NbtCompound();
            channelNbt.putInt("channel", channel);

            NbtList chunkList = new NbtList();
            for (Long2ObjectMap.Entry<LongSet> chunkEntry : chunks.long2ObjectEntrySet()) {
                long chunkLong = chunkEntry.getLongKey();
                LongSet positions = chunkEntry.getValue();

                NbtCompound chunkNbt = new NbtCompound();
                chunkNbt.putLong("chunk", chunkLong);
                chunkNbt.putLongArray("positions", positions.toLongArray());

                chunkList.add(chunkNbt);
            }
            channelNbt.put("chunks", chunkList);
            channelList.add(channelNbt);
        }

        nbt.put("channels", channelList);
        return nbt;
    }

    public static WorldLightIndex fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        WorldLightIndex index = new WorldLightIndex();

        NbtList channelList = nbt.getList("channels", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < channelList.size(); i++) {
            NbtCompound channelNbt = channelList.getCompound(i);
            int channel = channelNbt.getInt("channel");

            NbtList chunkList = channelNbt.getList("chunks", NbtElement.COMPOUND_TYPE);
            for (int j = 0; j < chunkList.size(); j++) {
                NbtCompound chunkNbt = chunkList.getCompound(j);
                long chunkLong = chunkNbt.getLong("chunk");
                long[] positions = chunkNbt.getLongArray("positions");

                LongSet posSet = new LongOpenHashSet(positions);
                index.index.computeIfAbsent(channel, k -> new Long2ObjectOpenHashMap<>())
                           .put(chunkLong, posSet);
            }
        }

        return index;
    }
}

