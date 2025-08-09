package com.enemaru.talkingclouds.mixin;

import com.enemaru.talkingclouds.api.BubblesContainer;
import com.enemaru.talkingclouds.api.BubblesHolder;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class BubblesHolderMixin implements BubblesHolder {
    @Unique
    private final BubblesContainer bubblesContainer = new BubblesContainer((Entity) (Object) this);

    @Override
    public BubblesContainer talkingclouds$getBubblesContainer() {
        return this.bubblesContainer;
    }
}
