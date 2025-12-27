package com.allyclient.module;

import com.allyclient.settings.Setting;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public String name;
    public boolean enabled;
    public List<Setting> settings = new ArrayList<>();
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public Module(String name, boolean enabled){
        this.name = name;
        this.enabled = enabled;
        if(enabled) onEnable();
    }

    public void toggle(){
        this.enabled = !this.enabled;
        if(this.enabled) onEnable();
        else onDisable();
    }

    protected void addSetting(Setting setting){
        this.settings.add(setting);
    }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
}
