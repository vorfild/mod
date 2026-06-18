package com.warfield.tankmod;

import com.warfield.tankmod.entity.TankEntity;
import com.warfield.tankmod.entity.TankShellEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TankMod.MODID);

    /**
     * Тип сущности «Пантера».
     * sized(2.5, 1.5) — хитбокс: 2.5 блока × 2.5 блока (ширина/глубина) × 1.5 высота.
     * Визуальная модель GeckoLib больше хитбокса (пушка выступает за пределы).
     */
    public static final DeferredHolder<EntityType<?>, EntityType<TankEntity>> TANK =
            ENTITIES.register("tank", () ->
                    EntityType.Builder.<TankEntity>of(TankEntity::new, MobCategory.MISC)
                            .sized(2.5f, 1.5f)
                            .setTrackingRange(80)
                            .build()
            );

    /** Снаряд 75mm KwK 42. Маленький хитбокс, высокая скорость, живёт ≤200 тиков. */
    public static final DeferredHolder<EntityType<?>, EntityType<TankShellEntity>> TANK_SHELL_ENTITY =
            ENTITIES.register("tank_shell_entity", () ->
                    EntityType.Builder.<TankShellEntity>of(TankShellEntity::new, MobCategory.MISC)
                            .sized(0.3f, 0.3f)
                            .setTrackingRange(80)
                            .build()
            );
}
