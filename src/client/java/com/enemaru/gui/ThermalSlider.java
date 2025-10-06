package com.enemaru.gui;

import com.enemaru.networking.payload.ThermalUpdateC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ThermalSlider extends SliderWidget {
    private int thermalPower = 0;
    public ThermalSlider(int x, int y, int width, int height, double initial) {
        super(x, y, width, height, Text.empty(), MathHelper.clamp(initial, 0.0, 1.0));
        this.thermalPower = (int)Math.round(this.value * 1000);
        updateMessage();
    }

    @Override
    protected void updateMessage() {
    }

    @Override
    protected void applyValue() {
        // ドラッグ中に常に現在値を反映 (送信はしない)
        this.thermalPower = MathHelper.clamp((int)Math.round(this.value * 1000), 0, 1000);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        this.thermalPower = (int) MathHelper.clamp(Math.round(this.value * 1000), 0, 1000);
        ThermalUpdateC2SPayload payload = new ThermalUpdateC2SPayload(this.thermalPower);
        ClientPlayNetworking.send(payload);
    }

    public int getThermalPower() {
        return this.thermalPower;
    }

    public void setValue(int power) {
        this.value = MathHelper.clamp(power / 1000.0, 0.0, 1.0);
        this.thermalPower = power;
    }
}
