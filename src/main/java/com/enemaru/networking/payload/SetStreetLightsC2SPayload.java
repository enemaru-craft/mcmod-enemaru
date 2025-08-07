package com.enemaru.networking.payload;

import com.enemaru.Enemaru;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * クライアント → サーバー：任意の boolean 値を送るペイロード
 */
public record SetStreetLightsC2SPayload(boolean value) implements CustomPayload {
    public static final Identifier SET_STREET_LIGHTS_PAYLOAD_ID = Identifier.of(Enemaru.MOD_ID, "give_glowing_effect");
    public static final Id<SetStreetLightsC2SPayload> ID = new Id<>(SET_STREET_LIGHTS_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, SetStreetLightsC2SPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.BOOL,                // boolean を読み書きする組み込みCodec
                    SetStreetLightsC2SPayload::value,    // payload → boolean 抜き出し
                    SetStreetLightsC2SPayload::new       // boolean → payload 再構築
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
