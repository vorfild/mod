package com.warfield.cursedfood.item;

import com.warfield.cursedfood.CursedFoodMod;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Реестр предметов мода. Все предметы регистрируются через DeferredRegister.Items
 * и автоматически получают правильный ResourceLocation: cursedfood:<id>.
 */
public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(CursedFoodMod.MODID);

    /**
     * Пирог с банхаммером (cursedfood:banhammer_pie).
     *
     * В 1.21.1 Item.Properties.food() является сокращением для
     * .component(DataComponents.FOOD, foodProperties) — старый API удалён.
     *
     * Питательность 8 и насыщение 1.2f — как у золотого яблока,
     * чтобы у игрока возник соблазн съесть «такую вкусную» еду.
     */
    public static final DeferredItem<Item> BANHAMMER_PIE = ITEMS.registerItem(
            "banhammer_pie",
            props -> new Item(props.food(
                    new FoodProperties.Builder()
                            .nutrition(8)
                            .saturationModifier(1.2f)
                            .build()
            ))
    );
}
