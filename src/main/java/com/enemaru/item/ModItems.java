package com.enemaru.item;

import com.enemaru.Enemaru;
import com.enemaru.lighting.ChannelConfiguratorItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {
    public static void initialize(){
        // Register the group.
        Registry.register(Registries.ITEM_GROUP, CUSTOM_ITEM_GROUP_KEY, CUSTOM_ITEM_GROUP);

        // Register items to the custom item group.
        ItemGroupEvents.modifyEntriesEvent(CUSTOM_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(ModItems.CONTROL_PANEL_ITEM);
            itemGroup.add(ModItems.CHANNEL_CONFIGURATOR);
            // ...
        });
    }

    public static Item register(Item item, String id){
        // Create the identifier for the item.
        Identifier itemID = Identifier.of(Enemaru.MOD_ID, id);

        // Register the item.
        Item registeredItem = Registry.register(Registries.ITEM, itemID, item);

        // Return the registered item!
        return registeredItem;
    }

    // Custom item group key and item group instance.
    public static final RegistryKey<ItemGroup> CUSTOM_ITEM_GROUP_KEY = RegistryKey.of(Registries.ITEM_GROUP.getKey(), Identifier.of(Enemaru.MOD_ID, "item_group"));
    public static final ItemGroup CUSTOM_ITEM_GROUP = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.CONTROL_PANEL_ITEM))
            .displayName(Text.translatable("itemGroup.enemaru"))
            .build();

    // Register items.
    public static final Item CONTROL_PANEL_ITEM = register(
            new ControlPanelItem(new Item.Settings()),
            "control_panel"
    );

    public static final Item CHANNEL_CONFIGURATOR = register(
            new ChannelConfiguratorItem(new Item.Settings()),
            "channel_configurator"
    );

}
