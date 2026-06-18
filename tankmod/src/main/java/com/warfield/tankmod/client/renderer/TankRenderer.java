package com.warfield.tankmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.warfield.tankmod.entity.TankEntity;
import com.warfield.tankmod.model.TankModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class TankRenderer extends GeoEntityRenderer<TankEntity> {

    public TankRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new TankModel());
        this.shadowRadius = 2.4f;
    }

    @Override
    public void render(TankEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.scale(2.0f, 2.0f, 2.0f);
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
}
