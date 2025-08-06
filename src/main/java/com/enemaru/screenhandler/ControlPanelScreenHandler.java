package com.enemaru.screenhandler;

import com.enemaru.Enemaru;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;

public class ControlPanelScreenHandler extends ScreenHandler {
    public ControlPanelScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(Enemaru.PANEL_SCREEN_HANDLER, syncId);
        // インベントリスロットを追加しないなら空でOK
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        // この ScreenHandler にはスロットがないので、
        // Shift＋クリックしても何も起こさない
        return ItemStack.EMPTY;
    }
}

