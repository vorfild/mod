package com.warfield.tankmod.event;

import com.warfield.tankmod.TankMod;
import com.warfield.tankmod.entity.TankEntity;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;

/**
 * Серверные игровые события мода (GAME bus).
 * Защита командира танка: пока люк закрыт, пассажир неуязвим к любому урону.
 */
@EventBusSubscriber(modid = TankMod.MODID)
public class TankEventHandler {

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity entity = event.getEntity();
        // Защищаем только пассажира (не сам танк)
        if (entity.getVehicle() instanceof TankEntity tank && !tank.isHatchOpen()) {
            event.setCanceled(true);
        }
    }
}
