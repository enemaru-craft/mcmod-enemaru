package com.enemaru.lighting.networking;

import com.enemaru.Enemaru;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * クライアントからサーバーへライトのチャンネルを変更するPayload。
 */
public record ChangeChannelC2SPayload(BlockPos pos, int channel) implements CustomPayload {
    public static final Id<ChangeChannelC2SPayload> ID = new Id<>(Identifier.of(Enemaru.MOD_ID, "change_channel"));

    public static final PacketCodec<ByteBuf, ChangeChannelC2SPayload> CODEC = PacketCodec.tuple(
            BlockPos.PACKET_CODEC, ChangeChannelC2SPayload::pos,
            PacketCodecs.VAR_INT, ChangeChannelC2SPayload::channel,
            ChangeChannelC2SPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

