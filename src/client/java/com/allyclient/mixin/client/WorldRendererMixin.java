package com.allyclient.mixin.client;


import com.allyclient.module.impl.BlockOverlayModule;
import com.allyclient.module.impl.ScriptFeatureModule;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    protected abstract void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, double cameraX, double cameraY, double cameraZ, OutlineRenderState outlineRenderState, int color, float lineWidth);

    @Redirect(
            method = "renderTargetBlockOutline",
            at = @At(
                    value = "INVOKE",
                    // ★修正: 引数の OutlineRenderState のパッケージに /state/ を追加
                    target = "Lnet/minecraft/client/render/WorldRenderer;drawBlockOutline(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;DDDLnet/minecraft/client/render/state/OutlineRenderState;IF)V"
            )
    )
    private void redirectDrawBlockOutline(
            WorldRenderer instance,
            MatrixStack matrices, VertexConsumer vertexConsumer,
            double x, double y, double z,
            OutlineRenderState state,
            int originalColor, float originalWidth
    ) {
        for (ScriptFeatureModule module : ScriptFeatureModule.LOADED_MODULES) {
            if (module.enabled) {
                Object colorObj = module.getOutput("block_overlay_outline_color");
                Number widthObj = module.getOutputAsNumber("block_overlay_line_width");

                if (colorObj != null && widthObj != null) {
                    int customColor = module.getOutputAsColor("block_overlay_outline_color", 0xFFFFFFFF);
                    float customWidth = widthObj.floatValue();
                    this.drawBlockOutline(matrices, vertexConsumer, x, y, z, state, customColor, customWidth);
                    return;
                } else if(colorObj == null && widthObj != null){
                    System.out.println("[Ally Client] ScriptFeatureModule provided null for block_overlay_outline_color");
                } else if(colorObj != null && widthObj == null){
                    System.out.println("[Ally Client] ScriptFeatureModule provided null for block_overlay_line_width");
                }
            }
        }
        if (BlockOverlayModule.INSTANCE == null || !BlockOverlayModule.INSTANCE.enabled) {
            this.drawBlockOutline(matrices, vertexConsumer, x, y, z, state, originalColor, originalWidth);
            return;
        }

        int customColor = BlockOverlayModule.INSTANCE.outlineColor.getValue();
        float customWidth = (float) BlockOverlayModule.INSTANCE.lineWidth.getValue();

        this.drawBlockOutline(matrices, vertexConsumer, x, y, z, state, customColor, customWidth);
    }
}