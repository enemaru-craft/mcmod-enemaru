package com.enemaru.block;

import com.enemaru.blockentity.EndRodLampBlockEntity;
import com.enemaru.blockentity.ModBlockEntities;
import com.enemaru.power.PowerNetwork;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.server.world.ServerWorld;

public class EndRodLampBlock extends BlockWithEntity {
    public static final BooleanProperty LIT = Properties.LIT;
    public static final DirectionProperty FACING = Properties.FACING;

    // 向きごとの当たり判定
    private static final VoxelShape SHAPE_UP = Block.createCuboidShape(6, 0, 6, 10, 16, 10);
    private static final VoxelShape SHAPE_DOWN = SHAPE_UP;
    private static final VoxelShape SHAPE_NORTH = Block.createCuboidShape(6, 6, 0, 10, 10, 16);
    private static final VoxelShape SHAPE_SOUTH = SHAPE_NORTH;
    private static final VoxelShape SHAPE_WEST = Block.createCuboidShape(0, 6, 6, 16, 10, 10);
    private static final VoxelShape SHAPE_EAST = SHAPE_WEST;

    public EndRodLampBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState()
                .with(LIT, false)
                .with(FACING, Direction.UP)
        );
    }

    public static int getLuminance(BlockState state) {
        return state.get(LIT) ? 15 : 0;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(EndRodLampBlock::new);
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(LIT, FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(LIT, false)
                .with(FACING, ctx.getPlayerLookDirection().getOpposite());
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new EndRodLampBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) return null;
        return validateTicker(type, ModBlockEntities.END_ROD_LAMP_ENTITY, EndRodLampBlockEntity::tick);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
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
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return getOutlineShape(state, world, pos, ctx);
    }

//    @Override
//    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
//        if (world.isClient) return ActionResult.SUCCESS;
//        if (!player.getAbilities().allowModifyWorld) return ActionResult.PASS;
//
//        ServerWorld sw = (ServerWorld) world;
//        PowerNetwork net = PowerNetwork.get(sw);
//        boolean newState = !net.getStreetlightsEnabled();
//        net.setStreetlightsEnabled(newState);
//
//        return ActionResult.CONSUME;
//    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
