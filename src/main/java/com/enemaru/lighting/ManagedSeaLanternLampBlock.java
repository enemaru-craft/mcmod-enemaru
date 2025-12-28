package com.enemaru.lighting;

import net.minecraft.block.BlockState;

/**
 * チャンネル管理式シーランタンランプブロック。
 */
public class ManagedSeaLanternLampBlock extends AbstractManagedLightBlock {
    public ManagedSeaLanternLampBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected int getDefaultChannel() {
        return 1; // 異なるデフォルトチャンネルを設定
    }
}

