package com.enemaru.block;

import com.enemaru.blockentity.CounterBlockEntity;
import com.enemaru.blockentity.ModBlockEntities;
import com.enemaru.power.PowerNetwork;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CounterBlock extends BlockWithEntity {
    public CounterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(CounterBlock::new);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CounterBlockEntity(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
//        if (!world.isClient) {
//            if (!(world.getBlockEntity(pos) instanceof CounterBlockEntity counterBlockEntity)) {
//                return super.onUse(state, world, pos, player, hit);
//            }
//
//            counterBlockEntity.incrementClicks();
//            world.updateListeners(pos, state, state, 0);
//            player.sendMessage(Text.literal("You've clicked the block for the " + counterBlockEntity.getClicks() + "th time."), true);
//        }
//        return ActionResult.SUCCESS;
        // クライアント側では SUCCESS を返しておくだけ
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }
        if (!player.getAbilities().allowModifyWorld) {
            // Skip if the player isn't allowed to modify the world.
            return ActionResult.PASS;
        } else {
            // サーバー側で PowerNetwork のフラグをトグル
            ServerWorld sw = (ServerWorld) world;
            PowerNetwork net = PowerNetwork.get(sw);
            boolean newState = !net.isStreetlightsEnabled();

            // テスト用なので世界とフラグを渡して一斉更新
            net.setStreetlightsEnabled(newState);

            return ActionResult.SUCCESS;
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.COUNTER_BLOCK_ENTITY, CounterBlockEntity::tick);
    }
}