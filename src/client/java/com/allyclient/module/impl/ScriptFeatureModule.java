package com.allyclient.module.impl;

import com.allyclient.module.FeatureModule;
import com.allyclient.script.*;
import com.allyclient.script.ast.AllyAst;
import com.allyclient.script.runtime.AllyInterpreter;
import com.allyclient.settings.impl.CodeSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScriptFeatureModule extends FeatureModule {

    public static final List<ScriptFeatureModule> LOADED_MODULES = new ArrayList<>();

    private final CodeSetting codeSetting;
    private String lastCode = "";

    // ★重要: インタプリタを保持し続ける
    private AllyInterpreter interpreter;
    private List<AllyAst.Stmt> cachedAst;
    private final Map<String, Object> moduleVariables = new HashMap<>(); // let変数の保存場所

    private boolean hasSyntaxError = false;

    // ★変数の値を外部から取得するためのキャッシュ
    // 例: "time" -> 1000
    private final Map<String, Object> currentOutputs = new HashMap<>();

    public ScriptFeatureModule(String name, String defaultScript) {
        super(name, false);
        this.codeSetting = new CodeSetting("Code", defaultScript);
        this.addSetting(codeSetting);
        LOADED_MODULES.add(this);
    }

    private void updateExecution() {
        String currentCode = codeSetting.getValue().replace("\r", "").replace("\t", " ").trim();

        // 1. コンパイル (変更があった時のみ)
        if (!currentCode.equals(lastCode)) {
            lastCode = currentCode;
            try {
                this.cachedAst = ScriptCompiler.compile(currentCode);
                this.interpreter = new AllyInterpreter(moduleVariables);
                this.interpreter.loadDefinitions(this.cachedAst);

                hasSyntaxError = false;
            } catch (Exception e) {
                hasSyntaxError = true;
                System.out.println("Script Feature Error (" + name + "): " + e.getMessage());
            }
        }

        // 2. 実行 (毎ティック)
        if (this.enabled && this.interpreter != null && this.cachedAst != null && !hasSyntaxError) {
            try {
                // スクリプト全体を実行
                Map<String, Object> results = this.interpreter.execute(this.cachedAst);

                // 結果を保存 (Mixinから参照するため)
                this.currentOutputs.clear();
                this.currentOutputs.putAll(results);

            } catch (Exception e) {
                // 実行時エラーはログに出す程度にする
            }
        }
    }

    // ★毎ティック呼び出してスクリプトを回す
    @Override
    public void onTick() {
        updateExecution();
    }

    // ★Mixinから値を読み取るためのメソッド
    public Object getOutput(String variableName) {
        // 1. まず outputs (単なる代入) を見る
        if (currentOutputs.containsKey(variableName)) {
            return currentOutputs.get(variableName);
        }
        // 2. なければ moduleVariables (let変数) を見る
        if (moduleVariables.containsKey(variableName)) {
            return moduleVariables.get(variableName);
        }
        return null;
    }

    // 数値として安全に取得するヘルパー
    public Number getOutputAsNumber(String variableName) {
        Object val = currentOutputs.get(variableName);
        return (val instanceof Number n) ? n : null;
    }

    // 真偽値として安全に取得するヘルパー
    public boolean getOutputAsBoolean(String variableName) {
        Object val = currentOutputs.get(variableName);
        if (val instanceof Boolean b) return b;
        if (val instanceof Number n) return n.doubleValue() != 0;
        return false;
    }

    // 色(int)として安全に取得するヘルパー
    public int getOutputAsColor(String variableName, int defaultColor){
        Object val = currentOutputs.get(variableName);
        if(val instanceof Number n) return n.intValue();
        if(val instanceof String s){
            try{
                if(s.startsWith("#")){
                    String hex = s.substring(1);
                    long colorVal = Long.parseLong(hex, 16);
                    if(hex.length() <= 6) colorVal |= 0xFF000000L;
                    return (int)colorVal;
                }
                return Integer.parseInt(s);
            } catch(Exception ignored){
                return defaultColor;
            }
        }
        return defaultColor;
    }
}