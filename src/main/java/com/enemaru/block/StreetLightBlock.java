package com.enemaru.block;

import com.enemaru.blockentity.ModBlockEntities;
import com.enemaru.blockentity.StreetLightBlockEntity;
import com.enemaru.power.PowerNetwork;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * 街灯ブロック本体。
 * LIT プロパティで点灯／消灯を切り替え、
 * luminance() で光度を自動制御します。
 */
public class StreetLightBlock extends BlockWithEntity {
    public static final BooleanProperty LIT = Properties.LIT;
    public static final BooleanProperty HANGING = Properties.HANGING;

    public StreetLightBlock(Settings settings) {
        super(settings);
        // デフォルトは消灯
        this.setDefaultState(getDefaultState().with(LIT, false));
    }

    public static int getLuminance(BlockState currentBlockState) {
        // LIT プロパティの値を取得し、光度を返す
        return currentBlockState.get(LIT) ? 15 : 0;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(StreetLightBlock::new);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HANGING, LIT);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new StreetLightBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient) return null;
        return validateTicker(type, ModBlockEntities.STREET_LIGHT_ENTITY, StreetLightBlockEntity::tick);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        boolean hanging = ctx.getSide() == Direction.DOWN;
        return this.getDefaultState()
                .with(HANGING, hanging)
                .with(LIT, false);
    }

//    @Override
//    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
//        if (!player.getAbilities().allowModifyWorld) {
//            // Skip if the player isn't allowed to modify the world.
//            return ActionResult.PASS;
//        } else {
//            // Get the current value of the "activated" property
//            boolean activated = state.get(LIT);
//
//            // Flip the value of activated and save the new blockstate.
//            world.setBlockState(pos, state.with(LIT, !activated));
//
//            // Play a click sound to emphasise the interaction.
//            world.playSound(player, pos, SoundEvents.BLOCK_COMPARATOR_CLICK, SoundCategory.BLOCKS, 1.0F, 1.0F);
//
//            return ActionResult.SUCCESS;
//        }
//    }
//    @Override
    ////    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
    ////        // クライアント側では SUCCESS を返しておくだけ
    ////        if (world.isClient) {
    ////            return ActionResult.SUCCESS;
    ////        }
    ////        if (!player.getAbilities().allowModifyWorld) {
    ////            // Skip if the player isn't allowed to modify the world.
    ////            return ActionResult.PASS;
    ////        } else {
    ////            // サーバー側で PowerNetwork のフラグをトグル
    ////            ServerWorld sw = (ServerWorld) world;
    ////            PowerNetwork net = PowerNetwork.get(sw);
    ////            boolean newState = !net.getStreetlightsEnabled();
    ////
    ////            // テスト用なので世界とフラグを渡して一斉更新
    ////            net.setStreetlightsEnabled(newState);
    ////
    ////            return ActionResult.CONSUME;
    ////        }
    ////    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    private static final VoxelShape BODY_SHAPE =
            VoxelShapes.cuboid(5 / 16.0, 0.0, 5 / 16.0, 11 / 16.0, 7 / 16.0, 11 / 16.0);

    private static final VoxelShape HANDLE_SHAPE =
            VoxelShapes.cuboid(6 / 16.0, 7 / 16.0, 6 / 16.0, 10 / 16.0, 9 / 16.0, 10 / 16.0);

    private static final VoxelShape SHAPE =
            VoxelShapes.union(BODY_SHAPE, HANDLE_SHAPE);

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return SHAPE;
    }


}
