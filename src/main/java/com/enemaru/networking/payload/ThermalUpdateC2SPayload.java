package com.enemaru.networking.payload;

import com.enemaru.Enemaru;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ThermalUpdateC2SPayload(int power) implements CustomPayload {
    public static final Identifier THERMAL_UPDATE_PAYLOAD_ID = Identifier.of(Enemaru.MOD_ID, "thermal_update");
    public static final CustomPayload.Id<ThermalUpdateC2SPayload> ID = new CustomPayload.Id<>(THERMAL_UPDATE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, ThermalUpdateC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER,
                    ThermalUpdateC2SPayload::power,
                    ThermalUpdateC2SPayload::new
            );
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
