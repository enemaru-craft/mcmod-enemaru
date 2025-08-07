// ControlPanelItem.java
package com.enemaru.item;

import com.enemaru.screenhandler.ControlPanelScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ControlPanelItem extends Item implements NamedScreenHandlerFactory {
    public ControlPanelItem(Settings settings) {
        super(settings);
    }

    // GUI のタイトル
    @Override
    public Text getDisplayName() {
        return Text.literal("Control Panel");
    }

    // ScreenHandler（サーバー／共通側）のインスタンスを生成
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ControlPanelScreenHandler(syncId, inv);
    }

    // 右クリック時に GUI を開く
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openHandledScreen(this);
        }
        return TypedActionResult.success(stack, world.isClient());
    }
}
