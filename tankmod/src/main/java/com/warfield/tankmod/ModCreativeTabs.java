package com.warfield.tankmod;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TankMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TANK_TAB =
            CREATIVE_MODE_TABS.register("tank_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.tankmod"))
                            .icon(() -> ModItems.TANK_SPAWNER.get().getDefaultInstance())
                            .displayItems((params, output) -> {
                                output.accept(ModItems.TANK_SPAWNER.get());
                                output.accept(ModItems.TANK_SHELL.get());
                            })
                            .build()
            );
}
