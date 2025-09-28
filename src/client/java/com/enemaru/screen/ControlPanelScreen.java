package com.enemaru.screen;

import com.enemaru.networking.payload.EquipmentRequestC2SPayload;
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

    private final int HOUSE_CONSUMPTION = 300;
    private final int FACILITY_CONSUMPTION = 1015;
    private final int LIGHT_CONSUMPTION = 5;
    private final int TRAIN_CONSUMPTION = 410;
    private final int FACTORY_CONSUMPTION = 300;

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
        int centerY = this.y + (this.backgroundHeight / 2) - 20; // 全体を上に20px移動

        int buttonWidth = 45;
        int buttonHeight = 20;

        // ==========================
        // 街灯
        // ==========================
        lightOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("light", true);
            ClientPlayNetworking.send(payload);
            updateLightButtons(true);
        }).position(centerX - buttonWidth - 2, centerY - 50).size(buttonWidth, buttonHeight).build();

        lightOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("light", false);
            ClientPlayNetworking.send(payload);
            updateLightButtons(false);
        }).position(centerX + 2, centerY - 50).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(lightOnButton);
        this.addDrawableChild(lightOffButton);

        lightLabelX = centerX;
        lightLabelY = centerY - 60;

        // ==========================
        // 電車
        // ==========================
        trainOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("train", true);
            ClientPlayNetworking.send(payload);
            updateTrainButtons(true);
        }).position(centerX - buttonWidth - 2, centerY - 10).size(buttonWidth, buttonHeight).build();

        trainOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("train", false);
            ClientPlayNetworking.send(payload);
            updateTrainButtons(false);
        }).position(centerX + 2, centerY - 10).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(trainOnButton);
        this.addDrawableChild(trainOffButton);

        trainLabelX = centerX;
        trainLabelY = centerY - 20;

        // ==========================
        // 工場
        // ==========================
        factoryOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("factory", true);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(true);
        }).position(centerX - buttonWidth - 2, centerY + 30).size(buttonWidth, buttonHeight).build();

        factoryOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("factory", false);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(false);
        }).position(centerX + 2, centerY + 30).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(factoryOnButton);
        this.addDrawableChild(factoryOffButton);

        factoryLabelX = centerX;
        factoryLabelY = centerY + 20;

        // ==========================
        // 家
        // ==========================
        houseOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("house", true);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(true);
        }).position(centerX - buttonWidth - 2, centerY + 70).size(buttonWidth, buttonHeight).build();

        houseOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("house", false);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(false);
        }).position(centerX + 2, centerY + 70).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(houseOnButton);
        this.addDrawableChild(houseOffButton);

        houseLabelX = centerX;
        houseLabelY = centerY + 60;

        // ==========================
        // 公共施設
        // ==========================
        facilityOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("facility", true);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(true);
        }).position(centerX - buttonWidth - 2, centerY - 90).size(buttonWidth, buttonHeight).build();

        facilityOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("facility", false);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(false);
        }).position(centerX + 2, centerY - 90).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(facilityOnButton);
        this.addDrawableChild(facilityOffButton);

        facilityLabelX = centerX;
        facilityLabelY = centerY - 100;

        // 初期状態を反映
        updateLightButtons(screenHandler.isLightEnabled());
        updateTrainButtons(screenHandler.isTrainEnabled());
        updateFactoryButtons(screenHandler.isFactoryEnabled());
        updateHouseButtons(screenHandler.isHouseEnabled());
        updateFacilityButtons(screenHandler.isFacilityEnabled());
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

    private void updateHouseButtons(boolean houseOn) {
        houseOnButton.active = !houseOn;
        houseOffButton.active = houseOn;
    }

    private void updateFacilityButtons(boolean facilityOn) {
        facilityOnButton.active = !facilityOn;
        facilityOffButton.active = facilityOn;
    }

    // フィールド
    private ButtonWidget lightOnButton, lightOffButton;
    private ButtonWidget trainOnButton, trainOffButton;
    private ButtonWidget factoryOnButton, factoryOffButton;
    private ButtonWidget houseOnButton, houseOffButton;
    private ButtonWidget facilityOnButton, facilityOffButton;

    private int lightLabelX, lightLabelY;
    private int trainLabelX, trainLabelY;
    private int factoryLabelX, factoryLabelY;
    private int houseLabelX, houseLabelY;
    private int facilityLabelX, facilityLabelY;


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

        // ラベル描画
        drawCenteredLabel(context, "街灯", lightLabelX, lightLabelY);
        drawCenteredLabel(context, "電車", trainLabelX, trainLabelY);
        drawCenteredLabel(context, "工場", factoryLabelX, factoryLabelY);
        drawCenteredLabel(context, "家", houseLabelX, houseLabelY);
        drawCenteredLabel(context, "公共施設", facilityLabelX, facilityLabelY);

        updateLightButtons(screenHandler.isLightEnabled());
        updateTrainButtons(screenHandler.isTrainEnabled());
        updateFactoryButtons(screenHandler.isFactoryEnabled());
        updateHouseButtons(screenHandler.isHouseEnabled());
        updateFacilityButtons(screenHandler.isFacilityEnabled());

        int energy = screenHandler.getGeneratedEnergy();
        int surplus = screenHandler.getSurplusEnergy();
        boolean light = screenHandler.isLightEnabled();
        boolean train = screenHandler.isTrainEnabled();
        boolean factory = screenHandler.isFactoryEnabled();
        boolean house = screenHandler.isHouseEnabled();
        boolean facility = screenHandler.isFacilityEnabled();
        boolean blackout = screenHandler.isBlackout();

        int textX = this.x + 8;
        int textY = this.y + 140;

        // ======================
        // Energy Bar
        // ======================
        int maxEnergy = 4200; //バーの最大値
        int barWidth = 150; //バーの幅
        int barHeight = 10;
        int filled = (int) ((double) energy / maxEnergy * barWidth);

        int barX = textX;
        int barY = textY;

        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555); // 背景
        context.fill(barX, barY, barX + filled, barY + barHeight, 0xFF00FF00);   // 中身

        // 現在のエネルギーをバー上に中央表示
        String energyText = String.format("%03d / %d", energy, maxEnergy);
        int textWidth = this.textRenderer.getWidth(energyText);
        context.drawText(this.textRenderer, energyText, barX + (barWidth - textWidth) / 2, barY - 10, 0xFFFFFF, false);

        // ======================
        // 予測表示（マウスオーバー）
        // ======================
        if (!light && lightOnButton.isMouseOver(mouseX, mouseY)) {
            int predictedIncrease = LIGHT_CONSUMPTION;
            int predictedFilled = (int) ((double)(energy + predictedIncrease) / maxEnergy * barWidth);
            context.fill(barX + filled, barY, barX + predictedFilled, barY + barHeight, 0xFFFF8888); // 薄い赤
            context.drawText(this.textRenderer, "+" + predictedIncrease, barX + predictedFilled + 5, barY, 0xFFFF8888, false);
        }
        if (light && lightOffButton.isMouseOver(mouseX, mouseY)) {
            int predictedDecrease = LIGHT_CONSUMPTION;
            int predictedFilled = (int) ((double)(energy - predictedDecrease) / maxEnergy * barWidth);
            context.fill(barX + predictedFilled, barY, barX + filled, barY + barHeight, 0xFF008800); // 濃い緑
            context.drawText(this.textRenderer, "-" + predictedDecrease, barX + predictedFilled + 15, barY, 0x8844FF44, false);
        }

        if (!train && trainOnButton.isMouseOver(mouseX, mouseY)) {
            int predictedIncrease = TRAIN_CONSUMPTION;
            int predictedFilled = (int) ((double)(energy + predictedIncrease) / maxEnergy * barWidth);
            context.fill(barX + filled, barY, barX + predictedFilled, barY + barHeight, 0xFFFF8888);
            context.drawText(this.textRenderer, "+" + predictedIncrease, barX + predictedFilled + 5, barY, 0xFFFF8888, false);
        }
        if (train && trainOffButton.isMouseOver(mouseX, mouseY)) {
            int predictedDecrease = TRAIN_CONSUMPTION;
            int predictedFilled = (int) ((double)(energy - predictedDecrease) / maxEnergy * barWidth);
            context.fill(barX + predictedFilled, barY, barX + filled, barY + barHeight, 0xFF008800);
            context.drawText(this.textRenderer, "-" + predictedDecrease, barX + predictedFilled + 25, barY, 0x8844FF44, false);
        }

        if (!factory && factoryOnButton.isMouseOver(mouseX, mouseY)) {
            int predictedIncrease = FACTORY_CONSUMPTION;
            int predictedFilled = (int) ((double)(energy + predictedIncrease) / maxEnergy * barWidth);
            context.fill(barX + filled, barY, barX + predictedFilled, barY + barHeight, 0xFFFF8888);
            context.drawText(this.textRenderer, "+" + predictedIncrease, barX + predictedFilled + 5, barY, 0xFFFF8888, false);
        }
        if (factory && factoryOffButton.isMouseOver(mouseX, mouseY)) {
            int predictedDecrease = FACTORY_CONSUMPTION;
            int predictedFilled = (int) ((double)(energy - predictedDecrease) / maxEnergy * barWidth);
            context.fill(barX + predictedFilled, barY, barX + filled, barY + barHeight, 0xFF008800);
            context.drawText(this.textRenderer, "-" + predictedDecrease, barX + predictedFilled + 35, barY, 0x8844FF44, false);
        }

        if (!house && houseOnButton.isMouseOver(mouseX, mouseY)) {
            int predictedIncrease = HOUSE_CONSUMPTION;
            int predictedFilled = (int) ((double)(energy + predictedIncrease) / maxEnergy * barWidth);
            context.fill(barX + filled, barY, barX + predictedFilled, barY + barHeight, 0xFFFF8888);
            context.drawText(this.textRenderer, "+" + predictedIncrease, barX + predictedFilled + 5, barY, 0xFFFF8888, false);
        }
        if (house && houseOffButton.isMouseOver(mouseX, mouseY)) {
            int predictedDecrease = HOUSE_CONSUMPTION;
            int predictedFilled = (int) ((double)(energy - predictedDecrease) / maxEnergy * barWidth);
            context.fill(barX + predictedFilled, barY, barX + filled, barY + barHeight, 0xFF008800);
            context.drawText(this.textRenderer, "-" + predictedDecrease, barX + predictedFilled + 15, barY, 0x8844FF44, false);
        }

        if (!facility && facilityOnButton.isMouseOver(mouseX, mouseY)) {
            int predictedIncrease = FACILITY_CONSUMPTION;
            int predictedFilled = (int) ((double)(energy + predictedIncrease) / maxEnergy * barWidth);
            context.fill(barX + filled, barY, barX + predictedFilled, barY + barHeight, 0xFFFF8888);
            context.drawText(this.textRenderer, "+" + predictedIncrease, barX + predictedFilled + 5, barY, 0xFFFF8888, false);
        }
        if (facility && facilityOffButton.isMouseOver(mouseX, mouseY)) {
            int predictedDecrease = FACILITY_CONSUMPTION;
            int predictedFilled = (int) ((double)(energy - predictedDecrease) / maxEnergy * barWidth);
            context.fill(barX + predictedFilled, barY, barX + filled, barY + barHeight, 0xFF008800);
            context.drawText(this.textRenderer, "-" + predictedDecrease, barX + predictedFilled + 20, barY, 0x8844FF44, false);
        }



        // 他情報表示
        context.drawText(this.textRenderer, "Surplus Energy: " + surplus, textX, textY + 15, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Streetlights: " + (light ? "On" : "Off"), textX + 200, textY - 50, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Train: " + (train ? "On" : "Off"), textX + 200, textY - 40, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Factory: " + (factory ? "On" : "Off"), textX + 200, textY - 30, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "House: " + (house ? "On" : "Off"), textX + 200, textY - 20, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Facility: " + (facility ? "On" : "Off"), textX + 200, textY - 10, 0xFFFFFF, false);
        context.drawText(this.textRenderer, "Blackout: " + (blackout ? "On" : "Off"), textX, textY + 25, 0xFFFFFF, false);

        drawMouseoverTooltip(context, mouseX, mouseY);
    }




    private void drawCenteredLabel(DrawContext context, String text, int cx, int cy) {
        int w = this.textRenderer.getWidth(text);
        context.drawText(this.textRenderer, text, cx - w / 2, cy, 0xFFFFFF, false);
    }
}