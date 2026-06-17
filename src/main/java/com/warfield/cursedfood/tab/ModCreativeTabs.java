package com.warfield.cursedfood.tab;

import com.warfield.cursedfood.CursedFoodMod;
import com.warfield.cursedfood.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Вкладка в креативном инвентаре для предметов мода CursedFood. */
public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CursedFoodMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CURSED_FOOD_TAB =
            CREATIVE_MODE_TABS.register("cursed_food_tab", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("itemGroup.cursedfood"))
                            .icon(() -> ModItems.BANHAMMER_PIE.get().getDefaultInstance())
                            .displayItems((params, output) ->
                                    output.accept(ModItems.BANHAMMER_PIE.get()))
                            .build()
            );
}
