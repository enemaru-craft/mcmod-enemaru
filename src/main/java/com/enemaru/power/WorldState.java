package com.enemaru.power;

import java.util.Map;

public class WorldState {
    public State state;
    public Map<String, TalkEntry> texts; // 以前: List<String>
    public Variables variables;

    public static class State {
        public boolean isHouseEnabled;
        public boolean isFacilityEnabled;
        public boolean isLightEnabled;
        public boolean isTrainEnabled;
        public boolean isFactoryEnabled;
        public boolean isBlackout;
    }

    public static class Variables {
        public float totalPower;
        public float surplusPower;
    }

    public static class TalkEntry {
        public String text;
        public String sentiment; // "positive" | "negative" など
    }
}
