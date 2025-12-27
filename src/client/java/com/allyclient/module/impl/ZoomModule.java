package com.allyclient.module.impl;

import com.allyclient.module.FeatureModule;
import com.allyclient.settings.impl.NumberSetting;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class ZoomModule extends FeatureModule {
    public static float currentZoomLevel = 1.0f;

    // 設定を追加 (初期値0.1, 最小0.01, 最大1)
    private final NumberSetting smoothSetting;

    public ZoomModule() {
        super("Zoom", true);
        this.smoothSetting = new NumberSetting("Smooth", 0.1, 0.01, 1.0);
        this.addSetting(smoothSetting);
    }

    @Override
    public void onTick() {
        boolean isPressed = InputUtil.isKeyPressed(mc.getWindow(), GLFW.GLFW_KEY_C);
        float target = isPressed ? 0.23f : 1.0f;

        // ★設定値を使う
        // floatにキャストが必要
        float smooth = (float) this.smoothSetting.getValue();

        currentZoomLevel += (target - currentZoomLevel) * smooth;
    }
}
