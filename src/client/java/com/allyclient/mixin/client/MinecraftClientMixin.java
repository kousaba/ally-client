package com.allyclient.mixin.client;

import com.allyclient.gui.screen.ConfigScreen;
import com.allyclient.module.impl.CpsModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin{
    private boolean isConfigScreenOpenKeyPressed = false;
    private boolean wasLeftButtonPressed = false;
    private boolean wasRightButtonPressed = false;
    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInputEvents(CallbackInfo ci){
        MinecraftClient client = (MinecraftClient)(Object)this;
        if(InputUtil.isKeyPressed(client.getWindow(), GLFW.GLFW_KEY_RIGHT_SHIFT)){
            if(!isConfigScreenOpenKeyPressed){
                if(client.currentScreen == null){
                    client.setScreen(new ConfigScreen());
                }else if(client.currentScreen instanceof ConfigScreen){
                    client.setScreen(null);
                }
            }
            isConfigScreenOpenKeyPressed = true;
        }else{
            isConfigScreenOpenKeyPressed = false;
        }

        boolean isLeftPressed = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS;
        if(isLeftPressed && !wasLeftButtonPressed){
            CpsModule.registerClick(0);
        }
        wasLeftButtonPressed = isLeftPressed;
        boolean isRightPressed = GLFW.glfwGetMouseButton(client.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_2) == GLFW.GLFW_PRESS;
        if(isRightPressed && !wasRightButtonPressed){
            CpsModule.registerClick(1);
        }
        wasRightButtonPressed = isRightPressed;
    }
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci){
        com.allyclient.module.ModuleManager.onTick();
    }
}
