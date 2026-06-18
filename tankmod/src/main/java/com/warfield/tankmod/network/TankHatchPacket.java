package com.warfield.tankmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C→S: игрок нажал R — переключить состояние люка (открыт/закрыт). */
public record TankHatchPacket() implements CustomPacketPayload {

    public static final Type<TankHatchPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("tankmod", "tank_hatch"));

    public static final StreamCodec<FriendlyByteBuf, TankHatchPacket> STREAM_CODEC =
            StreamCodec.unit(new TankHatchPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
