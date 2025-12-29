package com.enemaru.networking.payload;

import com.enemaru.Enemaru;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record EquipmentPercentC2SPayload(String equipment, int percent) implements CustomPayload {
    public static final Identifier EQUIPMENT_PERCENT_PAYLOAD_ID = Identifier.of(Enemaru.MOD_ID, "equipment_percent");
    public static final CustomPayload.Id<EquipmentPercentC2SPayload> ID = new CustomPayload.Id<>(EQUIPMENT_PERCENT_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, EquipmentPercentC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING,
                    EquipmentPercentC2SPayload::equipment,
                    PacketCodecs.INTEGER,
                    EquipmentPercentC2SPayload::percent,
                    EquipmentPercentC2SPayload::new
            );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}

