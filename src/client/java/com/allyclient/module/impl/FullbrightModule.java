package com.allyclient.module.impl;

import com.allyclient.module.FeatureModule;

public class FullbrightModule extends FeatureModule {
    public static boolean isActive = false;

    public FullbrightModule() {
        super("Fullbright", false);
    }

    @Override
    public void onEnable() { isActive = true; }

    @Override
    public void onDisable() { isActive = false; }
}