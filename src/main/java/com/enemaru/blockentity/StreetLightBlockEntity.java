package com.enemaru.blockentity;

import com.enemaru.block.StreetLightBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * 街灯の BlockEntity。
 * PowerNetwork から受け取ったフラグで点灯状態を updatePowered() で反映します。
 */
public class StreetLightBlockEntity extends BlockEntity {
    private boolean powered = false;

    public StreetLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STREET_LIGHT_ENTITY, pos, state);
    }

    public void updatePowered(boolean shouldBeLit) {
        // 変化がないならスキップ
        if (this.powered == shouldBeLit) return;
        this.powered = shouldBeLit;

        // 現在の BlockState を取得
        BlockState oldState = world.getBlockState(pos);
        // LIT プロパティだけ上書きした新しい状態を作成
        BlockState newState = oldState.with(StreetLightBlock.LIT, shouldBeLit);

        // ブロックを差し替え（3 = CLIENT & RENDERER に通知）
        world.setBlockState(pos, newState, 3);
    }

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