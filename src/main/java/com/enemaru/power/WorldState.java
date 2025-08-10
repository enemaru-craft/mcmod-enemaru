package com.enemaru.power;

import java.util.List;

public class WorldState {
    public State state;
    public List<String> texts;
    public Variables variables;

    public static class State {
        public boolean isLightEnabled;
        public boolean isTrainEnabled;
        public boolean isFactoryEnabled;
        public boolean isBlackout;
    }

    public static class Variables {
        public float totalPower;
        public float surplusPower;
    }
}
