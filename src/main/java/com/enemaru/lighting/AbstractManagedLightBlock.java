package com.enemaru.lighting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * チャンネル・レベル管理された照明ブロックの基底クラス。
 * - CHANNEL: 0-15 (どのチャンネルに属するか)
 * - LEVEL: 0/5/10/15 (光度レベル。0=OFF, 15=最大)
 */
public abstract class AbstractManagedLightBlock extends Block {
    // チャンネル: 0-15
    public static final IntProperty CHANNEL = IntProperty.of("channel", 0, 15);

    // 光度レベル: 0, 5, 10, 15 の4段階
    public static final IntProperty LEVEL = IntProperty.of("level", 0, 15);

    public AbstractManagedLightBlock(Settings settings) {
        super(settings);
        // デフォルト: サブクラスで指定されたチャンネル、消灯(LEVEL=0)
        this.setDefaultState(getDefaultState()
                .with(CHANNEL, getDefaultChannel())
                .with(LEVEL, 0)
        );
    }

    /**
     * このライトのデフォルトチャンネルを返す。
     * サブクラスでオーバーライドして種類ごとに異なるチャンネルを指定可能。
     *
     * @return デフォルトチャンネル番号 (0-15)
     */
    protected int getDefaultChannel() {
        return 0; // デフォルトはチャンネル0
    }

    /**
     * BlockStateから光度を取得するヘルパー。
     * Settings.luminance() で使用。
     */
    public static int getLuminance(BlockState state) {
        return state.get(LEVEL);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHANNEL, LEVEL);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if (!world.isClient && !oldState.isOf(this)) {
            // 新しくブロックが置かれた場合、インデックスに登録
            int channel = state.get(CHANNEL);
            LightIndex.register(world, channel, pos);

            // 1tick後に点灯状態を更新（配置が完全に完了してから）
            if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
                serverWorld.getServer().execute(() -> {
                    LightingManager.get(serverWorld).updateSingleLight(channel, pos.asLong());
                });
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!world.isClient && !newState.isOf(this)) {
            // ブロックが別のブロックに置き換えられる場合、インデックスから削除
            int channel = state.get(CHANNEL);
            LightIndex.unregister(world, channel, pos);
        }
        super.onStateReplaced(state, world, pos, newState, moved);
    }

    /**
     * チャンネルを変更する際のヘルパー。
     * 古いチャンネルから削除して新しいチャンネルに登録する。
     */
    public static void changeChannel(World world, BlockPos pos, BlockState state, int newChannel) {
        if (world.isClient) return;
        if (!(state.getBlock() instanceof AbstractManagedLightBlock)) return;

        int oldChannel = state.get(CHANNEL);
        if (oldChannel == newChannel) return;

        LightIndex.unregister(world, oldChannel, pos);
        world.setBlockState(pos, state.with(CHANNEL, newChannel), Block.NOTIFY_ALL);
        LightIndex.register(world, newChannel, pos);

        // 1tick後にライト状態を更新
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            serverWorld.getServer().execute(() -> {
                LightingManager.get(serverWorld).updateSingleLight(newChannel, pos.asLong());
            });
        }
    }
}

