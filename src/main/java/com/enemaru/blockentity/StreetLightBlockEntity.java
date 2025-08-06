package com.enemaru.blockentity;

import com.enemaru.Enemaru;
import com.enemaru.block.StreetLightBlock;
import com.enemaru.power.PowerNetwork;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 街灯の BlockEntity。
 * PowerNetwork から受け取ったフラグで点灯状態を updatePowered() で反映します。
 */
public class StreetLightBlockEntity extends BlockEntity {
    //private boolean powered = false;

    public StreetLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STREET_LIGHT_ENTITY, pos, state);
    }

//    /** チャンク読み込み時にネットワークへ登録 */
//    @Override
//    public void onLoad() {
//        if (world instanceof ServerWorld sw) {
//            PowerNetwork.get(sw).registerStreetLight(this);
//        }
//    }
//
//    /** チャンク削除／ブロック破壊時に登録解除 */
//    @Override
//    public void markRemoved() {
//        super.markRemoved();
//        if (world instanceof ServerWorld sw) {
//            PowerNetwork.get(sw).unregisterStreetLight(this);
//        }
//    }
//
//    /** PowerNetwork から呼ばれる一斉制御メソッド */
//    public void updatePowered(boolean shouldBeLit) {
//        if (this.powered == shouldBeLit) return;
//        this.powered = shouldBeLit;
//        world.setBlock(pos,
//                world.getBlockState(pos).with(StreetLightBlock.LIT, shouldBeLit),
//                Block.NOTIFY_LISTENERS
//        );
//    }

    /** tick() の中身は空で OK（一斉制御は PowerNetwork 側で） */
    public static void tick(World world, BlockPos blockPos, BlockState blockState, StreetLightBlockEntity entity) {
        // （不要）
    }

    // NBT 保存／読み込みが必要なら以下をオーバーライド
    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        //this.powered = nbt.getBoolean("powered");
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        //nbt.putBoolean("powered", powered);
        super.writeNbt(nbt, registryLookup);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }
}