package com.warfield.tankmod.network;

import com.warfield.tankmod.entity.TankEntity;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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

    public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
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

        // Фаза 3: выстрел
        reg.playToServer(
                TankShootPacket.TYPE,
                TankShootPacket.STREAM_CODEC,
                ModPackets::handleTankShoot
        );

        // Фаза 5: переключение люка
        reg.playToServer(
                TankHatchPacket.TYPE,
                TankHatchPacket.STREAM_CODEC,
                ModPackets::handleTankHatch
        );
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

    /**
     * Игрок нажал кнопку выстрела. Сервер проверяет условия и создаёт снаряд.
     */
    private static void handleTankShoot(TankShootPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (player.getVehicle() instanceof TankEntity tank) {
                tank.shoot();
            }
        });
    }

    /**
     * Переключаем состояние люка и сообщаем водителю текущий статус.
     */
    private static void handleTankHatch(TankHatchPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;
            if (player.getVehicle() instanceof TankEntity tank) {
                boolean nowOpen = !tank.isHatchOpen();
                tank.setHatchOpen(nowOpen);
                String key = nowOpen ? "tankmod.hatch.open" : "tankmod.hatch.closed";
                player.displayClientMessage(net.minecraft.network.chat.Component.translatable(key), true);
            }
        });
    }
}
