package com.allyclient.module;

import com.allyclient.settings.impl.BooleanSetting;
import com.allyclient.settings.impl.ColorSetting;
import com.allyclient.settings.impl.StringSetting;
import net.minecraft.client.gui.DrawContext;

public abstract class TextHudModule extends HudModule{
    protected ColorSetting colorSetting;
    protected BooleanSetting shadowSetting;
    protected StringSetting formatSetting;
    protected String placeHolder;
    public TextHudModule(String name, int x, int y, boolean enabled, String defaultFormat, String placeholder){
        super(name, x, y, enabled);
        this.colorSetting = new ColorSetting("Color", 0xFFFFFFFF);
        this.shadowSetting = new BooleanSetting("Shadow", true);
        this.formatSetting = new StringSetting("Format", defaultFormat);
        this.placeHolder = placeholder;
        this.addSetting(colorSetting);
        this.addSetting(shadowSetting);
        this.addSetting(formatSetting);
    }

    // 値のみを返す(fpsなら"64"などを返す)
    public abstract String getValue();

    // 表示用のテキストを返す(fpsなら"FPS: 64"などを返す(設定による))
    // Posのように、$(x)と$(y)などがある場合はOverrideで対応する
    public String getDisplayText(){
        String format = this.formatSetting.getValue();
        String value = getValue();
        return format.replace(this.placeHolder, value);
    }

    // ScriptModuleのみがOverrideする想定
    @Override
    public void render(DrawContext context){
        String text = getDisplayText();
        int color = colorSetting.getDisplayColor();
        this.width = mc.textRenderer.getWidth(text);
        this.height = mc.textRenderer.fontHeight;
        if(shadowSetting.isEnabled()){
            context.drawTextWithShadow(mc.textRenderer, text, this.x, this.y, color);
        }else{
            context.drawText(mc.textRenderer, text, this.x, this.y, color, true);
        }
    }
}
