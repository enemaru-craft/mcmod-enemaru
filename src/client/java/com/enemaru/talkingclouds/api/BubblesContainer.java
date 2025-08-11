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
        this.addBubble(text, false, false);
    }

    public void addBubble(Text text, boolean enableFormatting, boolean isPersistent) {
        if (!holder.isAlive()) {
            return;
        }

        this.bubbles.add(new Bubble(text, holder.age, enableFormatting, isPersistent));
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

        for (Bubble bubble : this.bubbles){
            if(bubble.isPersistent() & !bubble.isRemoved()){
                if(!holder.isAlive()){
                    // holderのentityが死んでいれば削除
                    bubble.markRemoved(holder.age);
                }
            }else{
                var lastBubble = bubble;
                if (holder.isAlive()) {
                    var lifeSpan = holder.age - lastBubble.getTimings().getSpawnAge();
                    // 時間を超えていれば削除フラグを立てる
                    if (lifeSpan > lastBubble.getTimings().getLifeDuration()) {
                        lastBubble.markRemoved(holder.age);
                    }
                } else {
                    for (Bubble bubble2 : this.bubbles) {
                        bubble2.markRemoved(holder.age);
                    }
                }

                if (lastBubble.isRemoved()) {
                    var dieDuration = ConfigManager.getBubbleDieDuration();
                    if (holder.age - lastBubble.getTimings().getRemoveAge() > dieDuration) {
                        this.bubbles.remove(lastBubble);
                        this.calculateTotalLines();
                    }
                }
                break;
            }
        }
//        var lastBubble = this.bubbles.get(0);


    }
}
