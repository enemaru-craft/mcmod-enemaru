package com.enemaru.networking.payload;

import com.enemaru.Enemaru;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EquipmentRequestC2SPayload(String equipment, boolean enable) implements CustomPayload {
    public static final Identifier TURN_ON_REQUEST_PAYLOAD_ID = Identifier.of(Enemaru.MOD_ID, "turn_on_request");
    public static final CustomPayload.Id<EquipmentRequestC2SPayload> ID = new CustomPayload.Id<>(TURN_ON_REQUEST_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, EquipmentRequestC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING,
                    EquipmentRequestC2SPayload::equipment,
                    PacketCodecs.BOOL,
                    EquipmentRequestC2SPayload::enable,
                    EquipmentRequestC2SPayload::new
            );
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
