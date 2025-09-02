package com.enemaru.blockentity;

import com.enemaru.Enemaru;
import com.enemaru.block.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static void initialize() {}

    public static final BlockEntityType<StreetLightBlockEntity> STREET_LIGHT_ENTITY =
            register("street_light", StreetLightBlockEntity::new, ModBlocks.STREET_LIGHT_BLOCK);

    public static final BlockEntityType<SeaLanternLampBlockEntity> SEA_LANTERN_LAMP_ENTITY =
            register("sea_lantern_lamp", SeaLanternLampBlockEntity::new, ModBlocks.SEA_LANTERN_LAMP_BLOCK);

    public static final BlockEntityType<GlowstoneLampBlockEntity> GLOWSTONE_LAMP_ENTITY =
            register("glowstone_lamp_block", GlowstoneLampBlockEntity::new, ModBlocks.GLOWSTONE_LAMP_BLOCK);

    // エンドロッドランプのブロックエンティティ
    public static final BlockEntityType<EndRodLampBlockEntity> END_ROD_LAMP_ENTITY =
            register("end_rod_lamp_block", EndRodLampBlockEntity::new, ModBlocks.END_ROD_LAMP_BLOCK);

    private static <T extends BlockEntity> BlockEntityType<T> register(String name,
                                                                       BlockEntityType.BlockEntityFactory<? extends T> entityFactory,
                                                                       Block... blocks) {
        Identifier id = Identifier.of(Enemaru.MOD_ID, name);
        return Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                id,
                BlockEntityType.Builder.<T>create(entityFactory, blocks).build(null)
        );
    }
}
