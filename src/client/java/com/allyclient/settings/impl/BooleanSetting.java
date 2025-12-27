package com.allyclient.settings.impl;

import com.allyclient.settings.Setting;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class BooleanSetting extends Setting {
    private boolean enabled;
    public BooleanSetting(String name, boolean defaultValue){
        super(name);
        this.enabled = defaultValue;
    }
    public boolean isEnabled(){
        return enabled;
    }
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }
    public void toggle(){
        this.enabled = !this.enabled;
    }

    @Override
    public JsonElement save(){
        return new JsonPrimitive(this.enabled);
    }

    @Override
    public void load(JsonElement element){
        if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()){
            this.enabled = element.getAsBoolean();
        }
    }
}
