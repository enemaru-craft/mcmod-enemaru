package com.enemaru.block;

import com.enemaru.Enemaru;
import com.enemaru.item.ModItems;
import com.enemaru.lighting.*;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {
    // 新システム（チャンネル管理式）
    public static Block MANAGED_LANTERN_BLOCK;
    public static Block MANAGED_SEA_LANTERN_LAMP_BLOCK;
    public static Block MANAGED_END_ROD_LAMP_BLOCK;
    public static Block MANAGED_GLOWSTONE_LAMP_BLOCK;
    public static Block MANAGED_STATION_END_ROD_BLOCK;

    public static void initialize() {
        // ===== 新システム（チャンネル管理式）=====
        // 管理式街灯
        MANAGED_LANTERN_BLOCK = new ManagedLanternBlock(
                AbstractBlock.Settings.copy(Blocks.LANTERN)
                        .luminance(ManagedLanternBlock::getLuminance)
        );
        // 管理式シーランタンランプ
        MANAGED_SEA_LANTERN_LAMP_BLOCK = new ManagedSeaLanternLampBlock(
                AbstractBlock.Settings.copy(Blocks.SEA_LANTERN)
                        .luminance(ManagedSeaLanternLampBlock::getLuminance)
        );
        // 管理式エンドロッドランプ
        MANAGED_END_ROD_LAMP_BLOCK = new ManagedEndRodLampBlock(
                AbstractBlock.Settings.copy(Blocks.END_ROD)
                        .luminance(ManagedEndRodLampBlock::getLuminance)
        );
        // 管理式グロウストーンランプ
        MANAGED_GLOWSTONE_LAMP_BLOCK = new ManagedGlowstoneLampBlock(
                AbstractBlock.Settings.copy(Blocks.GLOWSTONE)
                        .luminance(ManagedGlowstoneLampBlock::getLuminance)
        );
        // 管理式駅エンドロッド
        MANAGED_STATION_END_ROD_BLOCK = new ManagedStationEndRodBlock(
                AbstractBlock.Settings.copy(Blocks.END_ROD)
                        .luminance(ManagedStationEndRodBlock::getLuminance)
        );

        // ===== 新システムの登録 =====
        Registry.register(Registries.BLOCK, Identifier.of(Enemaru.MOD_ID, "managed_lantern_block"), MANAGED_LANTERN_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(Enemaru.MOD_ID, "managed_lantern_block"),
                new BlockItem(MANAGED_LANTERN_BLOCK, new Item.Settings()));

        Registry.register(Registries.BLOCK, Identifier.of(Enemaru.MOD_ID, "managed_sea_lantern_lamp_block"), MANAGED_SEA_LANTERN_LAMP_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(Enemaru.MOD_ID, "managed_sea_lantern_lamp_block"),
                new BlockItem(MANAGED_SEA_LANTERN_LAMP_BLOCK, new Item.Settings()));

        Registry.register(Registries.BLOCK, Identifier.of(Enemaru.MOD_ID, "managed_end_rod_lamp_block"), MANAGED_END_ROD_LAMP_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(Enemaru.MOD_ID, "managed_end_rod_lamp_block"),
                new BlockItem(MANAGED_END_ROD_LAMP_BLOCK, new Item.Settings()));

        Registry.register(Registries.BLOCK, Identifier.of(Enemaru.MOD_ID, "managed_glowstone_lamp_block"), MANAGED_GLOWSTONE_LAMP_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(Enemaru.MOD_ID, "managed_glowstone_lamp_block"),
                new BlockItem(MANAGED_GLOWSTONE_LAMP_BLOCK, new Item.Settings()));

        Registry.register(Registries.BLOCK, Identifier.of(Enemaru.MOD_ID, "managed_station_end_rod_block"), MANAGED_STATION_END_ROD_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(Enemaru.MOD_ID, "managed_station_end_rod_block"),
                new BlockItem(MANAGED_STATION_END_ROD_BLOCK, new Item.Settings()));

        // クリエイティブタブに追加
        ItemGroupEvents.modifyEntriesEvent(ModItems.CUSTOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(MANAGED_LANTERN_BLOCK.asItem());
            itemGroup.add(MANAGED_SEA_LANTERN_LAMP_BLOCK.asItem());
            itemGroup.add(MANAGED_END_ROD_LAMP_BLOCK.asItem());
            itemGroup.add(MANAGED_GLOWSTONE_LAMP_BLOCK.asItem());
            itemGroup.add(MANAGED_STATION_END_ROD_BLOCK.asItem());
        });
    }
}
