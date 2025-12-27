package com.allyclient.module.impl;

import com.allyclient.module.TextHudModule;

public class CoordModule extends TextHudModule {
    public CoordModule() {
        super("FPS", 10, 10, true, "x: $(x), y: $(y), z: $(z)", null);
    }

    @Override
    public String getValue(){
        return "";
    }

    @Override
    public String getDisplayText(){
        if(mc.player == null) return "";
        String format = formatSetting.getValue();
        String x = String.format("%.1f", mc.player.getX());
        String y = String.format("%.1f", mc.player.getY());
        String z = String.format("%.1f", mc.player.getZ());
        return format.replace("$(x)", x).replace("$(y)", y).replace("$(z)", z);
    }
}
