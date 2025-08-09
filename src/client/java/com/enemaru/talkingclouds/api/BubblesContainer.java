package com.enemaru.talkingclouds.api;

import com.enemaru.talkingclouds.config.ConfigManager;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class BubblesContainer {
    private final List<Bubble> bubbles;
    private final Entity holder;
    private int totalLines;

    public BubblesContainer(Entity holder) {
        this.bubbles = new ArrayList<>();
        this.holder = holder;
    }

    public static BubblesContainer of(Entity entity) {
        return BubblesHolder.of(entity).talkingclouds$getBubblesContainer();
    }

    public List<Bubble> getBubbles() {
        return this.bubbles;
    }

    public void addBubble(Text text) {
        this.addBubble(text, false);
    }

    public void addBubble(Text text, boolean enableFormatting) {
        if (!holder.isAlive()) {
            return;
        }

        this.bubbles.add(new Bubble(text, holder.age, enableFormatting));
        this.calculateTotalLines();

        var maxStack = ConfigManager.getMaxBubblesStackCount();
        var maxTotalLines = ConfigManager.getMaxBubbleLines();

        if (this.bubbles.size() > 2 && this.totalLines > maxTotalLines) {
            this.bubbles.get(0).markRemoved(holder.age);
        }

        var outOfStackCount = this.bubbles.size() - maxStack;
        if (outOfStackCount > 0) {
            for (int i = outOfStackCount - 1; i >= 0; i--) {
                if (i >= maxStack) {
                    this.bubbles.remove(i);
                } else {
                    this.bubbles.get(i).markRemoved(holder.age);
                }
            }
        }
    }

    private void calculateTotalLines() {
        var totalLines = 0;

        for (var bubble : this.bubbles) {
            totalLines += bubble.getTextLines().size();
        }

        this.totalLines = totalLines;
    }

    public boolean hasBubbles() {
        return !this.bubbles.isEmpty();
    }

    public void tick() {
        if (!this.hasBubbles()) {
            return;
        }

        var lastBubble = this.bubbles.get(0);

        if (holder.isAlive()) {
            var lifeSpan = holder.age - lastBubble.getTimings().getSpawnAge();
            if (lifeSpan > lastBubble.getTimings().getLifeDuration()) {
                lastBubble.markRemoved(holder.age);
            }
        } else {
            for (Bubble bubble : this.bubbles) {
                bubble.markRemoved(holder.age);
            }
        }

        if (lastBubble.isRemoved()) {
            var dieDuration = ConfigManager.getBubbleDieDuration();
            if (holder.age - lastBubble.getTimings().getRemoveAge() > dieDuration) {
                this.bubbles.remove(0);
                this.calculateTotalLines();
            }
        }
    }
}
