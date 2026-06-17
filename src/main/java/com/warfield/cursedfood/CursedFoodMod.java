package com.warfield.cursedfood;

import com.mojang.logging.LogUtils;
import com.warfield.cursedfood.item.ModItems;
import com.warfield.cursedfood.tab.ModCreativeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Точка входа мода. NeoForge инжектирует modEventBus и modContainer через конструктор.
 */
@Mod(CursedFoodMod.MODID)
public class CursedFoodMod {

    public static final String MODID = "cursedfood";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CursedFoodMod(IEventBus modEventBus, ModContainer modContainer) {
        // Регистрируем предметы и вкладку через мод-шину (регистры — это мод-события)
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        // SERVER-конфиг хранится в saves/<мир>/serverconfig/cursedfood-server.toml
        // и синхронизируется с клиентами при подключении.
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SPEC);
    }
}
