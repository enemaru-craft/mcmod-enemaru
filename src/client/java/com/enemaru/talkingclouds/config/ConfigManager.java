package com.enemaru.talkingclouds.config;


import com.enemaru.EnemaruClient;

public class ConfigManager {
    public static boolean isChatBubblesEnabled() {
        return EnemaruClient.CONFIG.enableChatBubbles;
    }

    public static boolean isSelfBubblesEnabled() {
        return EnemaruClient.CONFIG.enableSelfBubbles;
    }

    public static int getMaxBubblesStackCount() {
        return EnemaruClient.CONFIG.bubblesMaxStackCount;
    }

    public static int getMaxBubblesDistance() {
        return EnemaruClient.CONFIG.bubblesDistance;
    }

    public static int getBubbleLineDuration() {
        return EnemaruClient.CONFIG.bubbleLineDuration * 20;
    }

    public static int getBubbleSpawnDuration() {
        return (int) (EnemaruClient.CONFIG.bubbleSpawnDuration * 20);
    }

    public static int getBubbleDieDuration() {
        return (int) (EnemaruClient.CONFIG.bubbleDieDuration * 20);
    }

    public static boolean showBubblesWhenHudHidden() {
        return EnemaruClient.CONFIG.showWhenHudHidden;
    }

    public static int getMaxBubbleWidth() {
        return EnemaruClient.CONFIG.maxBubbleWidth;
    }

    public static int getMaxBubbleLines() {
        return EnemaruClient.CONFIG.maxBubbleLines;
    }

    public static boolean isTextCenteredInBubbles() {
        return EnemaruClient.CONFIG.centeredText;
    }
}
