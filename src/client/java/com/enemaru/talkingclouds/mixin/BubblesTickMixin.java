package com.enemaru.talkingclouds.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.enemaru.talkingclouds.api.BubblesContainer;
import com.enemaru.talkingclouds.api.BubblesHolder;

public abstract class BubblesTickMixin {
    @Mixin(Entity.class)
    public abstract static class EntityHolder implements BubblesHolder {
        @Inject(method = "tick", at = @At("TAIL"))
        private void tickInject(CallbackInfo info) {
            BubblesContainer.of((Entity) (Object) this).tick();
        }
    }

//    @Mixin(AbstractDecorationEntity.class)
//    public abstract static class DecorationEntityHolder implements BubblesHolder {
//        @Inject(method = "tick", at = @At("TAIL"))
//        private void tickInject(CallbackInfo info) {
//            BubblesContainer.of((Entity) (Object) this).tick();
//        }
//    }
}
