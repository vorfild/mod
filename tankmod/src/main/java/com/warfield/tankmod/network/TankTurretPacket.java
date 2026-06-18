package com.warfield.tankmod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C→S пакет: желаемый угол башни в мировых координатах (градусы, шкала Minecraft).
 * Отправляется только при изменении угла взгляда игрока более чем на 0.5°.
 */
public record TankTurretPacket(float yaw) implements CustomPacketPayload {

    public static final Type<TankTurretPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("tankmod", "tank_turret"));

    public static final StreamCodec<FriendlyByteBuf, TankTurretPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, TankTurretPacket::yaw,
                    TankTurretPacket::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
