package com.enemaru.lighting;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 管理式ライトのチャンネルを設定できるツール。
 * - Shift+右クリック（空中）: チャンネルを循環（0-15）
 * - 右クリック（ブロック）: 対象ブロックにチャンネルを適用
 */
public class ChannelConfiguratorItem extends Item {

    public ChannelConfiguratorItem(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        if (player.isSneaking()) {
            // Shift+右クリック: チャンネルを循環
            int currentChannel = getChannel(stack);
            int nextChannel = (currentChannel + 1) % 16;
            setChannel(stack, nextChannel);

            if (!world.isClient) {
                player.sendMessage(
                        Text.literal(String.format("チャンネル: %d", nextChannel))
                                .formatted(Formatting.AQUA),
                        true
                );
            }

            return TypedActionResult.success(stack, world.isClient);
        }

        return TypedActionResult.pass(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        // 管理式ライトかチェック
        if (!(state.getBlock() instanceof AbstractManagedLightBlock)) {
            if (player != null && !world.isClient) {
                player.sendMessage(
                        Text.literal("このブロックは管理式ライトではありません")
                                .formatted(Formatting.RED),
                        true
                );
            }
            return ActionResult.FAIL;
        }

        int targetChannel = getChannel(stack);

        if (!world.isClient) {
            // サーバー側: チャンネルを直接変更
            AbstractManagedLightBlock.changeChannel(world, pos, state, targetChannel);

            if (player != null) {
                player.sendMessage(
                        Text.literal(String.format("チャンネル %d を適用しました", targetChannel))
                                .formatted(Formatting.GREEN),
                        true
                );
            }
        }

        return ActionResult.SUCCESS;
    }

    /**
     * アイテムに保存されているチャンネルを取得。
     */
    public static int getChannel(ItemStack stack) {
        var nbt = stack.getOrDefault(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT).copyNbt();
        return nbt.getInt("Channel");
    }

    /**
     * アイテムにチャンネルを保存。
     */
    public static void setChannel(ItemStack stack, int channel) {
        stack.apply(net.minecraft.component.DataComponentTypes.CUSTOM_DATA,
                net.minecraft.component.type.NbtComponent.DEFAULT,
                comp -> comp.apply(nbt -> nbt.putInt("Channel", channel)));
    }
}

