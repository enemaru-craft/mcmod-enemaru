package com.enemaru.networking.payload;

import com.enemaru.Enemaru;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record StateUpdateRequestC2SPayload(boolean isLightEnabled, boolean isTrainEnabled, boolean isFactoryEnabled) implements CustomPayload {
    public static final Identifier STATE_UPDATE_REQUEST_PAYLOAD_ID = Identifier.of(Enemaru.MOD_ID, "state_update_request");
    public static final CustomPayload.Id<StateUpdateRequestC2SPayload> ID = new CustomPayload.Id<>(STATE_UPDATE_REQUEST_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, StateUpdateRequestC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.BOOL,
                    StateUpdateRequestC2SPayload::isLightEnabled,
                    PacketCodecs.BOOL,
                    StateUpdateRequestC2SPayload::isTrainEnabled,
                    PacketCodecs.BOOL,
                    StateUpdateRequestC2SPayload::isFactoryEnabled,
                    StateUpdateRequestC2SPayload::new
            );
    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
