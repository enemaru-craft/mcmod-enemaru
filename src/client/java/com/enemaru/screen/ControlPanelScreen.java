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
        int centerX = (width - backgroundWidth) / 2;
        int centerY = (height - backgroundHeight) / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("街灯をオン"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(true, true, true);
            ClientPlayNetworking.send(payload);
        }).position(centerX + 38, centerY + 30).size(100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("街灯をオフ"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(false, true, true);
            ClientPlayNetworking.send(payload);
        }).position(centerX + 38, centerY + 55).size(100, 20).build());
    }


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