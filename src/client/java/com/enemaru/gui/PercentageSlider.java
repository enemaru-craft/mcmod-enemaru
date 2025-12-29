package com.enemaru.gui;

import com.enemaru.networking.payload.EquipmentPercentC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

/**
 * 汎用パーセンテージスライダー
 * 0～100%の値を管理し、機器制御のためのPayloadを送信
 */
public class PercentageSlider extends SliderWidget {
    private final String equipment;
    private int percent = 0;
    private boolean blockServerSync = false;

    /**
     * @param x x座標
     * @param y y座標
     * @param width スライダーの幅
     * @param height スライダーの高さ
     * @param equipment 機器名 ("light", "factory", "house", "facility")
     * @param initialPercent 初期パーセント値 (0～100)
     */
    public PercentageSlider(int x, int y, int width, int height, String equipment, int initialPercent) {
        super(x, y, width, height, Text.empty(), MathHelper.clamp(initialPercent / 100.0, 0.0, 1.0));
        this.equipment = equipment;
        this.percent = MathHelper.clamp(initialPercent, 0, 100);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        // メッセージ更新時に何もしない（renderで直接描画する）
    }

    @Override
    protected void applyValue() {
        // ドラッグ中に常に現在値を反映
        this.percent = MathHelper.clamp((int) Math.round(this.value * 100), 0, 100);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        this.blockServerSync = true;
        super.onClick(mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        this.blockServerSync = true;
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        this.percent = (int) MathHelper.clamp(Math.round(this.value * 100), 0, 100);

        // サーバーにPayloadを送信
        EquipmentPercentC2SPayload payload = new EquipmentPercentC2SPayload(this.equipment, this.percent);
        ClientPlayNetworking.send(payload);
        this.blockServerSync = false;
    }

    /**
     * 現在のパーセント値を取得
     */
    public int getPercent() {
        return this.percent;
    }

    /**
     * パーセント値を設定
     */
    public void setPercent(int percent) {
        this.percent = MathHelper.clamp(percent, 0, 100);
        this.value = this.percent / 100.0;
    }

    public boolean isBlockingServerSync() {
        return this.blockServerSync;
    }
}
