package com.warfield.tankmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.warfield.tankmod.TankMod;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = TankMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModKeyMappings {

    public static KeyMapping TOGGLE_HATCH;

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        TOGGLE_HATCH = new KeyMapping(
                "key.tankmod.toggle_hatch",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_R,
                "key.categories.tankmod"
        );
        event.register(TOGGLE_HATCH);
    }
}
