package com.allyclient.module.impl;

import com.allyclient.module.TextHudModule;

public class WatermarkModule extends TextHudModule {

    public WatermarkModule() {
        super("Ally Client", 10, 30, true, "$(client)", "$(client)");
    }

    @Override
    public String getValue() {
        return "Ally Client";
    }
}