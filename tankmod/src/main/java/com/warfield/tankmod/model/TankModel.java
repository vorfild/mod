package com.warfield.tankmod.model;

import com.warfield.tankmod.entity.TankEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
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
     * Вызывается каждый кадр на стороне клиента.
     * Здесь поворачиваем кость «turret» в соответствии с DATA_TURRET_YAW.
     * gun_barrel и hatch — дочерние кости turret, поворачиваются автоматически.
     */
    @Override
    public void setCustomAnimations(TankEntity entity, long instanceId,
                                    AnimationState<TankEntity> animationState) {
        // ── Поворот башни ─────────────────────────────────────────────────
        GeoBone turret = getAnimationProcessor().getBone("turret");
        if (turret != null) {
            float worldTurretYaw = entity.getTurretYaw();
            float tankBodyYaw    = entity.getYRot();
            float relativeYaw    = worldTurretYaw - tankBodyYaw;
            // MC yRot CW, GeckoLib rotY CCW → инверсия знака обязательна
            turret.setRotY((float) -Math.toRadians(relativeYaw));
        }

        // ── Люк: открыт / закрыт ─────────────────────────────────────────
        GeoBone hatch = getAnimationProcessor().getBone("hatch");
        if (hatch != null) {
            // Открыт → крышка откидывается на -90° по X (назад относительно турели)
            hatch.setRotX(entity.isHatchOpen() ? (float) Math.toRadians(-90) : 0f);
        }
    }
}
