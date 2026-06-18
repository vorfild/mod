package com.warfield.tankmod.network;

import com.warfield.tankmod.entity.TankEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadsEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Регистрация и обработка всех сетевых пакетов мода.
 *
 * Вызывается из TankMod через modEventBus.addListener(ModPackets::onRegisterPayloads).
 *
 * Архитектура пакетов:
 *  C→S TankInputPacket   — ввод водителя (W/A/S/D)
 *  C→S TankTurretPacket  — угол башни (добавится в Фазе 2)
 *  C→S TankShootPacket   — выстрел (Фаза 3)
 *  C→S TankLoadPacket    — зарядить снаряд (Фаза 3)
 */
public class ModPackets {

    public static void onRegisterPayloads(RegisterPayloadsEvent event) {
        var reg = event.registrar("tankmod");

        // Фаза 1: ввод движения (W/A/S/D)
        reg.playToServer(
                TankInputPacket.TYPE,
                TankInputPacket.STREAM_CODEC,
                ModPackets::handleTankInput
        );

        // Фаза 2: поворот башни
        reg.playToServer(
                TankTurretPacket.TYPE,
                TankTurretPacket.STREAM_CODEC,
                ModPackets::handleTankTurret
        );

        // Фазы 3–5: пакеты будут добавлены при расширении
    }

    // ─────────────────────────────────────────────────────────────────────
    // Обработчики (исполняются на серверном потоке через enqueueWork)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Применяем ввод клавиш к TankEntity на сервере.
     * enqueueWork гарантирует выполнение в главном серверном потоке.
     */
    private static void handleTankInput(TankInputPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (player.getVehicle() instanceof TankEntity tank) {
                tank.setInput(packet.fwd(), packet.back(), packet.left(), packet.right());
            }
        });
    }

    /**
     * Обновляем угол башни на сервере (и через SynchedEntityData — на всех клиентах).
     */
    private static void handleTankTurret(TankTurretPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (player.getVehicle() instanceof TankEntity tank) {
                tank.setTurretYaw(packet.yaw());
            }
        });
    }
}
