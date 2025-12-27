package com.allyclient.module.impl;

import com.allyclient.module.TextHudModule;

import java.util.ArrayList;
import java.util.List;

public class CpsModule extends TextHudModule {
    public static final List<Long> leftClicks = new ArrayList<>();
    public static final List<Long> rightClicks = new ArrayList<>();
    public CpsModule() {
        super("CPS", 10, 30, true, "CPS: $(l) | $(r)", null);
    }

    public static void registerClick(int button){
        if(button == 0){
            leftClicks.add(System.currentTimeMillis());
        }else if(button == 1){
            rightClicks.add(System.currentTimeMillis());
        }
    }
    public static int getCps(List<Long> clicks){
        long currentTime = System.currentTimeMillis();
        clicks.removeIf(time -> currentTime - time > 1000);
        return clicks.size();
    }
    @Override
    public String getValue(){
        return "";
    }
    @Override
    public String getDisplayText(){
        String format = formatSetting.getValue();
        int leftCps = getCps(leftClicks);
        int rightCps = getCps(rightClicks);
        return format.replace("$(l)", String.valueOf(leftCps)).replace("$(r)", String.valueOf(rightCps));
    }
}
