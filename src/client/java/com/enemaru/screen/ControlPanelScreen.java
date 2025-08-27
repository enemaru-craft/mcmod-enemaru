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
        this.backgroundHeight = 186;
        screenHandler = (ControlPanelScreenHandler) handler;
    }

    @Override
    protected void init() {
        super.init();

        // 背景の中心座標
        int centerX = this.x + (this.backgroundWidth / 2);
        int centerY = this.y + (this.backgroundHeight / 2);

        // 現在のライト状態を取得
        // boolean lightOn = screenHandler.isLightEnabled();

        int buttonWidth = 45;
        int buttonHeight = 20;

        // ==========================
        // 街灯 (Streetlights)
        // ==========================
        lightOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(
                    true,   // light
                    screenHandler.isTrainEnabled(),   // trainの現在値を維持
                    screenHandler.isFactoryEnabled()  // factoryの現在値を維持
            );
            ClientPlayNetworking.send(payload);
            updateLightButtons(true);
        }).position(centerX - buttonWidth - 2, centerY - 60).size(buttonWidth, buttonHeight).build();

        lightOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(
                    false,   // light
                    screenHandler.isTrainEnabled(),   // trainの現在値を維持
                    screenHandler.isFactoryEnabled()  // factoryの現在値を維持
            );
            ClientPlayNetworking.send(payload);
            updateLightButtons(false);
        }).position(centerX + 2, centerY - 60).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(lightOnButton);
        this.addDrawableChild(lightOffButton);

        lightLabelX = centerX;
        lightLabelY = centerY - 70;

        // ==========================
        // 電車 (Train)
        // ==========================
        trainOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(
                    screenHandler.isLightEnabled(),
                    true,
                    screenHandler.isFactoryEnabled()
            );
            ClientPlayNetworking.send(payload);
            updateTrainButtons(true);
        }).position(centerX - buttonWidth - 2, centerY - 20).size(buttonWidth, buttonHeight).build();

        trainOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(
                    screenHandler.isLightEnabled(),
                    false,
                    screenHandler.isFactoryEnabled()
            );
            ClientPlayNetworking.send(payload);
            updateTrainButtons(false);
        }).position(centerX + 2, centerY - 20).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(trainOnButton);
        this.addDrawableChild(trainOffButton);

        trainLabelX = centerX;
        trainLabelY = centerY - 30;

        // ==========================
        // 工場 (Factory)
        // ==========================
        factoryOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(
                    screenHandler.isLightEnabled(),
                    screenHandler.isTrainEnabled(),
                    true
            );
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(true);
        }).position(centerX - buttonWidth - 2, centerY + 20).size(buttonWidth, buttonHeight).build();

        factoryOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(
                    screenHandler.isLightEnabled(),
                    screenHandler.isTrainEnabled(),
                    false
            );
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(false);
        }).position(centerX + 2, centerY + 20).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(factoryOnButton);
        this.addDrawableChild(factoryOffButton);

        factoryLabelX = centerX;
        factoryLabelY = centerY + 10;

        // 初期状態を反映
        updateLightButtons(screenHandler.isLightEnabled());
        updateTrainButtons(screenHandler.isTrainEnabled());
        updateFactoryButtons(screenHandler.isFactoryEnabled());
    }

    // =============================
    // ON/OFFボタン制御
    // =============================
    private void updateLightButtons(boolean lightOn) {
        lightOnButton.active = !lightOn;
        lightOffButton.active = lightOn;
    }

    private void updateTrainButtons(boolean trainOn) {
        trainOnButton.active = !trainOn;
        trainOffButton.active = trainOn;
    }

    private void updateFactoryButtons(boolean factoryOn) {
        factoryOnButton.active = !factoryOn;
        factoryOffButton.active = factoryOn;
    }

    // フィールド
    private ButtonWidget lightOnButton, lightOffButton;
    private ButtonWidget trainOnButton, trainOffButton;
    private ButtonWidget factoryOnButton, factoryOffButton;

    private int lightLabelX, lightLabelY;
    private int trainLabelX, trainLabelY;
    private int factoryLabelX, factoryLabelY;


    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        // シンプルな背景色で塗りつぶし
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xFF202020);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // ラベルを描画
        drawCenteredLabel(context, "街灯", lightLabelX, lightLabelY);
        drawCenteredLabel(context, "電車", trainLabelX, trainLabelY);
        drawCenteredLabel(context, "工場", factoryLabelX, factoryLabelY);

        // サーバーの状態を毎フレーム反映
        updateLightButtons(screenHandler.isLightEnabled());
        updateTrainButtons(screenHandler.isTrainEnabled());
        updateFactoryButtons(screenHandler.isFactoryEnabled());

        // ScreenHandlerに用意したゲッター経由で取得
        int energy = screenHandler.getGeneratedEnergy();
        int surplus = screenHandler.getSurplusEnergy();
        boolean light = screenHandler.isLightEnabled();
        boolean train = screenHandler.isTrainEnabled();
        boolean factory = screenHandler.isFactoryEnabled();
        boolean blackout = screenHandler.isBlackout();

        // 画面左上からの相対座標（背景の左上は this.x / this.y）
         int textX = this.x + 8;
         int textY = this.y + 140;

        // ======================
        // Energy Bar
        // ======================
        int maxEnergy = 4000;
        int barWidth = 100; // バーの最大幅
        int barHeight = 10;
        int filled = (int) ((double) energy / maxEnergy * barWidth);

        int barX = textX;
        int barY = textY;

        // バー背景（灰色）
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);
        // バー中身（緑）
        context.fill(barX, barY, barX + filled, barY + barHeight, 0xFF00FF00);

        // 数字表示（バーの右側に表示）
        context.drawText(this.textRenderer, energy + " / " + maxEnergy, barX + barWidth + 5, barY, 0xFFFFFF, false);


        // context.drawText(this.textRenderer, "Generated Energy: " + energy, textX, textY, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Surplus Energy: " + surplus, textX, textY+15, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Streetlights: " + (light ? "On" : "Off"), textX + 200, textY-50, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Train: " + (train ? "On" : "Off"), textX + 200, textY-40, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Factory: " + (factory ? "On" : "Off"), textX + 200, textY-30, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Blackout: " + (blackout ? "On" : "Off"), textX, textY+25, 0xFFFFFF, false);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    private void drawCenteredLabel(DrawContext context, String text, int cx, int cy) {
        int w = this.textRenderer.getWidth(text);
        context.drawText(this.textRenderer, text, cx - w / 2, cy, 0xFFFFFF, false);
    }
}