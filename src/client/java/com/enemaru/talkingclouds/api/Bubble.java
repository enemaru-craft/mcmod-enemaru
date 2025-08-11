package com.enemaru.talkingclouds.api;

import com.enemaru.talkingclouds.config.ConfigManager;
import com.enemaru.talkingclouds.utils.TextProcessor;
import net.minecraft.text.Text;

import java.util.List;

public class Bubble {
    private final List<Text> textLines;
    private final BubbleTimings timings;
    private boolean removed;
    private boolean isPersistent;

    public Bubble(Text text, int holderAge, boolean enableFormatting, boolean isPersistent) {
        this.textLines = Bubble.splitTextToLines(text, enableFormatting);
        this.timings = new BubbleTimings();
        this.timings.spawnAge = holderAge;
        this.isPersistent = isPersistent;

        var oneLineDuration = ConfigManager.getBubbleLineDuration();
        this.timings.lifeDuration = this.textLines.size() > 1
                ? oneLineDuration + (int) (oneLineDuration * this.textLines.size() * 0.3)
                : oneLineDuration;
    }

    private static List<Text> splitTextToLines(Text text, boolean enableFormatting) {
        int maxLineLength = ConfigManager.getMaxBubbleWidth();
        return TextProcessor.splitText(text, maxLineLength, enableFormatting);
    }

    public List<Text> getTextLines() {
        return this.textLines;
    }

    public void markRemoved(int holderAge) {
        if (!this.removed) {
            this.removed = true;
            this.timings.removeAge = holderAge;
        }
    }

    public boolean isRemoved() {
        return this.removed;
    }

    public boolean isPersistent(){
        return this.isPersistent;
    }

    public BubbleTimings getTimings() {
        return this.timings;
    }

    public static class BubbleTimings {
        private int spawnAge;
        private int removeAge;
        private int lifeDuration;

        public int getSpawnAge() {
            return this.spawnAge;
        }

        public int getRemoveAge() {
            return this.removeAge;
        }

        public int getLifeDuration() {
            return this.lifeDuration;
        }
    }
}
