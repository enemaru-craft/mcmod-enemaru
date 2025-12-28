package com.enemaru.lighting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

/**
 * チャンネル管理式駅エンドロッドブロック。
 */
public class ManagedStationEndRodBlock extends AbstractManagedLightBlock {
    public static final DirectionProperty FACING = Properties.FACING;

    protected static final VoxelShape SHAPE_UP = Block.createCuboidShape(6.0, 0.0, 6.0, 10.0, 16.0, 10.0);
    protected static final VoxelShape SHAPE_DOWN = SHAPE_UP;
    protected static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(6.0, 6.0, 0.0, 10.0, 10.0, 16.0);
    protected static final VoxelShape SHAPE_SOUTH = SHAPE_NORTH;
    protected static final VoxelShape SHAPE_WEST = Block.createCuboidShape(0.0, 6.0, 6.0, 16.0, 10.0, 10.0);
    protected static final VoxelShape SHAPE_EAST = SHAPE_WEST;

    public ManagedStationEndRodBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, net.minecraft.block.ShapeContext context) {
        return switch (state.get(FACING)) {
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            case EAST -> SHAPE_EAST;
        };
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.with(FACING, mirror.apply(state.get(FACING)));
    }

    @Override
    protected int getDefaultChannel() {
        return 4; // 異なるデフォルトチャンネルを設定
    }
}
