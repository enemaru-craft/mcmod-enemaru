package com.enemaru.block;

import com.enemaru.Enemaru;
import com.enemaru.item.ModItems;
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

    public static Block STREET_LIGHT_BLOCK;
    public static Block SEA_LANTERN_LAMP_BLOCK;
    public static Block GLOWSTONE_LAMP_BLOCK;
    public static Block END_ROD_LAMP_BLOCK; // ← 追加
    public static Block STATION_END_ROD_BLOCK;

    public static void initialize() {
        // ブロック登録
        // ランタン
        STREET_LIGHT_BLOCK = new StreetLightBlock(
                AbstractBlock.Settings.copy(Blocks.LANTERN)
                        .luminance(StreetLightBlock::getLuminance)
        );
        // シーランタン
        SEA_LANTERN_LAMP_BLOCK = new SeaLanternLampBlock(
                AbstractBlock.Settings.copy(Blocks.SEA_LANTERN)
                        .luminance(StreetLightBlock::getLuminance)
        );
        // グロウストーンランプ
        GLOWSTONE_LAMP_BLOCK = new GlowstoneLampBlock(
                AbstractBlock.Settings.copy(Blocks.GLOWSTONE)
                        .luminance(GlowstoneLampBlock::getLuminance)
        );
        // エンドロッドランプ
        END_ROD_LAMP_BLOCK = new EndRodLampBlock(
                AbstractBlock.Settings.copy(Blocks.END_ROD)
                        .luminance(EndRodLampBlock::getLuminance)
        );

        STATION_END_ROD_BLOCK = new StationEndRodBlock(
                AbstractBlock.Settings.copy(Blocks.END_ROD)
                        .luminance(StationEndRodBlock::getLuminance)
        );

        // ブロックの登録
        Registry.register(Registries.BLOCK, Identifier.of(Enemaru.MOD_ID, "street_light_block"), STREET_LIGHT_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(Enemaru.MOD_ID, "street_light_block"),
                new BlockItem(STREET_LIGHT_BLOCK, new Item.Settings()));

        Registry.register(Registries.BLOCK, Identifier.of(Enemaru.MOD_ID, "sea_lantern_lamp_block"), SEA_LANTERN_LAMP_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(Enemaru.MOD_ID, "sea_lantern_lamp_block"),
                new BlockItem(SEA_LANTERN_LAMP_BLOCK, new Item.Settings()));

        Registry.register(Registries.BLOCK, Identifier.of(Enemaru.MOD_ID, "glowstone_lamp_block"), GLOWSTONE_LAMP_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(Enemaru.MOD_ID, "glowstone_lamp_block"),
                new BlockItem(GLOWSTONE_LAMP_BLOCK, new Item.Settings()));

        Registry.register(Registries.BLOCK, Identifier.of(Enemaru.MOD_ID, "end_rod_lamp_block"), END_ROD_LAMP_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(Enemaru.MOD_ID, "end_rod_lamp_block"),
                new BlockItem(END_ROD_LAMP_BLOCK, new Item.Settings()));

        Registry.register(Registries.BLOCK, Identifier.of(Enemaru.MOD_ID, "station_end_rod_block"), STATION_END_ROD_BLOCK);
        Registry.register(Registries.ITEM, Identifier.of(Enemaru.MOD_ID, "station_end_rod_block"),
                new BlockItem(STATION_END_ROD_BLOCK, new Item.Settings()));

        // クリエイティブタブに追加
        ItemGroupEvents.modifyEntriesEvent(ModItems.CUSTOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(STREET_LIGHT_BLOCK.asItem());
            itemGroup.add(SEA_LANTERN_LAMP_BLOCK.asItem());
            itemGroup.add(GLOWSTONE_LAMP_BLOCK.asItem());
            itemGroup.add(END_ROD_LAMP_BLOCK.asItem());
            itemGroup.add(STATION_END_ROD_BLOCK.asItem());
        });
    }
}
