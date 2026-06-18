package com.warfield.tankmod.model;

import com.warfield.tankmod.entity.TankEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.animation.AnimationState;

/**
 * GeckoLib-модель танка «Пантера».
 *
 * setCustomAnimations() вызывается каждый кадр рендера (только клиент).
 * Здесь в Фазе 2 будет применяться поворот башни.
 * В Фазе N — анимация гусениц по скорости.
 */
public class TankModel extends GeoModel<TankEntity> {

    @Override
    public ResourceLocation getModelResource(TankEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("tankmod", "geo/panther.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(TankEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("tankmod", "textures/entity/panther.png");
    }

    @Override
    public ResourceLocation getAnimationResource(TankEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("tankmod", "animations/panther.animation.json");
    }

    /**
     * Поворот башни.
     * Вызывается каждый кадр — не делаем тяжёлых вычислений, только setRotY.
     *
     * Фаза 2: здесь будет:
     *   GeoBone turret = getAnimationProcessor().getBone("turret");
     *   if (turret != null) turret.setRotY((float) Math.toRadians(entity.getTurretYaw()));
     */
    @Override
    public void setCustomAnimations(TankEntity entity, long instanceId,
                                    AnimationState<TankEntity> animationState) {
        // Фаза 1: пока пусто. Поворот башни подключается в Фазе 2.
    }
}
