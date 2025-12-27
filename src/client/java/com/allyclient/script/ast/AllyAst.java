package com.allyclient.script.ast;

import java.util.List;

public class AllyAst {
    public sealed interface Node {}
    public sealed interface Stmt extends Node {}
    public sealed interface Expr extends Node {}

    public record BlockStmt(List<Stmt> statements) implements Stmt {}
    public record IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) implements Stmt {}
    public record AssignStmt(String identifier, Expr value) implements Stmt {}
    public record FuncDefStmt(String name, List<String> params, Stmt body) implements Stmt {}
    public record VarDeclStmt(String name, Expr initializer) implements Stmt {}
    public record FuncCallStmt(String name, List<Expr> arguments) implements Stmt {}
    public record CallExpr(String name, List<Expr> arguments) implements Expr {}
    public record ReturnStmt(Expr value) implements Stmt{}

    public record BinaryExpr(Expr left, String operator, Expr right) implements Expr {}
    public record UnaryExpr(String operator, Expr right) implements Expr {}
    public record LiteralExpr(Object value) implements Expr {}
    public record LocalVarExpr(String name) implements Expr {}
    public record GlobalVarExpr(String name) implements Expr {}
    public record ArrayExpr(List<Expr> elements) implements Expr {}
    public record IndexExpr(Expr target, Expr index) implements Expr {}
    public record StaticDeclStmt(String name, Expr initializer) implements Stmt {}
    public record WhileStmt(Expr condition, Stmt body) implements Stmt {}
    public record ForInStmt(String varName, Expr collection, Stmt body) implements Stmt {}
    public record BreakStmt() implements Stmt {}
    public record ContinueStmt() implements Stmt {}
    public record MapEntry(Expr key, Expr value) implements Node {}
    public record MapExpr(List<MapEntry> entries) implements Expr {}
}
