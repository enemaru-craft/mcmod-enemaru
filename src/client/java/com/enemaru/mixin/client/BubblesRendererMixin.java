package com.enemaru.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.enemaru.Enemaru;
import com.enemaru.talkingclouds.api.Bubble;
import com.enemaru.talkingclouds.api.BubblesContainer;
import com.enemaru.talkingclouds.config.ConfigManager;

import static com.enemaru.Enemaru.LOGGER;

@Mixin(EntityRenderer.class)
public abstract class BubblesRendererMixin<T extends Entity> {
    @Unique
    private static final Identifier BUBBLE_BACKGROUND =
            Identifier.of(Enemaru.MOD_ID, "textures/gui/background.png");

    @Shadow
    @Final
    protected EntityRenderDispatcher dispatcher;

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void renderInject(T entity, float yaw, float tickDelta, MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers, int light, CallbackInfo info) {
        var bubblesContainer = BubblesContainer.of(entity);
        if (bubblesContainer.hasBubbles()
                && (MinecraftClient.isHudEnabled() || ConfigManager.showBubblesWhenHudHidden())
                && this.renderBubblesIfPresent(entity, bubblesContainer, matrices, vertexConsumers, light)) {
            info.cancel();
        }
    }

    @Unique
    private boolean renderBubblesIfPresent(T entity, BubblesContainer bubblesContainer, MatrixStack matrices,
                                           VertexConsumerProvider vertexConsumers, int light) {
        var squaredDistanceToCamera = this.dispatcher.getSquaredDistanceToCamera(entity);
        var maxDistance = ConfigManager.getMaxBubblesDistance();
        var squaredMaxDistance = (maxDistance * maxDistance);

        if (squaredDistanceToCamera > squaredMaxDistance) {
            return false;
        }

        var maxSeeThroughDistance = 16;
        var squaredSeeThroughDistance = maxSeeThroughDistance * maxSeeThroughDistance;
        var distanceMultiply = (float) (squaredDistanceToCamera / squaredMaxDistance);

        var bubbles = bubblesContainer.getBubbles();
        var totalLines = 0;
        for (int i = 0; i < bubbles.size(); i++) {
            var seeThrough = squaredDistanceToCamera <= squaredSeeThroughDistance;
            var bubble = bubbles.get(bubbles.size() - 1 - i);
            this.drawBubble(entity, bubble, i, totalLines, distanceMultiply, seeThrough, matrices, vertexConsumers, light);
            totalLines += bubble.getTextLines().size();
        }

        return true;
    }

