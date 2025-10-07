package com.enemaru.blockentity;

import com.enemaru.block.EndRodLampBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EndRodLampBlockEntity extends BlockEntity {
    private boolean powered = false;

    public EndRodLampBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.END_ROD_LAMP_ENTITY, pos, state);
    }

    /** サーバからの点灯／消灯を反映 */
    public void updatePowered(boolean shouldBeLit, boolean forceUpdate) {
        if (!forceUpdate && this.powered == shouldBeLit) return;
        this.powered = shouldBeLit;

        BlockState oldState = world.getBlockState(pos);
        BlockState newState = oldState.with(EndRodLampBlock.LIT, shouldBeLit); // ← Glowstone ではなく EndRod
        world.setBlockState(pos, newState, 3);
    }

    public static void tick(World world, BlockPos pos, BlockState state, EndRodLampBlockEntity entity) {
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
