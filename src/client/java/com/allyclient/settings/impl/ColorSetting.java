package com.allyclient.settings.impl;

import com.allyclient.settings.Setting;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class ColorSetting extends Setting {
    private int color; // ARGB
    private boolean chroma; // 虹色モードか
    public ColorSetting(String name, int defaultColor){
        super(name);
        this.color = defaultColor;
        this.chroma = false;
    }
    public int getValue(){
        return color;
    }
    public void setValue(int color){
        this.color = color;
    }
    public int getDisplayColor(){
        return chroma ? calculateChroma() : color;
    }
    private int calculateChroma(){
        float hue = (System.currentTimeMillis() % 5000) / 500f;
        return java.awt.Color.HSBtoRGB(hue, 1.0f, 1.0f);
    }

    @Override
    public JsonElement save(){
        return new JsonPrimitive(this.color);
    }
    @Override
    public void load(JsonElement element){
        if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()){
            this.color = element.getAsInt();
        }
    }
}
