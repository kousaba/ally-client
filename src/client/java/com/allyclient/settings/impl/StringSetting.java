package com.allyclient.settings.impl;

import com.allyclient.settings.Setting;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public class StringSetting extends Setting {
    private String value;
    public StringSetting(String name, String defaultValue){
        super(name);
        value = defaultValue;
    }
    public String getValue(){
        return value;
    }
    public void setValue(String value){
        this.value = value;
    }

    @Override
    public JsonElement save(){
        return new JsonPrimitive(this.value);
    }

    @Override
    public void load(JsonElement element){
        if(element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()){
            this.value = element.getAsString();
        }
    }
}
