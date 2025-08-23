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
    public static final int PROP_POWER = 1;
    public static final int PROP_STREETLIGHT = 2;
    private final PropertyDelegate propertyDelegate;

    /**
     * クライアント側
     */
    public ControlPanelScreenHandler(int syncId, PlayerInventory inv) {
        this(syncId, inv, new ArrayPropertyDelegate(3));
    }

    /**
     * サーバ側
     */
    public ControlPanelScreenHandler(int syncId, PlayerInventory inv, PropertyDelegate delegate) {
        super(Enemaru.PANEL_SCREEN_HANDLER, syncId);
        this.propertyDelegate = delegate;
        this.addProperties(delegate); // ★ PropertyDelegate が自動同期対象になる
    }

    // 発電量
    public int getGeneratedEnergy() { return propertyDelegate.get(PROP_ENERGY); }
    public void setGeneratedEnergy(int energy) { propertyDelegate.set(PROP_ENERGY, energy); }

    // 街灯
    public boolean isStreetLightOn() { return propertyDelegate.get(PROP_STREETLIGHT) != 0; }
    public void setStreetLightOn(boolean state) { propertyDelegate.set(PROP_STREETLIGHT, state ? 1 : 0); }

    // パワーレベル
    public int getPowerLevel() { return propertyDelegate.get(PROP_POWER); }
    public void setPowerLevel(int level) { propertyDelegate.set(PROP_POWER, level); }

    @Override
    public boolean canUse(PlayerEntity player) { return true; }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) { return ItemStack.EMPTY; }
}
