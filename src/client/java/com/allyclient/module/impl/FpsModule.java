package com.allyclient.module.impl;

import com.allyclient.module.TextHudModule;

public class FpsModule extends TextHudModule{
    public FpsModule() {
        super("FPS", 10, 10, true, "$(fps) FPS", "$(fps)");
    }
    @Override
    public String getValue(){
        return String.valueOf(mc.getCurrentFps());
    }
}