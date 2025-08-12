package com.enemaru.screenhandler;

import com.enemaru.Enemaru;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;

public class ControlPanelScreenHandler extends ScreenHandler {
    public static final int PROP_ENERGY = 0;
    private final PropertyDelegate propertyDelegate;

    /**
     * クライアント側
     */
    public ControlPanelScreenHandler(int syncId, PlayerInventory inv) {
        this(syncId, inv, new ArrayPropertyDelegate(1));
    }

    /**
     * サーバ側
     */
    public ControlPanelScreenHandler(int syncId, PlayerInventory inv, PropertyDelegate delegate) {
        super(Enemaru.PANEL_SCREEN_HANDLER, syncId);
        this.propertyDelegate = delegate;
        this.addProperties(delegate); // ★ これでintが自動同期対象になる
    }

    public int getGeneratedEnergy() {
        return propertyDelegate.get(PROP_ENERGY);
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

