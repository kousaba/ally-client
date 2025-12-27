package com.allyclient.settings;

import com.google.gson.JsonElement;

public abstract class Setting {
    public String name;
    public Setting(String name) {
        this.name = name;
    }

    public abstract JsonElement save();
    public abstract void load(JsonElement element);
}
