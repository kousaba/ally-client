package com.allyclient.module.impl;

import com.allyclient.module.FeatureModule;
import com.allyclient.settings.impl.ColorSetting;
import com.allyclient.settings.impl.NumberSetting;
import net.minecraft.world.gen.feature.Feature;

public class BlockOverlayModule extends FeatureModule {
    public static BlockOverlayModule INSTANCE;
    public final ColorSetting outlineColor;
    public final NumberSetting lineWidth;

    public BlockOverlayModule(){
        super("BlockOverlay", false);
        INSTANCE = this;

        this.outlineColor = new ColorSetting("OutlineColor", 0xFFFFFFFF);
        this.lineWidth = new NumberSetting("Width", 4.0, 1.0, 10.0);

        this.addSetting(outlineColor);
        this.addSetting(lineWidth);
    }

    public float getRed() { return ((outlineColor.getValue() >> 16) & 0xFF) / 255.0f; }
    public float getGreen() { return ((outlineColor.getValue() >> 8) & 0xFF) / 255.0f; }
    public float getBlue() { return (outlineColor.getValue() & 0xFF) / 255.0f; }
    public float getAlpha() { return ((outlineColor.getValue() >> 24) & 0xFF) / 255.0f; }
}
