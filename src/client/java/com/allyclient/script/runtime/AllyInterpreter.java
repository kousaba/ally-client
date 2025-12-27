package com.allyclient.script.runtime;

import com.allyclient.script.ast.AllyAst;
import com.allyclient.script.lib.AllyLibrary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AllyInterpreter {
    // 実行結果 (text, color) を保持するマップ (HUD用)
    private final Map<String, Object> outputs = new HashMap<>();

    // 関数定義を保持するマップ
    private final Map<String, AllyAst.FuncDefStmt> functions = new HashMap<>();

    // モジュール固有の永続変数 (letで定義したもの)
    private final Map<String, Object> scriptVariables;

    private int loopCounter = 0;
    private static final int MAX_LOOP_CYCLES = 1000;

    // コンストラクタ: モジュールごとの永続変数を渡す
    public AllyInterpreter(Map<String, Object> scriptVariables) {
        this.scriptVariables = scriptVariables;
    }

    // --- コンパイル後の初期化 (関数とトップレベル変数のロード) ---
    public void loadDefinitions(List<AllyAst.Stmt> statements) {
        functions.clear();
        for (AllyAst.Stmt stmt : statements) {
            if (stmt instanceof AllyAst.FuncDefStmt func) {
                functions.put(func.name(), func);
            } else if (stmt instanceof AllyAst.VarDeclStmt var) {
                // トップレベルの変数を初期化して保存
                Object val = evaluate(var.initializer(), scriptVariables);
                scriptVariables.put(var.name(), val);
            }
        }
    }

    // --- メイン実行 (HUD用: 上から下まで実行) ---
    public Map<String, Object> execute(List<AllyAst.Stmt> statements) {
        outputs.clear();
        loopCounter = 0;

        // 実行中の変数を保持する一時スコープ
        Map<String, Object> frameScope = new HashMap<>();

        for (AllyAst.Stmt stmt : statements) {
            if (!(stmt instanceof AllyAst.FuncDefStmt)) {
                // frameScope を渡して、行をまたいで変数を共有できるようにする
                executeStmt(stmt, frameScope);
            }
        }
        return outputs;
    }

    // --- イベント呼び出し (Feature用: 特定の関数を実行) ---
    public Object callFunction(String funcName, Map<String, Object> eventArgs) {
        if (!functions.containsKey(funcName)) return null;
//        System.out.println("Calling function: " + funcName + " with args: " + eventArgs + " For Feature");

        AllyAst.FuncDefStmt funcDef = functions.get(funcName);

        // 引数などを入れるスコープ
        Map<String, Object> funcScope = new HashMap<>();
        if (eventArgs != null) {
            funcScope.putAll(eventArgs);
        }

        try {
            executeStmt(funcDef.body(), funcScope);
        } catch (Return r) {
            return r.value;
        }
        return null;
    }

    // --- ステートメント実行 ---
    private void executeStmt(AllyAst.Stmt stmt, Map<String, Object> scope) {
//        System.out.println("Executing stmt: " + stmt);
        if (stmt instanceof AllyAst.BlockStmt block) {
            for (AllyAst.Stmt s : block.statements()) executeStmt(s, scope);
        } else if (stmt instanceof AllyAst.IfStmt ifStmt) {
            if (evaluateCondition(ifStmt.condition(), scope)) {
                executeStmt(ifStmt.thenBranch(), scope);
            } else if (ifStmt.elseBranch() != null) {
                executeStmt(ifStmt.elseBranch(), scope);
            }
        } else if (stmt instanceof AllyAst.AssignStmt assign) {
            Object val = evaluate(assign.value(), scope);
            // 1. ローカル変数(引数含む)にあれば更新
            if (scope.containsKey(assign.identifier())) {
                scope.put(assign.identifier(), val);
            }
            // 2. モジュール変数(let)にあれば更新
            else if (scriptVariables.containsKey(assign.identifier())) {
                scriptVariables.put(assign.identifier(), val);
            }
            // 3. どちらにもなければ outputs (HUDの表示用) に入れる
            else {
                outputs.put(assign.identifier(), val);
            }
        } else if (stmt instanceof AllyAst.VarDeclStmt varDecl) {
            Object val = evaluate(varDecl.initializer(), scope);

            // ★修正: 保存先の優先順位
            // 1. もし永続変数(ロード時に定義されたもの)なら、そちらを更新 (Featureモジュール用)
            if (scriptVariables.containsKey(varDecl.name())) {
                scriptVariables.put(varDecl.name(), val);
            }
            // 2. そうでなければ、現在のスコープ(このフレーム限り)に入れる (Canvasモジュールのアニメーション用)
            else {
                scope.put(varDecl.name(), val);
            }
        } else if (stmt instanceof AllyAst.FuncCallStmt call) {
            // 文としての関数呼び出し (戻り値無視)
            evaluateCallExpr(new AllyAst.CallExpr(call.name(), call.arguments()), scope);
        } else if (stmt instanceof AllyAst.ReturnStmt returnStmt) {
            Object value = null;
            if (returnStmt.value() != null) {
                value = evaluate(returnStmt.value(), scope);
            }
            throw new Return(value);
        } else if (stmt instanceof AllyAst.StaticDeclStmt staticDecl){
            String name = staticDecl.name();
            if(!scriptVariables.containsKey(name)){
                Object val = evaluate(staticDecl.initializer(), scope);
                scriptVariables.put(name, val);
            }
        } else if (stmt instanceof AllyAst.BreakStmt){
            throw new Break();
        } else if (stmt instanceof AllyAst.ContinueStmt){
            throw new Continue();
        } else if (stmt instanceof AllyAst.WhileStmt whileStmt){
            while(isTruthy(evaluate(whileStmt.condition(), scope))){
                if(++loopCounter > MAX_LOOP_CYCLES){
                    throw new RuntimeException("Infinite loop detected! (max cycles: " + MAX_LOOP_CYCLES + ")");
                }
                try{
                    executeStmt(whileStmt.body(), scope);
                } catch (Break b){
                    break;
                } catch (Continue c){
                    continue;
                }
            }
        } else if (stmt instanceof AllyAst.ForInStmt forStmt){
            Object collection = evaluate(forStmt.collection(), scope);
            if (collection instanceof List<?> list){
                for (Object item : list){
                    if(++loopCounter > MAX_LOOP_CYCLES){
                        throw new RuntimeException("Infinite loop detected! (max cycles: " + MAX_LOOP_CYCLES + ")");
                    }
                    Map<String, Object> forScope = new HashMap<>(scope);
                    forScope.put(forStmt.varName(), item);
                    try{
                        executeStmt(forStmt.body(), forScope);
                    } catch (Break b){
                        break;
                    } catch (Continue c) {
                        continue;
                    }
                }
            }
        }
    }

    // --- 式の評価 ---
    private Object evaluate(AllyAst.Expr expr, Map<String, Object> scope) {
//        System.out.println("Evaluating expr: " + expr);
        if (expr instanceof AllyAst.LiteralExpr lit) {
            return lit.value();

        } else if (expr instanceof AllyAst.LocalVarExpr localVar) {
            // ★ 普通の変数 (count, arg など)
            // 1. 現在のスコープ (引数やローカル変数)
            if (scope.containsKey(localVar.name())) {
                return scope.get(localVar.name());
            }
            // 2. モジュール変数 (letで定義したもの)
            if (scriptVariables.containsKey(localVar.name())) {
                return scriptVariables.get(localVar.name());
            }
            // 3. 出力変数(width, heightなど)
            if (outputs.containsKey(localVar.name())) {
                return outputs.get(localVar.name());
            }
            return 0.0; // 未定義

        } else if (expr instanceof AllyAst.UnaryExpr unary) {
            Object right = evaluate(unary.right(), scope);
            return switch (unary.operator()) {
                case "!" -> !isTruthy(right);
                case "-" -> -asDouble(right);
                default -> null;
            };

        } else if (expr instanceof AllyAst.BinaryExpr bin) {
            String op = bin.operator();
            if (op.equals("&&")) {
                return isTruthy(evaluate(bin.left(), scope)) && isTruthy(evaluate(bin.right(), scope));
            }
            if (op.equals("||")) {
                return isTruthy(evaluate(bin.left(), scope)) || isTruthy(evaluate(bin.right(), scope));
            }

            Object left = evaluate(bin.left(), scope);
            Object right = evaluate(bin.right(), scope);

            return switch (op) {
                case "+" -> {
                    if (left instanceof String || right instanceof String) {
                        yield String.valueOf(left) + String.valueOf(right);
                    }
                    yield asDouble(left) + asDouble(right);
                }
                case "-" -> asDouble(left) - asDouble(right);
                case "*" -> asDouble(left) * asDouble(right);
                case "/" -> {
                    double r = asDouble(right);
                    yield (r == 0) ? 0.0 : asDouble(left) / r;
                }
                case "%" -> asDouble(left) % asDouble(right);
                case ">" -> asDouble(left) > asDouble(right);
                case ">=" -> asDouble(left) >= asDouble(right);
                case "<" -> asDouble(left) < asDouble(right);
                case "<=" -> asDouble(left) <= asDouble(right);
                case "==" -> {
                    if (left instanceof String || right instanceof String) yield String.valueOf(left).equals(String.valueOf(right));
                    yield asDouble(left) == asDouble(right);
                }
                case "!=" -> {
                    if (left instanceof String || right instanceof String) yield !String.valueOf(left).equals(String.valueOf(right));
                    yield asDouble(left) != asDouble(right);
                }
                default -> 0.0;
            };

        } else if (expr instanceof AllyAst.CallExpr call) {
//            System.out.println("Calling: " + call.name() + " with args: " + call.arguments() + " in evaluate");
            return evaluateCallExpr(call, scope);
        } else if (expr instanceof AllyAst.GlobalVarExpr var){
            Object val = GlobalVariableManager.getGlobals().get(var.name());
            if(val instanceof java.util.function.Supplier<?> supplier){
                return supplier.get();
            }
            return val != null ? val : "null";
        } else if (expr instanceof AllyAst.ArrayExpr arrayExpr){
            List<Object> list = new ArrayList<>();
            for(AllyAst.Expr e : arrayExpr.elements()){
                list.add(evaluate(e, scope));
            }
            return list;
        } else if (expr instanceof AllyAst.IndexExpr indexExpr){
            Object target = evaluate(indexExpr.target(), scope);
            Object indexObj = evaluate(indexExpr.index(), scope);

            if (target instanceof List<?> list && indexObj instanceof Number n) {
                int idx = n.intValue();
                if (idx >= 0 && idx < list.size()) {
                    return list.get(idx);
                }
                throw new RuntimeException("Index out of bounds: " + idx);
            }
            if (target instanceof Map<?, ?> map){
                String key = String.valueOf(indexObj);
                return map.getOrDefault(key, null);
            }
            throw new RuntimeException("Invalid array access");
        } else if (expr instanceof AllyAst.MapExpr mapExpr){
            Map<String, Object> map = new HashMap<>();
            for(AllyAst.MapEntry entry : mapExpr.entries()){
                Object key = evaluate(entry.key(), scope);
                Object val = evaluate(entry.value(), scope);
                map.put(String.valueOf(key), val);
            }
            return map;
        }
        return null;
    }

    // 関数呼び出し評価 (組み込み + ユーザー定義)
    private Object evaluateCallExpr(AllyAst.CallExpr call, Map<String, Object> scope) {
        String name = call.name();
        List<Object> args = call.arguments().stream().map(arg -> evaluate(arg, scope)).toList();
//        System.out.println("Calling function: " + name + " with args: " + args);
        if (AllyLibrary.has(name)){
//            System.out.println("Found in AllyLibrary: " + name);
            return AllyLibrary.get(name).call(args);
        }

        // --- ユーザー定義関数 ---
        if (functions.containsKey(name)) {
            return callUserFunction(name, call.arguments(), scope);
        }
        return 0.0;
    }

    private Object callUserFunction(String name, List<AllyAst.Expr> arguments, Map<String, Object> callerScope) {
        AllyAst.FuncDefStmt funcDef = functions.get(name);
        Map<String, Object> funcScope = new HashMap<>(); // 新しいスコープ

        for (int i = 0; i < funcDef.params().size(); i++) {
            String paramName = funcDef.params().get(i);
            Object argValue = evaluate(arguments.get(i), callerScope);
            funcScope.put(paramName, argValue);
        }
        try {
            executeStmt(funcDef.body(), funcScope);
        } catch (Return r) {
            return r.value;
        }
        return null;
    }

    private boolean isTruthy(Object obj) {
        if (obj == null) return false;
        if (obj instanceof Boolean b) return b;
        if (obj instanceof Number n) return n.doubleValue() != 0;
        return true;
    }

    private double asDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        return 0.0;
    }

    private int asColor(Object o) {
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

    private boolean evaluateCondition(AllyAst.Expr expr, Map<String, Object> scope) {
        return isTruthy(evaluate(expr, scope));
    }
}