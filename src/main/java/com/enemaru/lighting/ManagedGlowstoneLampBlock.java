package com.enemaru.lighting;

import net.minecraft.block.BlockState;

/**
 * チャンネル管理式グロウストーンランプブロック。
 */
public class ManagedGlowstoneLampBlock extends AbstractManagedLightBlock {
    public ManagedGlowstoneLampBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected int getDefaultChannel() {
        return LightChannels.GLOWSTONE; // 異なるデフォルトチャンネルを設定
    }
}

