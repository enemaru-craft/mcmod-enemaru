package com.enemaru.screen;

import com.enemaru.networking.payload.SetStreetLightsC2SPayload;
import com.enemaru.screenhandler.ControlPanelScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ControlPanelScreen extends HandledScreen<ControlPanelScreenHandler> {

    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/gui/container/dispenser.png");

    public ControlPanelScreen(ControlPanelScreenHandler handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);
        this.backgroundWidth = 176;
        this.backgroundHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = (width - backgroundWidth) / 2;
        int centerY = (height - backgroundHeight) / 2;

        addDrawableChild(ButtonWidget.builder(Text.literal("街灯をオン"), button -> {
            SetStreetLightsC2SPayload payload = new SetStreetLightsC2SPayload(true);
            ClientPlayNetworking.send(payload);
        }).position(centerX + 38, centerY + 30).size(100, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("街灯をオフ"), button -> {
            SetStreetLightsC2SPayload payload = new SetStreetLightsC2SPayload(false);
            ClientPlayNetworking.send(payload);
        }).position(centerX + 38, centerY + 55).size(100, 20).build());
    }


    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        // シンプルな背景色で塗りつぶし
        context.fill(x, y, x + backgroundWidth, y + backgroundHeight, 0xC0101010);
        context.drawBorder(x, y, backgroundWidth, backgroundHeight, 0xFF8B8B8B);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}