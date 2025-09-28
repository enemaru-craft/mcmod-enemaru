package com.enemaru.item;

import com.enemaru.power.PowerNetwork;
import com.enemaru.screenhandler.ControlPanelScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ControlPanelItem extends Item {
    public ControlPanelItem(Settings settings) {
        super(settings);
    }

    // 右クリック時に GUI を開く
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openHandledScreen(new NamedScreenHandlerFactory() {
                @Override
                public Text getDisplayName() {
                    return Text.translatable("screen.enemaru.control_panel");
                }

                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity p) {
                    // delegate を使ってサーバ側のエネルギー量とクライアント側を同期する
                    PropertyDelegate delegate = new PropertyDelegate() {
                        @Override
                        public int get(int index) {
                            var network = PowerNetwork.get(serverPlayer.getServerWorld());
                            return switch (index) {
                                case ControlPanelScreenHandler.PROP_ENERGY -> network.getGeneratedEnergy();
                                case ControlPanelScreenHandler.PROP_SURPLUS -> network.getSurplusEnergy();
                                case ControlPanelScreenHandler.PROP_LIGHT -> network.getStreetlightsEnabled() ? 1 : 0;
                                case ControlPanelScreenHandler.PROP_TRAIN -> network.getTrainEnabled() ? 1 : 0;
                                case ControlPanelScreenHandler.PROP_FACTORY -> network.getFactoryEnabled() ? 1 : 0;
                                case ControlPanelScreenHandler.PROP_BLACKOUT -> network.getBlackout() ? 1 : 0;
                                case ControlPanelScreenHandler.PROP_HOUSE -> network.getHouseEnabled() ? 1 : 0;
                                default -> -1;
                            };
                        }

                        @Override
                        public void set(int index, int value) {

                        }

                        @Override
                        public int size() {
                            return ControlPanelScreenHandler.NUM_PROPS;
                        }
                    };
                    return new ControlPanelScreenHandler(syncId, inv, delegate);
                }
            });
        }
        return TypedActionResult.success(stack, world.isClient());
    }
}
