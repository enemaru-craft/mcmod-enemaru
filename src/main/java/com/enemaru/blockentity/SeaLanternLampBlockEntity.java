package com.enemaru.blockentity;

import com.enemaru.block.SeaLanternLampBlock;
import com.enemaru.block.StreetLightBlock;
import com.enemaru.power.PowerNetwork;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * シーランタンランプの BlockEntity。
 * PowerNetwork から受け取ったフラグで点灯状態を updatePowered() で反映します。
 */
public class SeaLanternLampBlockEntity extends BlockEntity {
    private boolean powered = false;

    public SeaLanternLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SEA_LANTERN_LAMP_ENTITY, pos, state);
    }

    /** サーバからの点灯／消灯を反映 */
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
