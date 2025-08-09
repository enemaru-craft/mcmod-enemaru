package com.enemaru.talkingclouds.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "talkingclouds")
public class TalkingcloudsConfig implements ConfigData {
    boolean enableChatBubbles = true;
    boolean enableSelfBubbles = false;
    @ConfigEntry.BoundedDiscrete(min = 16, max = 64)
    int bubblesDistance = 32;
    @ConfigEntry.BoundedDiscrete(min = 2, max = 5)
    int bubblesMaxStackCount = 3;
    @ConfigEntry.BoundedDiscrete(min = 2, max = 5)
    int bubbleLineDuration = 3;
    @ConfigEntry.Gui.Excluded
    float bubbleSpawnDuration = 0.2f;
    @ConfigEntry.Gui.Excluded
    float bubbleDieDuration = 0.5f;
    @ConfigEntry.BoundedDiscrete(min = 15, max = 30)
    int maxBubbleWidth = 20;
    @ConfigEntry.BoundedDiscrete(min = 3, max = 10)
    int maxBubbleLines = 5;
    boolean centeredText = true;
    boolean showWhenHudHidden = false;
}
