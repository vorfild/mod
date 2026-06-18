package com.warfield.tankmod.client;

import com.warfield.tankmod.ModEntities;
import com.warfield.tankmod.TankMod;
import com.warfield.tankmod.client.renderer.TankRenderer;
import com.warfield.tankmod.entity.TankEntity;
import com.warfield.tankmod.network.TankInputPacket;
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
        // Рендер снаряда добавится в Фазе 3
    }

    // ─── GAME bus — ввод игрока (отдельный subscriber ниже) ───
}

/** Отдельный класс для GAME-bus событий (отвязан от MOD bus выше). */
@EventBusSubscriber(modid = TankMod.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
class TankInputHandler {

    /** Последний отправленный ввод — сравниваем, чтобы не слать пакеты каждый тик. */
    private static TankInputPacket lastSentInput = new TankInputPacket(false, false, false, false);

    /**
     * Каждый тик: если игрок едет на танке — считываем нажатые клавиши.
     * Пакет отправляется ТОЛЬКО при изменении состояния клавиш.
     * Таким образом при постоянно зажатой W отправляется 1 пакет, а не 20/сек.
     */
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !(mc.player.getVehicle() instanceof TankEntity)) return;

        boolean fwd   = mc.options.keyUp.isDown();
        boolean back  = mc.options.keyDown.isDown();
        boolean left  = mc.options.keyLeft.isDown();
        boolean right = mc.options.keyRight.isDown();

        TankInputPacket newInput = new TankInputPacket(fwd, back, left, right);

        if (!newInput.equals(lastSentInput)) {
            PacketDistributor.sendToServer(newInput);
            lastSentInput = newInput;
        }
    }
}
