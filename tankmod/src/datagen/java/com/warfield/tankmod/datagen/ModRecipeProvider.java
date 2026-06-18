package com.warfield.tankmod.datagen;

import com.warfield.tankmod.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {

        // ── Снаряд (8 штук) ───────────────────────────────────────────────
        // 8 железных слитков + 1 порох → 8 снарядов
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.TANK_SHELL.get(), 8)
                .pattern("III")
                .pattern("IGI")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('G', Items.GUNPOWDER)
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(output);

        // ── Ствол орудия ─────────────────────────────────────────────────
        // 3 железных блока в столбик
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TANK_BARREL.get())
                .pattern(" B ")
                .pattern(" B ")
                .pattern(" B ")
                .define('B', Items.IRON_BLOCK)
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(output);

        // ── Бронекорпус ───────────────────────────────────────────────────
        // 8 железных блоков кольцом (как сундук в MC, только жирнее)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TANK_HULL.get())
                .pattern("BBB")
                .pattern("B B")
                .pattern("BBB")
                .define('B', Items.IRON_BLOCK)
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(output);

        // ── Спавнер танка ─────────────────────────────────────────────────
        // 2 ствола + 6 корпусов (башня + корпус + люк)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.TANK_SPAWNER.get())
                .pattern("HBH")
                .pattern("HHH")
                .pattern("HBH")
                .define('H', ModItems.TANK_HULL.get())
                .define('B', ModItems.TANK_BARREL.get())
                .unlockedBy("has_hull", has(ModItems.TANK_HULL.get()))
                .save(output);
    }
}
