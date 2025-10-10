package com.enemaru.screen;

import com.enemaru.gui.ThermalSlider;
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
    private ThermalSlider slider;

    private final int HOUSE_CONSUMPTION = 300;
    private final int FACILITY_CONSUMPTION = 1015;
    private final int LIGHT_CONSUMPTION = 5;
    private final int TRAIN_CONSUMPTION = 410;
    private final int FACTORY_CONSUMPTION = 300;

    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        // 何も描かない → デフォルトのインベントリタイトルを消す
    }

    public ControlPanelScreen(ScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundWidth = 400;
        this.backgroundHeight = 210;
        screenHandler = (ControlPanelScreenHandler) handler;
    }

    @Override
    protected void init() {
        super.init();

        // 背景の中心座標
        int centerX = this.x + (this.backgroundWidth / 2);
        int centerY = this.y + (this.backgroundHeight / 2) - 20; // 全体を上に20px移動

        int buttonWidth = 40;
        int buttonHeight = 15;

        // ==========================
        // 街灯
        // ==========================
        lightOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("light", true);
            ClientPlayNetworking.send(payload);
            updateLightButtons(true);
        }).position(centerX - buttonWidth - 90, centerY - 60).size(buttonWidth, buttonHeight).build();

        lightOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("light", false);
            ClientPlayNetworking.send(payload);
            updateLightButtons(false);
        }).position(centerX - 86, centerY - 60).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(lightOnButton);
        this.addDrawableChild(lightOffButton);

        lightLabelX = centerX - 88;
        lightLabelY = centerY - 70;

        // ==========================
        // 電車
        // ==========================
        trainOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("train", true);
            ClientPlayNetworking.send(payload);
            updateTrainButtons(true);
        }).position(centerX - buttonWidth - 90, centerY - 30).size(buttonWidth, buttonHeight).build();

        trainOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("train", false);
            ClientPlayNetworking.send(payload);
            updateTrainButtons(false);
        }).position(centerX - 86, centerY - 30).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(trainOnButton);
        this.addDrawableChild(trainOffButton);

        trainLabelX = centerX - 88;
        trainLabelY = centerY - 40;

        // ==========================
        // 工場
        // ==========================
        factoryOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("factory", true);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(true);
        }).position(centerX - buttonWidth - 90, centerY).size(buttonWidth, buttonHeight).build();

        factoryOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("factory", false);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(false);
        }).position(centerX - 86, centerY).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(factoryOnButton);
        this.addDrawableChild(factoryOffButton);

        factoryLabelX = centerX - 88;
        factoryLabelY = centerY - 10;

        // ==========================
        // 家
        // ==========================
        houseOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("house", true);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(true);
        }).position(centerX - buttonWidth - 90, centerY + 30).size(buttonWidth, buttonHeight).build();

        houseOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("house", false);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(false);
        }).position(centerX - 86, centerY + 30).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(houseOnButton);
        this.addDrawableChild(houseOffButton);

        houseLabelX = centerX - 88;
        houseLabelY = centerY + 20;

        // ==========================
        // 公共施設
        // ==========================
        facilityOnButton = ButtonWidget.builder(Text.literal("ON"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("facility", true);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(true);
        }).position(centerX - buttonWidth - 90, centerY + 60).size(buttonWidth, buttonHeight).build();

        facilityOffButton = ButtonWidget.builder(Text.literal("OFF"), button -> {
            EquipmentRequestC2SPayload payload = new EquipmentRequestC2SPayload("facility", false);
            ClientPlayNetworking.send(payload);
            updateFactoryButtons(false);
        }).position(centerX - 86, centerY + 60).size(buttonWidth, buttonHeight).build();

        this.addDrawableChild(facilityOnButton);
        this.addDrawableChild(facilityOffButton);

        facilityLabelX = centerX - 88;
        facilityLabelY = centerY + 50;

        // 火力発電用スライダー
        int sliderX = facilityOnButton.getX() - 35;
        int sliderY = facilityOnButton.getY() + 40;

        slider = new ThermalSlider(sliderX, sliderY, 156, 20, 0);
        this.addDrawableChild(slider);

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

    private boolean isThermalSynced = false;

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

        int centerX = this.x + (this.backgroundWidth / 2);
        int centerY = this.y + (this.backgroundHeight / 2) - 20;

        int maxEnergy = 3500;

        // ラベル描画
        drawCenteredLabel(context, "街灯", lightLabelX, lightLabelY);
        drawCenteredLabel(context, "電車", trainLabelX, trainLabelY);
        drawCenteredLabel(context, "工場", factoryLabelX, factoryLabelY);
        drawCenteredLabel(context, "住宅", houseLabelX, houseLabelY);
        drawCenteredLabel(context, "公共施設", facilityLabelX, facilityLabelY);

        // ボタン状態更新
        updateLightButtons(screenHandler.isLightEnabled());
        updateTrainButtons(screenHandler.isTrainEnabled());
        updateFactoryButtons(screenHandler.isFactoryEnabled());
        updateHouseButtons(screenHandler.isHouseEnabled());
        updateFacilityButtons(screenHandler.isFacilityEnabled());

        // ネットワーク受信値
        int energy = screenHandler.getGeneratedEnergy();
        int thermalPower = screenHandler.getThermalEnergy();
        int usedEnergy = 0;
        boolean light = screenHandler.isLightEnabled();
        boolean train = screenHandler.isTrainEnabled();
        boolean factory = screenHandler.isFactoryEnabled();
        boolean house = screenHandler.isHouseEnabled();
        boolean facility = screenHandler.isFacilityEnabled();
        boolean blackout = screenHandler.isBlackout();

        if (light) usedEnergy += LIGHT_CONSUMPTION;
        if (train) usedEnergy += TRAIN_CONSUMPTION;
        if (factory) usedEnergy += FACTORY_CONSUMPTION;
        if (house) usedEnergy += HOUSE_CONSUMPTION;
        if (facility) usedEnergy += FACILITY_CONSUMPTION;

        int surplus = energy - usedEnergy;
        if (surplus < 0) surplus = 0;

        // 火力発電をサーバ側の値に同期
        if (!isThermalSynced) {
            if(thermalPower > 0) {
                slider.setValue(thermalPower);
                isThermalSynced = true;
            }
        }

        // ======================
        // 描画用の値をフレームごとに滑らかに更新
        // ======================
        if (!this.hasSetInitialDisplayed) {
            displayedEnergy = energy;
            displayedSurplus = surplus;
            this.hasSetInitialDisplayed = true;
        } else {
            // energy が最大値を超える場合は更新しない
            int effectiveEnergy = Math.min(energy, maxEnergy);
            int effectiveSurplus = Math.min(surplus, maxEnergy);

            // 収束しきい値を設定（差が2以下なら即座に同期）
            int energyDiff = effectiveEnergy - displayedEnergy;
            int surplusDiff = effectiveSurplus - displayedSurplus;

            if (Math.abs(energyDiff) <= 4) {
                displayedEnergy = effectiveEnergy;
            } else {
                displayedEnergy += energyDiff / 4;
            }

            if (Math.abs(surplusDiff) <= 4) {
                displayedSurplus = effectiveSurplus;
            } else {
                displayedSurplus += surplusDiff / 4;
            }
        }


        int textX = centerX + 30;
        int textY = centerY - 50;

        // ======================
        // Energy Bar
        // ======================
        int barWidth = 150;
        int barHeight = 10;
        int barX = textX;
        int barY = textY;

        // 背景
        context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF555555);

        // 全体のエネルギー
        int filled = (int)((double)displayedEnergy / maxEnergy * barWidth);
        context.fill(barX, barY, barX + filled, barY + barHeight, 0xFF00FF00);

        // 火力発電分
        int thermalEnergy = slider.getThermalPower();
        int thermalWidth = (int)((double)thermalEnergy / maxEnergy * barWidth);
        if (thermalWidth > filled) thermalWidth = filled;
        int thermalBarStart = filled - thermalWidth;
        if (thermalBarStart < barX) thermalBarStart = barX;
        context.fill(thermalBarStart, barY, thermalBarStart + thermalWidth, barY + barHeight, 0xFFFF69B4);

        // Energy テキスト
        String energyText = String.format("受信電力量: %04d kW / %d kW", (int)displayedEnergy, maxEnergy);
        int textWidth = this.textRenderer.getWidth(energyText);
        context.drawText(this.textRenderer, energyText, barX + (barWidth - textWidth) / 2, barY - 10, 0xFFFFFF, false);

        // 火力発電量テキスト
        String thermalRatioText = String.format("火力発電量: %d kW/ %d kW", thermalEnergy, (int)displayedEnergy);
        int thermalRatioWidth = this.textRenderer.getWidth(thermalRatioText);
        context.drawText(this.textRenderer, thermalRatioText, barX + (barWidth - thermalRatioWidth) / 2, barY + barHeight + 2, 0xFFFF69B4, false);

        // スライダー上の文字
        int sliderX = slider.getX();
        int sliderY = slider.getY();
        String thermalText = "火力発電: " + thermalEnergy + " kW / 1000 kW";
        int ThermaltextWidth = this.textRenderer.getWidth(thermalText);
        context.drawText(this.textRenderer, thermalText, sliderX + (slider.getWidth() - ThermaltextWidth) / 2, sliderY - 10, 0xFFFFFF, false);

        // ======================
        // 余裕電力バー
        // ======================
        int surplusBarY = barY + barHeight + 50;
        int surplusBarMax = (int)displayedEnergy; // 最大値を受信電力量に設定
        int surplusFilled = (int)((double)displayedSurplus / surplusBarMax * barWidth);
        context.fill(barX, surplusBarY, barX + barWidth, surplusBarY + barHeight, 0xFF555555);
        context.fill(barX, surplusBarY, barX + surplusFilled, surplusBarY + barHeight, 0xFF00FF00);

        String surplusText = String.format("余裕電力量: %d kW / %d kW", (int)displayedSurplus, (int)displayedEnergy);
        int surplusTextWidth = this.textRenderer.getWidth(surplusText);
        context.drawText(this.textRenderer, surplusText, barX + (barWidth - surplusTextWidth) / 2, surplusBarY - 10, 0xFFFFFF, false);

        // 現在の使用量バー下に表示
        String usedText = String.format("使用量: %d kW", usedEnergy);
        int usedTextWidth = this.textRenderer.getWidth(usedText);
        context.drawText(this.textRenderer, usedText, barX + (barWidth - usedTextWidth) / 2, surplusBarY + barHeight + 2, 0xFFFFFF, false);


        // ======================
        // 予測バー表示
        // ======================
        int baseX = barX + surplusFilled;
        int barH = barHeight;

        renderPredicted(context, mouseX, mouseY, baseX, barX, barH, surplusBarY, displayedEnergy,light, train, factory, house, facility);

        // ======================
        // 他情報表示
        // ======================
        // 表の左上座標
        int tableX = textX + 20;
        int tableY = textY + 90;

        // 列幅
        int col1Width = 50; // ラベル列
        int col2Width = 40; // 状態列
        int rowHeight = this.textRenderer.fontHeight + 4;  // フォント高さ + 余白

        // ヘッダー
        context.drawText(this.textRenderer, "設備", tableX + 2, tableY + 2, 0xFFFFAA00, false);
        context.drawText(this.textRenderer, "状態", tableX + col1Width + 2, tableY + 2, 0xFFFFAA00, false);

        // 線の色
        int lineColor = 0xFFAAAAAA;

        // ヘッダー下の線
        context.fill(tableX, tableY + rowHeight, tableX + col1Width + col2Width, tableY + rowHeight + 1, lineColor);

        // データ行
        String[][] rows = {
                {"街灯", light ? "On" : "Off"},
                {"電車", train ? "On" : "Off"},
                {"工場", factory ? "On" : "Off"},
                {"住宅", house ? "On" : "Off"},
                {"公共施設", facility ? "On" : "Off"}
        };

        for (int i = 0; i < rows.length; i++) {
            int y = tableY + rowHeight * (i + 1);

            // ラベル列
            context.drawText(this.textRenderer, rows[i][0], tableX + 2, y + 2, 0xFFFFFF, false);

            // 状態列
            context.drawText(this.textRenderer, rows[i][1], tableX + col1Width + 2, y + 2, 0xFFFFFF, false);

            // 行下線
            context.fill(tableX, y + rowHeight -1, tableX + col1Width + col2Width, y + rowHeight, lineColor);

            // 列の区切り線（垂直）
            context.fill(tableX + col1Width - 3, y - 13, tableX + col1Width - 2, y + rowHeight, lineColor);
        }


        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    // 描画用フィールド
    private int displayedEnergy = 0;
    private int displayedSurplus = 0;
    private boolean hasSetInitialDisplayed = false;

    // ======================
    // 予測バー描画用メソッド
    // ======================
    private void renderPredicted(DrawContext context, int mouseX, int mouseY, int baseX, int barX, int barH, int barYSurplus, int currentEnergy,
                                 boolean light, boolean train, boolean factory, boolean house, boolean facility) {

        // Light
        if (!light && lightOnButton.isMouseOver(mouseX, mouseY)) {
            int w = (int)((double)LIGHT_CONSUMPTION / currentEnergy * 150);
            int startX = baseX - w;
            if (startX < barX) startX = barX;
            context.fill(startX, barYSurplus, baseX, barYSurplus + barH, 0xFFFF0000);

            int textY = barYSurplus + (barH - this.textRenderer.fontHeight) / 2;
            String text = String.valueOf(LIGHT_CONSUMPTION);
            context.drawText(this.textRenderer,
                    text,
                    startX - this.textRenderer.getWidth(text) - 2,
                    textY,
                    0xFFFF0000,
                    false);
        }
        if (light && lightOffButton.isMouseOver(mouseX, mouseY)) {
                int w = (int)((double)LIGHT_CONSUMPTION / currentEnergy * 150);
            context.fill(baseX, barYSurplus, baseX + w, barYSurplus + barH, 0xFF008800);

            int textY = barYSurplus + (barH - this.textRenderer.fontHeight) / 2;
            String text = String.valueOf(LIGHT_CONSUMPTION);
            context.drawText(this.textRenderer,
                    text,
                    baseX + w + 2,
                    textY,
                    0x8844FF44,
                    false);
        }

        // Train
        if (!train && trainOnButton.isMouseOver(mouseX, mouseY)) {
            int w = (int)((double)TRAIN_CONSUMPTION / currentEnergy * 150);
            int startX = baseX - w;
            if (startX < barX) startX = barX;
            context.fill(startX, barYSurplus, baseX, barYSurplus + barH, 0xFFFF0000);

            int textY = barYSurplus + (barH - this.textRenderer.fontHeight) / 2;
            String text = String.valueOf(TRAIN_CONSUMPTION);
            context.drawText(this.textRenderer,
                    text,
                    startX - this.textRenderer.getWidth(text) - 2,
                    textY,
                    0xFFFF0000,
                    false);
        }
        if (train && trainOffButton.isMouseOver(mouseX, mouseY)) {
            int w = (int)((double)TRAIN_CONSUMPTION / currentEnergy * 150);
            context.fill(baseX, barYSurplus, baseX + w, barYSurplus + barH, 0xFF008800);

            int textY = barYSurplus + (barH - this.textRenderer.fontHeight) / 2;
            String text = String.valueOf(TRAIN_CONSUMPTION);
            context.drawText(this.textRenderer,
                    text,
                    baseX + w + 2,
                    textY,
                    0x8844FF44,
                    false);
        }

        // Factory
        if (!factory && factoryOnButton.isMouseOver(mouseX, mouseY)) {
            int w = (int)((double)FACTORY_CONSUMPTION / currentEnergy * 150);
            int startX = baseX - w;
            if (startX < barX) startX = barX;
            context.fill(startX, barYSurplus, baseX, barYSurplus + barH, 0xFFFF0000);

            int textY = barYSurplus + (barH - this.textRenderer.fontHeight) / 2;
            String text = String.valueOf(FACTORY_CONSUMPTION);
            context.drawText(this.textRenderer,
                    text,
                    startX - this.textRenderer.getWidth(text) - 2,
                    textY,
                    0xFFFF0000,
                    false);
        }
        if (factory && factoryOffButton.isMouseOver(mouseX, mouseY)) {
            int w = (int)((double)FACTORY_CONSUMPTION / currentEnergy * 150);
            context.fill(baseX, barYSurplus, baseX + w, barYSurplus + barH, 0xFF008800);

            int textY = barYSurplus + (barH - this.textRenderer.fontHeight) / 2;
            String text = String.valueOf(FACTORY_CONSUMPTION);
            context.drawText(this.textRenderer,
                    text,
                    baseX + w + 2,
                    textY,
                    0x8844FF44,
                    false);
        }

        // House
        if (!house && houseOnButton.isMouseOver(mouseX, mouseY)) {
            int w = (int)((double)HOUSE_CONSUMPTION / currentEnergy * 150);
            int startX = baseX - w;
            if (startX < barX) startX = barX;
            context.fill(startX, barYSurplus, baseX, barYSurplus + barH, 0xFFFF0000);

            int textY = barYSurplus + (barH - this.textRenderer.fontHeight) / 2;
            String text = String.valueOf(HOUSE_CONSUMPTION);
            context.drawText(this.textRenderer,
                    text,
                    startX - this.textRenderer.getWidth(text) - 2,
                    textY,
                    0xFFFF0000,
                    false);
        }
        if (house && houseOffButton.isMouseOver(mouseX, mouseY)) {
            int w = (int)((double)HOUSE_CONSUMPTION / currentEnergy * 150);
            context.fill(baseX, barYSurplus, baseX + w, barYSurplus + barH, 0xFF008800);

            int textY = barYSurplus + (barH - this.textRenderer.fontHeight) / 2;
            String text = String.valueOf(HOUSE_CONSUMPTION);
            context.drawText(this.textRenderer,
                    text,
                    baseX + w + 2,
                    textY,
                    0x8844FF44,
                    false);
        }

        // Facility
        if (!facility && facilityOnButton.isMouseOver(mouseX, mouseY)) {
            int w = (int)((double)FACILITY_CONSUMPTION / currentEnergy * 150);
            int startX = baseX - w;
            if (startX < barX) startX = barX;
            context.fill(startX, barYSurplus, baseX, barYSurplus + barH, 0xFFFF0000);

            int textY = barYSurplus + (barH - this.textRenderer.fontHeight) / 2;
            String text = String.valueOf(FACILITY_CONSUMPTION);
            context.drawText(this.textRenderer,
                    text,
                    startX - this.textRenderer.getWidth(text) - 2,
                    textY,
                    0xFFFF0000,
                    false);
        }
        if (facility && facilityOffButton.isMouseOver(mouseX, mouseY)) {
            int w = (int)((double)FACILITY_CONSUMPTION / currentEnergy * 150);
            context.fill(baseX, barYSurplus, baseX + w, barYSurplus + barH, 0xFF008800);

            int textY = barYSurplus + (barH - this.textRenderer.fontHeight) / 2;
            String text = String.valueOf(FACILITY_CONSUMPTION);
            context.drawText(this.textRenderer,
                    text,
                    baseX + w + 2,
                    textY,
                    0x8844FF44,
                    false);
        }
    }


    private void drawCenteredLabel(DrawContext context, String text, int cx, int cy) {
        int w = this.textRenderer.getWidth(text);
        context.drawText(this.textRenderer, text, cx - w / 2, cy, 0xFFFFFF, false);
    }
}