package com.allyclient.mixin.client;


import com.allyclient.module.impl.ScriptFeatureModule;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class ClientWorldMixin {
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void onGetTimeOfDay(CallbackInfoReturnable<Long> cir) {
        // ロードされている全てのスクリプト機能を確認
        for (ScriptFeatureModule module : ScriptFeatureModule.LOADED_MODULES) {
            if (module.enabled) {
                Number val = module.getOutputAsNumber("time");

                if (val != null) {
                    cir.setReturnValue(val.longValue());
                    return;
                }
            }
        }
    }
}
