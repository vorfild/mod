package com.warfield.tankmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** C→S сигнал: игрок нажал кнопку атаки в танке. Сервер проверяет условия и стреляет. */
public record TankShootPacket() implements CustomPacketPayload {

    public static final Type<TankShootPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("tankmod", "tank_shoot"));

    public static final StreamCodec<FriendlyByteBuf, TankShootPacket> STREAM_CODEC =
            StreamCodec.unit(new TankShootPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
