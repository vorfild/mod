package com.warfield.tankmod.client;

import com.warfield.tankmod.ModEntities;
import com.warfield.tankmod.TankMod;
import com.warfield.tankmod.client.renderer.TankRenderer;
import com.warfield.tankmod.client.renderer.TankShellRenderer;
import com.warfield.tankmod.entity.TankEntity;
import com.warfield.tankmod.network.TankInputPacket;
import com.warfield.tankmod.network.TankShootPacket;
import com.warfield.tankmod.network.TankTurretPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.tick.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Все клиентские события мода:
 *  - Регистрация рендеров (MOD bus)
 *  - Чтение ввода и отправка TankInputPacket на сервер (GAME bus)
 */
@EventBusSubscriber(modid = TankMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    // ─── MOD bus — регистрация рендеров ───

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.TANK.get(), TankRenderer::new);
        event.registerEntityRenderer(ModEntities.TANK_SHELL_ENTITY.get(), TankShellRenderer::new);
    }

    // ─── GAME bus — ввод игрока (отдельный subscriber ниже) ───
}

/** Отдельный класс для GAME-bus событий (отвязан от MOD bus выше). */
@EventBusSubscriber(modid = TankMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
class TankInputHandler {

    /** Последний отправленный ввод движения — не слать пакеты каждый тик без изменений. */
    private static TankInputPacket lastSentInput = new TankInputPacket(false, false, false, false);

    /** Последний отправленный угол башни (мировой, градусы). */
    private static float lastSentTurretYaw = Float.NaN;

    /** Было ли нажато «атака» в прошлом тике (для edge-detection). */
    private static boolean wasAttackDown = false;

    /**
     * Каждый тик: если игрок едет на танке — считываем клавиши и угол взгляда.
     * Пакеты отправляются ТОЛЬКО при изменении состояния (оптимизация трафика).
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof TankEntity tank)) {
            // Сбрасываем при выходе из танка
            lastSentInput    = new TankInputPacket(false, false, false, false);
            lastSentTurretYaw = Float.NaN;
            wasAttackDown    = false;
            return;
        }

        // ── Движение ──────────────────────────────────────────────────
        boolean fwd   = mc.options.keyUp.isDown();
        boolean back  = mc.options.keyDown.isDown();
        boolean left  = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();

        TankInputPacket newInput = new TankInputPacket(fwd, back, left, right);
        if (!newInput.equals(lastSentInput)) {
            PacketDistributor.sendToServer(newInput);
            lastSentInput = newInput;
        }

        // ── Поворот башни ─────────────────────────────────────────────
        float desiredYaw = mc.player.getYRot();
        if (Float.isNaN(lastSentTurretYaw) || Math.abs(desiredYaw - lastSentTurretYaw) > 0.5f) {
            PacketDistributor.sendToServer(new TankTurretPacket(desiredYaw));
            lastSentTurretYaw = desiredYaw;
        }

        // ── Выстрел (кнопка «атака», ПКМ не используем — она для погрузки снарядов) ──
        boolean isAttackDown = mc.options.keyAttack.isDown();
        if (isAttackDown && !wasAttackDown) {
            PacketDistributor.sendToServer(new TankShootPacket());
        }
        wasAttackDown = isAttackDown;

        // ── HUD: боезапас (обновляем каждые 10 тиков) ─────────────────
        if (mc.player.tickCount % 10 == 0) {
            Component hud;
            if (tank.getReloadTimer() > 0) {
                hud = Component.translatable("tankmod.reloading");
            } else {
                hud = Component.translatable("tankmod.ammo",
                        tank.getLoadedShells(), tank.getMaxShells());
            }
            mc.player.displayClientMessage(hud, true); // true = ActionBar (над хотбаром)
        }
    }
}
