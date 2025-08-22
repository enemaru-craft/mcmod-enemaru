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

            // クライアント側のUIに反映させるためにヘッダーの変数を変更
            screenHandler.setStreetLightOn(true);
        }).position(centerX + 38, centerY + 30).size(100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("街灯をオフ"), button -> {
            StateUpdateRequestC2SPayload payload = new StateUpdateRequestC2SPayload(false, true, true);
            ClientPlayNetworking.send(payload);

            // クライアント側のUIに反映させるためにヘッダーの変数を変更
            screenHandler.setStreetLightOn(false);
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
        // 背景描画
        this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        // =========================
        // 同期済みデータ（サーバーとの同期済み変数）
        // =========================
        int energy = screenHandler.getGeneratedEnergy();       // 発電量
        boolean streetLightOn = screenHandler.isStreetLightOn(); // 街灯の状態
        int powerLevel = screenHandler.getPowerLevel();         // 任意のパワー値

        // =========================
        // テキスト描画
        // =========================
        int textX = this.x + 8;
        int textY = this.y + 100;

        context.drawText(this.textRenderer,
                "Generated Energy: " + energy,
                textX, textY, 0xFFFFFF, false);

        context.drawText(this.textRenderer,
                "Street Light: " + (streetLightOn ? "ON" : "OFF"),
                textX, textY + 12, 0xFFFF00, false); // 黄色で表示

        context.drawText(this.textRenderer,
                "Power Level: " + powerLevel,
                textX, textY + 24, 0x00FFFF, false); // 水色で表示

        // =========================
        // 視覚的バーでパワーを表示（例）
        // =========================
        int barX = this.x + 8;
        int barY = textY + 40;
        int barWidth = 100;
        int barHeight = 10;

        // 背景バー（グレー）
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);
        // パワーバー（緑、同期済み powerLevel に比例）
        int filledWidth = Math.min(barWidth, powerLevel);
        context.fill(barX, barY, barX + filledWidth, barY + barHeight, 0xFF00FF00);

        // =========================
        // マウスオーバーツールチップ
        // =========================
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

}