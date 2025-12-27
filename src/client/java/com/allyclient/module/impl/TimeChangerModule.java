package com.allyclient.module.impl;

import com.allyclient.module.FeatureModule;
import com.allyclient.settings.impl.NumberSetting;

public class TimeChangerModule extends FeatureModule {
    public static long customTime = 6000;
    public static boolean isActive = false;

    private final NumberSetting timeSetting;

    public TimeChangerModule(){
        super("TimeChanger", false);

        this.timeSetting = new NumberSetting("Time", 6000, 0, 24000);
        this.addSetting(timeSetting);
    }

    @Override
    public void onEnable(){isActive = true;}

    @Override
    public void onDisable(){isActive = false;}

    @Override
    public void onTick(){
        customTime = (long) this.timeSetting.getValue();
    }
}
