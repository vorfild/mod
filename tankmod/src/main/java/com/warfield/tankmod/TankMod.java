package com.warfield.tankmod;

import com.mojang.logging.LogUtils;
import com.warfield.tankmod.network.ModPackets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(TankMod.MODID)
public class TankMod {

    public static final String MODID = "tankmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TankMod(IEventBus modEventBus, ModContainer modContainer) {
        // Регистрируем типы сущностей, предметы, вкладку
        ModEntities.ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // Регистрируем сетевые пакеты (C↔S ввод, поворот башни, выстрел)
        modEventBus.addListener(ModPackets::onRegisterPayloads);

        // SERVER-конфиг: saves/<мир>/serverconfig/tankmod-server.toml
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }
}
