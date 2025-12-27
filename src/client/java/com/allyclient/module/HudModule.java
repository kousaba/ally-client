package com.allyclient.module;

import net.minecraft.client.gui.DrawContext;

public abstract class HudModule extends Module{
    public int x,y,width,height;
    public HudModule(String name, int x, int y, boolean enabled){
        super(name, enabled);
        this.x = x;
        this.y = y;
    }

    public abstract void render(DrawContext context);

    public boolean isHovered(double mouseX, double mouseY){
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
