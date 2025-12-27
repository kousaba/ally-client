package com.allyclient.module.impl;

import com.allyclient.module.HudModule;
import com.allyclient.script.ScriptCompiler;
import com.allyclient.script.ast.AllyAst;
import com.allyclient.script.lib.ScriptRenderContext;
import com.allyclient.script.runtime.AllyInterpreter;
import com.allyclient.settings.impl.CodeSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptCanvasModule extends HudModule {
    private final CodeSetting codeSetting;
    private String lastCode = "";
    private AllyInterpreter interpreter;
    private List<AllyAst.Stmt> cachedAst;
    private final Map<String, Object> moduleVariables = new HashMap<>();
    private String errorMessage = null;

    public ScriptCanvasModule(String name, int x, int y, String defaultScript){
        super(name, x, y, true);
        this.width = 100;
        this.height = 100;
        this.codeSetting = new CodeSetting("Code", defaultScript);
        this.addSetting(codeSetting);
    }

    // コードが変わったときだけリコンパイル
    private void compileIfNeeded(){
        String currentCode = codeSetting.getValue().replace("\r", "").replace("\t", " ").trim();
        if (!currentCode.equals(lastCode)) {
            lastCode = currentCode;
            try {
                this.cachedAst = ScriptCompiler.compile(currentCode);
                moduleVariables.clear();
                this.interpreter = new AllyInterpreter(moduleVariables);
                this.interpreter.loadDefinitions(this.cachedAst);
                this.errorMessage = null;
            } catch (Exception e) {
                this.errorMessage = e.getMessage();
            }
        }
    }

    @Override
    public void render(DrawContext context){
        compileIfNeeded();
        if(errorMessage != null){
            context.drawText(mc.textRenderer, "Error: " + errorMessage, this.x, this.y, 0xFFFF0000, false);
            return;
        }
        if(this.interpreter != null && this.cachedAst != null){
            try{
                ScriptRenderContext.begin(context, this.x, this.y);
                Map<String, Object> results = this.interpreter.execute(this.cachedAst);
                if(results.containsKey("width")) this.width = ((Number) results.get("width")).intValue();
                if(results.containsKey("height")) this.height = ((Number) results.get("height")).intValue();
            } catch(Exception e){
                context.drawText(mc.textRenderer, "Runtime Error: " + e.getMessage(), this.x, this.y, 0xFFFF0000, false);
            } finally {
                ScriptRenderContext.end();
            }
        }
    }
}
