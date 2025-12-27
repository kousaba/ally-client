package com.allyclient.mixin.client;


import com.allyclient.module.impl.ScriptFeatureModule;
import com.allyclient.module.impl.ZoomModule;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void onGetFov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        // ZoomModuleの値を使ってFOVを計算
        // currentZoomLevelが 1.0 なら変化なし、0.5 なら半分になる
        float originalFov = cir.getReturnValue();
        cir.setReturnValue(originalFov * ZoomModule.currentZoomLevel);
        for (ScriptFeatureModule module : ScriptFeatureModule.LOADED_MODULES) {
            if (module.enabled) {
                Number val = module.getOutputAsNumber("zooming");
                Number val2 = module.getOutputAsNumber("zooming_fov_multiplier");
                if (val != null && val2 != null) {
                    cir.setReturnValue(originalFov * val2.floatValue());
                    return;
                } else if(val == null && val2 != null){
                    System.out.println("[Ally Client] ScriptFeatureModule provided null for zooming");
                } else if(val != null && val2 == null){
                    System.out.println("[Ally Client] ScriptFeatureModule provided null for zooming_fov_multiplier");
                }
            }
        }
    }
}