    @Unique
    private void drawBubble(T entity, Bubble bubble, int index, int totalLines, float distanceMultiply, boolean seeThrough,
                            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {

        var xPadding = 4;
        var yPadding = 3;
        var spacing = 2;

        var textLines = bubble.getTextLines();
        float heightOffset = (bubble.getTextLines().size() - 1) * -10 + (totalLines * -10) - (index * (spacing + yPadding));

        var multiplier = 0f;

        if (bubble.isRemoved()) {
            var startDieTime = bubble.getTimings().getRemoveAge();
            multiplier = (entity.age - startDieTime) / (float) ConfigManager.getBubbleDieDuration();
            multiplier = Math.min(multiplier, 1f);

            var offset = -5f * multiplier;
            heightOffset += offset;
        } else {
            var startSpawnTime = bubble.getTimings().getSpawnAge();
            var currentDuration = entity.age - startSpawnTime;
            var spawnDuration = ConfigManager.getBubbleSpawnDuration();

            if (currentDuration <= spawnDuration) {
                multiplier = 1f - (entity.age - startSpawnTime) / (float) ConfigManager.getBubbleSpawnDuration();

                var offset = 5f * multiplier;
                heightOffset += offset;
            }
        }

        float f = ((entity).getStandingEyeHeight() + 0.7f) + (distanceMultiply * 0.5f);
        matrices.push();
        matrices.translate(0.0, f, 0.0);

        matrices.multiply(this.dispatcher.getRotation()); // 常にプレイヤーの方向を向く
//        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(toEulerXyzDegrees(this.dispatcher.getRotation()).y())); // ワールドに垂直

        var scaleMultiply = distanceMultiply * 0.07f;
        matrices.scale(0.025f - scaleMultiply, -0.025f - scaleMultiply, 0.025f - scaleMultiply);
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        TextRenderer textRenderer = this.getTextRenderer();

        var centeredText = ConfigManager.isTextCenteredInBubbles();

        var maxLineWidth = 0;
        for (var text : textLines) {
            var lineWidth = textRenderer.getWidth(text);
            if (lineWidth > maxLineWidth) {
                maxLineWidth = lineWidth;
            }
        }

        var leftX = (int) (-maxLineWidth / 2f) - xPadding;
        var topY = (int) heightOffset - yPadding;
        var rightX = (int) (maxLineWidth / 2f) + xPadding;
        var botY = (int) (textLines.size() * 10 + heightOffset + yPadding - 2);

        var isEntityHidden = entity.isInvisible() && entity.isInvisibleTo(MinecraftClient.getInstance().player);

        if (!isEntityHidden) {
            RenderSystem.setShaderColor(1f, 1f, 1f, Math.max((0.85f - distanceMultiply * 1.4f) * (1 - multiplier), 0.1f));
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();
            RenderSystem.polygonOffset(-1.0f, -10.0f);
            RenderSystem.enablePolygonOffset();

            //RenderSystem.setShaderTexture(0, BUBBLE_BACKGROUND);

            VertexConsumerProvider.Immediate bubbleLayerProvider = VertexConsumerProvider.immediate(new BufferAllocator(256));

            MinecraftClient client = MinecraftClient.getInstance();
            DrawContext context = DrawContextAccessor.getDrawContext(client, matrices, bubbleLayerProvider);

//            LOGGER.info("Drawing bubble for entity: {}, index: {}, totalLines: {}, distanceMultiply: {}, seeThrough: {}, text: {}",
//                    entity.getName().getString(), index, totalLines, distanceMultiply, seeThrough, textLines.getFirst().getString());
            context.drawTexture(BUBBLE_BACKGROUND, leftX, topY, 0f, 0f, 4, 4, 16, 16);
            context.drawTexture(BUBBLE_BACKGROUND, rightX - 4, topY, 8f, 0f, 4, 4, 16, 16);
            context.drawTexture(BUBBLE_BACKGROUND, leftX, botY - 4, 0f, 8f, 4, 4, 16, 16);
            context.drawTexture(BUBBLE_BACKGROUND, rightX - 4, botY - 4, 8f, 8f, 4, 4, 16, 16);

            context.drawTexture(BUBBLE_BACKGROUND, leftX, topY + 4, 4, (botY - 4) - (topY + 4), 0f, 4f, 4, 4, 16, 16);
            context.drawTexture(BUBBLE_BACKGROUND, rightX - 4, topY + 4, 4, (botY - 4) - (topY + 4), 8f, 4f, 4, 4, 16, 16);
            context.drawTexture(BUBBLE_BACKGROUND, leftX + 4, topY, (rightX - 4) - (leftX + 4), 4, 4f, 0f, 4, 4, 16, 16);
            context.drawTexture(BUBBLE_BACKGROUND, leftX + 4, botY - 4, (rightX - 4) - (leftX + 4), 4, 4f, 8f, 4, 4, 16, 16);

            context.drawTexture(BUBBLE_BACKGROUND, leftX + 4, topY + 4, (rightX - 4) - (leftX + 4), (botY - 4) - (topY + 4), 4f, 4f, 4, 4, 16, 16);

            if (index == 0) {
                int xPos = Math.min((int) (entity.getWidth() * 5), rightX - 12);
                var off = -0.01;
                matrices.translate(0.0, 0.0, off);
                context.drawTexture(BUBBLE_BACKGROUND, xPos, botY - 1, 0f, 12f, 12, 4, 16, 16);
                matrices.translate(0.0, 0.0, -off);
            }

            bubbleLayerProvider.draw();
        }

        double textOpacity = Math.max(1.0 - multiplier - distanceMultiply * 1, 0.1);
        int textColor = (int) (textOpacity * 255.0) << 24 | 0xffffff;

        double backTextOpacity = Math.max(0.3 * (1.0 - multiplier), 0.1);
        int backTextColor = (int) (backTextOpacity * 255.0) << 24 | 0xffffff;

        for (int i = 0; i < textLines.size(); i++) {
            var text = textLines.get(i);
            float textHalfWidth = centeredText ? -textRenderer.getWidth(text) / 2f : -maxLineWidth / 2f;
            if (seeThrough) {
                textRenderer.draw(text, textHalfWidth, (int) heightOffset + (i * 10), backTextColor, seeThrough, matrix4f, vertexConsumers, isEntityHidden ? TextRenderer.TextLayerType.NORMAL : TextRenderer.TextLayerType.SEE_THROUGH, 0, light);
            }
            if (!isEntityHidden) {
                textRenderer.draw(text, textHalfWidth, (int) heightOffset + (i * 10), textColor, seeThrough, matrix4f, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, light);
            }
        }

        matrices.pop();

        // Reset RenderSystem state to default
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.polygonOffset(0f, 0f);
        RenderSystem.disablePolygonOffset();

    }

    @Unique
    private static Vector3f toEulerXyz(Quaternionf quaternionf) {
        float f = quaternionf.w() * quaternionf.w();
        float g = quaternionf.x() * quaternionf.x();
        float h = quaternionf.y() * quaternionf.y();
        float i = quaternionf.z() * quaternionf.z();
        float j = f + g + h + i;
        float k = 2.0f * quaternionf.w() * quaternionf.x() - 2.0f * quaternionf.y() * quaternionf.z();
        float l = (float) Math.asin(k / j);
        if (Math.abs(k) > 0.999f * j) {
            return new Vector3f(l, 2.0f * (float) Math.atan2(quaternionf.y(), quaternionf.w()), 0.0f);
        }
        return new Vector3f(l, (float) Math.atan2(2.0f * quaternionf.x() * quaternionf.z() + 2.0f * quaternionf.y() * quaternionf.w(), f - g - h + i),
                (float) Math.atan2(2.0f * quaternionf.x() * quaternionf.y() + 2.0f * quaternionf.w() * quaternionf.z(), f - g + h - i));
    }

    @Unique
    private static Vector3f toEulerXyzDegrees(Quaternionf quaternionf) {
        Vector3f vec3f = BubblesRendererMixin.toEulerXyz(quaternionf);
        return new Vector3f((float) Math.toDegrees(vec3f.x()), (float) Math.toDegrees(vec3f.y()), (float) Math.toDegrees(vec3f.z()));
    }
}
