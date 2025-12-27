package com.allyclient.module.impl;

import com.allyclient.module.TextHudModule;
import com.allyclient.script.*;
import com.allyclient.script.ast.AllyAst;
import com.allyclient.script.runtime.AllyInterpreter;
import com.allyclient.script.runtime.GlobalVariableManager;
import com.allyclient.settings.impl.CodeSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptModule extends TextHudModule {

    private final CodeSetting codeSetting;
    private String lastCode = "";
    private List<AllyAst.Stmt> cachedAst = null;
    private String errorMessage = null;

    // 実行状態のキャッシュ
    private int cachedColor = 0xFFFFFFFF;
    private String cachedText = "Script Initializing...";

    // ★重要: インタプリタとモジュール変数を保持する
    private AllyInterpreter interpreter;
    private final Map<String, Object> moduleVariables = new HashMap<>();

    public ScriptModule(String id, int defaultX, int defaultY) {
        super(id, defaultX, defaultY, true, "", null);

        String defaultScript = """
                if $fps >= 60 {
                    text = "FPS: Good " + int($fps);
                    color = #00FF00;
                } else {
                    text = "FPS: Low " + int($fps);
                    color = #FF0000;
                }
                """.trim();

        this.codeSetting = new CodeSetting("Code", defaultScript);
        this.addSetting(codeSetting);
    }

    @Override
    public String getValue() { return ""; }

    @Override
    public String getDisplayText() { return cachedText; }

    private void updateScriptExecution() {
        String currentCode = codeSetting.getValue().replace("\r", "").replace("\t", " ").trim();

        // 1. 再コンパイルチェック
        if (!currentCode.equals(lastCode)) {
            lastCode = currentCode;
            try {
                this.cachedAst = ScriptCompiler.compile(currentCode);
                this.interpreter = new AllyInterpreter(moduleVariables);
                this.interpreter.loadDefinitions(this.cachedAst);
            } catch (Exception e) {
                this.cachedAst = null;
                this.errorMessage = "Syntax Error: " + e.getMessage();
                this.cachedText = this.errorMessage;
                this.cachedColor = 0xFFFF0000;
                return;
            }
        }

        if (this.cachedAst == null || this.interpreter == null) return;

        try {
            // 2. 実行 (GlobalVariableManagerの値が自動的に参照される)
            Map<String, Object> results = interpreter.execute(this.cachedAst);

            // 3. 結果反映
            Object textObj = results.get("text");
            if (textObj != null) {
                // interpolateStringは、Globals変数の置換のためまだ必要
                this.cachedText = interpolateString(textObj.toString(), GlobalVariableManager.getGlobals());
            } else {
                this.cachedText = "";
            }

            Object colorObj = results.get("color");
            if (colorObj != null) {
                // 文字列か数値か判別して色に変換 (InterpreterのasColorロジックと同様のことをする)
                this.cachedColor = parseColor(colorObj);
            } else {
                this.cachedColor = 0xFFFFFFFF;
            }

        } catch (Exception e) {
            this.cachedText = "Runtime Error: " + e.getMessage();
            this.cachedColor = 0xFFFF0000;
        }
    }

    @Override
    public void render(DrawContext context) {
        updateScriptExecution();
        this.width = mc.textRenderer.getWidth(cachedText);
        this.height = mc.textRenderer.fontHeight;

        if (shadowSetting.isEnabled()) {
            context.drawTextWithShadow(mc.textRenderer, cachedText, this.x, this.y, this.cachedColor);
        } else {
            context.drawText(mc.textRenderer, cachedText, this.x, this.y, this.cachedColor, true);
        }
    }

    private String interpolateString(String text, Map<String, Object> globals) {
        Pattern pattern = Pattern.compile("\\$\\(([a-zA-Z0-9_]+)\\)");
        Matcher matcher = pattern.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = globals.get(varName);
            if (value instanceof java.util.function.Supplier<?> s) value = s.get();

            String replacement = (value != null) ? value.toString() : "??";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private int parseColor(Object o) {
        if (o instanceof Number n) return n.intValue();
        if (o instanceof String s) {
            try {
                if (s.startsWith("#")) {
                    String hex = s.substring(1);
                    long val = Long.parseLong(hex, 16);
                    if (hex.length() <= 6) val |= 0xFF000000L;
                    return (int) val;
                }
                return Integer.parseInt(s);
            } catch (Exception e) { return 0xFFFFFFFF; }
        }
        return 0xFFFFFFFF;
    }
}