package com.enemaru.networking.payload;


import com.enemaru.Enemaru;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;

public record SendBubbleS2CPayload(int entityId, Text text, boolean isPersistent, boolean isReset) implements CustomPayload{
    public static final Identifier SEND_BUBBLE_PAYLOAD_ID = Identifier.of(Enemaru.MOD_ID, "send_bubble");
    public static final Id<SendBubbleS2CPayload> ID = new Id<>(SEND_BUBBLE_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, SendBubbleS2CPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER,                // boolean を読み書きする組み込みCodec
                    SendBubbleS2CPayload::entityId,
                    TextCodecs.PACKET_CODEC,        // Text text
                    SendBubbleS2CPayload::text,     // getter
                    PacketCodecs.BOOL,            // boolean isPersistent
                    SendBubbleS2CPayload::isPersistent, // getter
                    PacketCodecs.BOOL,
                    SendBubbleS2CPayload::isReset,
                    SendBubbleS2CPayload::new       // boolean → payload 再構築
            );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
