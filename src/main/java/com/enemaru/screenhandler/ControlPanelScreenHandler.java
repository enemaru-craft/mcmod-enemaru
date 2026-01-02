package com.enemaru.screenhandler;

import com.enemaru.Enemaru;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import java.util.ArrayList;
import java.util.List;

public class ControlPanelScreenHandler extends ScreenHandler {
    public static final int PROP_ENERGY = 0;
    public static final int PROP_SURPLUS = 1;
    public static final int PROP_STREETLIGHT = 2;
    public static final int PROP_TRAIN = 3;
    public static final int PROP_FACTORY = 4;
    public static final int PROP_BLACKOUT = 5;
    public static final int PROP_HOUSE = 6;
    public static final int PROP_FACILITY = 7;
    public static final int PROP_THERMAL = 8;
    public static final int NUM_PROPS = 9;

    // グラフ用の静的リスト
    public static final List<Integer> energyHistory = new ArrayList<>();
    public static final int MAX_HISTORY = 2400;

    private final PropertyDelegate propertyDelegate;

    /**
     * クライアント側
     */
    public ControlPanelScreenHandler(int syncId, PlayerInventory inv) {
        this(syncId, inv, new ArrayPropertyDelegate(NUM_PROPS));
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

    public int getSurplusEnergy() {
        return propertyDelegate.get(PROP_SURPLUS);
    }

    public int getThermalEnergy() {
        return propertyDelegate.get(PROP_THERMAL);
    }

    public boolean isLightEnabled() {
        return propertyDelegate.get(PROP_STREETLIGHT) != 0;
    }

    public boolean isTrainEnabled() {
        return propertyDelegate.get(PROP_TRAIN) != 0;
    }

    public boolean isFactoryEnabled() {
        return propertyDelegate.get(PROP_FACTORY) != 0;
    }

    public boolean isBlackout() {
        return propertyDelegate.get(PROP_BLACKOUT) != 0;
    }

    public boolean isHouseEnabled() {
        return propertyDelegate.get(PROP_HOUSE) != 0;
    }

    public boolean isFacilityEnabled() {
        return propertyDelegate.get(PROP_FACILITY) != 0;
    }

    public int getLightPercent() {
        return propertyDelegate.get(PROP_STREETLIGHT);
    }
    public int getFactoryPercent() {
        return propertyDelegate.get(PROP_FACTORY);
    }
    public int getHousePercent() {
        return propertyDelegate.get(PROP_HOUSE);
    }
    public int getFacilityPercent() {
        return propertyDelegate.get(PROP_FACILITY);
    }
    
    /**
     * エネルギー履歴を追加
     */
    public static void addEnergyHistory(int energy) {
        if (energyHistory.size() >= MAX_HISTORY) {
            energyHistory.remove(0);
        }
        energyHistory.add(energy);
    }
    
    /**
     * エネルギー履歴を取得
     */
    public static List<Integer> getEnergyHistory() {
        return energyHistory;
    }

    /**
     * エネルギー履歴をクリア
     */
    public static void clearEnergyHistory() {
        energyHistory.clear();
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

