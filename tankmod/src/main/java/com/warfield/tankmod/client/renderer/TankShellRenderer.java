package com.warfield.tankmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.warfield.tankmod.ModItems;
import com.warfield.tankmod.entity.TankShellEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Рендерит снаряд как маленький предмет tank_shell (как выброшенный предмет). */
public class TankShellRenderer extends EntityRenderer<TankShellEntity> {

    private final ItemRenderer itemRenderer;

    public TankShellRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.itemRenderer = ctx.getItemRenderer();
    }

    @Override
    public void render(TankShellEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYRot() - 90f));
        poseStack.scale(0.6f, 0.6f, 0.6f);

        this.itemRenderer.renderStatic(
                new ItemStack(ModItems.TANK_SHELL.get()),
                ItemDisplayContext.GROUND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                null,
                entity.getId()
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(TankShellEntity entity) {
        return ResourceLocation.fromNamespaceAndPath("tankmod", "textures/item/tank_shell.png");
    }
}
