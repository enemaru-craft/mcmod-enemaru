package com.enemaru.screen;

import com.enemaru.networking.payload.StateUpdateRequestC2SPayload;
import com.enemaru.screenhandler.ControlPanelScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;

public class ControlPanelScreen extends HandledScreen<ScreenHandler> {

    ControlPanelScreenHandler screenHandler;

    public ControlPanelScreen(ScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
        screenHandler = (ControlPanelScreenHandler) handler;
    }

    @Override
    protected void init() {
        super.init();

        // 背景の中心座標
        int centerX = this.x + (this.backgroundWidth / 2);
        int centerY = this.y + (this.backgroundHeight / 2);

        // 現在のライト状態を取得
        boolean lightOn = screenHandler.isLightEnabled();

        int buttonWidth = 45;
        int buttonHeight = 20;

        // ONボタン
        ButtonWidget onButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(true, true, true);
            ClientPlayNetworking.send(payload);
            updateLightButtons(true);
        }).position(centerX - buttonWidth - 2, centerY - 50).size(buttonWidth, buttonHeight).build();

        // OFFボタン
        ButtonWidget offButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(false, true, true);
            ClientPlayNetworking.send(payload);
            updateLightButtons(false);
        }).position(centerX + 2, centerY - 50).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(onButton);
        this.addDrawableChild(offButton);

        // ボタンを保持しておいて状態反映用に使う
        this.lightOnButton = onButton;
        this.lightOffButton = offButton;

        // 初期状態を反映
        updateLightButtons(lightOn);

        // ラベル用の位置を保持
        this.labelX = centerX;
        this.labelY = centerY - 60; // ボタンの少し上に表示
    }

    // ON/OFFの状態でボタンの色や有効状態を変える
    private void updateLightButtons(boolean lightOn) {
        if (lightOn) {
            lightOnButton.active = false;   // ONのときはONボタンを暗く
            lightOffButton.active = true;
        } else {
            lightOnButton.active = true;
            lightOffButton.active = false;  // OFFのときはOFFボタンを暗く
        }
    }

    // フィールド追加
    private ButtonWidget lightOnButton;
    private ButtonWidget lightOffButton;


    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        // シンプルな背景色で塗りつぶし
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFF202020);
    }

    private int labelX;
    private int labelY;

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // ラベルを中央寄せで描画
        String label = "街灯";
        int textWidth = this.textRenderer.getWidth(label);
        context.drawText(this.textRenderer, label, labelX - textWidth / 2, labelY, 0xFFFFFF, false);

        // サーバー側の状態を毎回チェックして反映
        updateLightButtons(screenHandler.isLightEnabled());

        // ScreenHandlerに用意したゲッター経由で取得
        int energy = screenHandler.getGeneratedEnergy();
        int surplus = screenHandler.getSurplusEnergy();
        boolean light = screenHandler.isLightEnabled();
        boolean train = screenHandler.isTrainEnabled();
        boolean factory = screenHandler.isFactoryEnabled();
        boolean blackout = screenHandler.isBlackout();

        // 画面左上からの相対座標（背景の左上は this.x / this.y）
        int textX = this.x + 8;
        int textY = this.y + 90;

        context.drawText(this.textRenderer, "Generated Energy: " + energy, textX, textY, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Surplus Energy: " + surplus, textX, textY+10, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Streetlights: " + (light ? "On" : "Off"), textX, textY+20, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Train: " + (train ? "On" : "Off"), textX, textY+30, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Factory: " + (factory ? "On" : "Off"), textX, textY+40, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Blackout: " + (blackout ? "On" : "Off"), textX, textY+50, 0xFFFFFF, false);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}