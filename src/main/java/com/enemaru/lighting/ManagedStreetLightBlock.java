package com.enemaru.lighting;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

/**
 * チャンネル管理式街灯ブロック。
 */
public class ManagedStreetLightBlock extends AbstractManagedLightBlock {
    public static final BooleanProperty HANGING = Properties.HANGING;

    // ランタンの当たり判定（バニラLanternと同じ）
    private static final VoxelShape STANDING_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 7.0, 11.0),
            Block.createCuboidShape(6.0, 7.0, 6.0, 10.0, 9.0, 10.0)
    );
    private static final VoxelShape HANGING_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(5.0, 1.0, 5.0, 11.0, 8.0, 11.0),
            Block.createCuboidShape(6.0, 8.0, 6.0, 10.0, 10.0, 10.0)
    );

    public ManagedStreetLightBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(HANGING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HANGING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getSide();
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos().offset(direction.getOpposite()));
        return blockState.isOf(this) && blockState.get(HANGING) == (direction == Direction.DOWN)
                ? this.getDefaultState().with(HANGING, direction == Direction.DOWN)
                : this.getDefaultState().with(HANGING, direction == Direction.DOWN);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, net.minecraft.block.ShapeContext context) {
        return state.get(HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
    }

    @Override
    protected int getDefaultChannel() {
        return LightChannels.LANTERN; // 異なるデフォルトチャンネルを設定
    }
}

