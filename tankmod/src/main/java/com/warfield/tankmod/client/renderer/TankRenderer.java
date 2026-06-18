package com.warfield.tankmod.client.renderer;

import com.warfield.tankmod.entity.TankEntity;
import com.warfield.tankmod.model.TankModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Рендер танка через GeckoLib.
 * GeoEntityRenderer берёт на себя всю работу: загружает модель, текстуру,
 * кэширует их, применяет кости и трансформации.
 * Кэш модели встроен в GeckoLib — не пересоздаём объект каждый кадр.
 */
public class TankRenderer extends GeoEntityRenderer<TankEntity> {

    public TankRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TankModel());
        // shadowRadius — тень под танком (~половина ширины)
        this.shadowRadius = 1.2f;
    }
}
