package com.enemaru.talkingclouds.api;

import net.minecraft.entity.Entity;

public interface BubblesHolder {
    static BubblesHolder of(Entity entity) {
        return (BubblesHolder) entity;
    }

    BubblesContainer talkingclouds$getBubblesContainer();
}
