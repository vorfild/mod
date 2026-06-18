package com.warfield.tankmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * Пакет C→S: текущий ввод водителя (какие клавиши нажаты).
 * Отправляется только при ИЗМЕНЕНИИ состояния клавиш (см. ClientEvents).
 * Record автоматически генерирует equals() — используем для сравнения с предыдущим вводом.
 */
public record TankInputPacket(boolean fwd, boolean back, boolean left, boolean right)
        implements CustomPacketPayload {

    public static final Type<TankInputPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("tankmod", "tank_input"));

    public static final StreamCodec<FriendlyByteBuf, TankInputPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL, TankInputPacket::fwd,
                    ByteBufCodecs.BOOL, TankInputPacket::back,
                    ByteBufCodecs.BOOL, TankInputPacket::left,
                    ByteBufCodecs.BOOL, TankInputPacket::right,
                    TankInputPacket::new
            );

    @Override
    public Type<TankInputPacket> type() { return TYPE; }
}
