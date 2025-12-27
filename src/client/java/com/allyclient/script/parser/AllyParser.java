package com.allyclient.script.parser;

import com.allyclient.script.ast.AllyAst;

import java.util.List;
import java.util.ArrayList;
import static com.allyclient.script.parser.AllyLexer.TokenType.*;

public class AllyParser {
    private final List<AllyLexer.Token> tokens;
    private int current = 0;

    public AllyParser(List<AllyLexer.Token> tokens) {this.tokens = tokens;}

    public List<AllyAst.Stmt> parse() {
        List<AllyAst.Stmt> statements = new ArrayList<>();
        while(!isAtEnd()){
            statements.add(statement());
        }
        return statements;
    }

    private AllyAst.Stmt statement(){
        skipWhitespace();
        if(match(FUNC)) return functionDefinition();
        if(match(LET)) return variableDeclaration();
        if(match(IF)) return ifStatement();
        if(match(LBRACE)) return new AllyAst.BlockStmt(block());
        if(match(RETURN)) return returnStmt();
        if(match(STATIC)) return staticDeclaration();
        if(match(WHILE)) return whileStatement();
        if(match(FOR)) return forStatement();
        if(match(BREAK)) return breakStatement();
        if(match(CONTINUE)) return continueStatement();
        if (!tokens.isEmpty() && tokens.get(current).type() == IDENTIFIER) {
            // 次が ( なら関数呼び出しの可能性
            if (tokens.get(current + 1).type() == LPAREN) {
                return functionCallStatement();
            }
        }
        return assignmentStatement();
    }

    private AllyAst.Stmt whileStatement(){
        AllyAst.Expr condition = expression();
        consume(LBRACE, "Expect '{' after while condition.");
        AllyAst.Stmt body = new AllyAst.BlockStmt(block()); // ブロックとして扱う
        return new AllyAst.WhileStmt(condition, body);
    }

    private AllyAst.Stmt forStatement(){
        AllyLexer.Token varName = consume(IDENTIFIER, "Expect iterator variable name.");
        consume(IN, "Expect 'in' after variable.");
        AllyAst.Expr collection = expression();
        consume(LBRACE, "Expect '{' after for-in statement.");
        AllyAst.Stmt body = new AllyAst.BlockStmt(block()); // ブロックとして扱う
        return new AllyAst.ForInStmt(varName.value(), collection, body);
    }

    private AllyAst.Stmt breakStatement(){
        consume(SEMICOLON, "Expect ';' after break.");
        return new AllyAst.BreakStmt();
    }

    private AllyAst.Stmt continueStatement(){
        consume(SEMICOLON, "Expect ';' after continue.");
        return new AllyAst.ContinueStmt();
    }

    private AllyAst.Stmt staticDeclaration(){
        AllyLexer.Token name = consume(IDENTIFIER, "Expect static variable name.");
        consume(ASSIGN, "Expect '=' after static variable name.");
        AllyAst.Expr initializer = expression();
        consume(SEMICOLON, "Expect ';' after static variable declaration.");
        return new AllyAst.StaticDeclStmt(name.value(), initializer);
    }

    private AllyAst.Stmt returnStmt(){
        AllyAst.Expr value = null;
        if(!check(SEMICOLON)){
            value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return value.");
        return new AllyAst.ReturnStmt(value);
    }

    private AllyAst.Stmt functionDefinition(){
        AllyLexer.Token name = consume(IDENTIFIER, "Expect function name.");
        consume(LPAREN, "Expect '(' after function name.");
        List<String> params = new ArrayList<>();
        if(!check(RPAREN)){
            do{
                AllyLexer.Token param = consume(IDENTIFIER, "Expect parameter name.");
                params.add(param.value());
            }while(match(COMMA));
        }
        consume(RPAREN, "Expect ')' after parameters.");
        AllyAst.Stmt body = statement();
        return new AllyAst.FuncDefStmt(name.value(), params, body);
    }

    private AllyAst.Stmt variableDeclaration(){
        AllyLexer.Token name = consume(IDENTIFIER, "Expect variable name.");
        consume(ASSIGN, "Expect '=' after variable name.");
        AllyAst.Expr initializer = expression();
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new AllyAst.VarDeclStmt(name.value(), initializer);
    }

    private AllyAst.Stmt functionCallStatement(){
        AllyLexer.Token name = consume(IDENTIFIER, "Expect function name.");
        consume(LPAREN, "Expect '(' after function name.");
        List<AllyAst.Expr> arguments = new ArrayList<>();
        if(!check(RPAREN)){
            do{
                arguments.add(expression());
            }while(match(COMMA));
        }
        consume(RPAREN, "Expect ')' after arguments.");
        consume(SEMICOLON, "Expect ';' after function call.");
        return new AllyAst.FuncCallStmt(name.value(), arguments);
    }

    private AllyAst.Stmt ifStatement(){
        AllyAst.Expr condition = expression();
        AllyAst.Stmt thenBranch = statement();
        AllyAst.Stmt elseBranch = null;
        if(match(ELSE)){
            elseBranch = statement();
        }
        return new AllyAst.IfStmt(condition, thenBranch, elseBranch);
    }

    private List<AllyAst.Stmt> block(){
        List<AllyAst.Stmt> statements = new ArrayList<>();
        while(!check(RBRACE) && !isAtEnd()){
            statements.add(statement());
        }
        consume(RBRACE, "Expect '}' after block.");
        return statements;
    }

    private AllyAst.Stmt assignmentStatement() {
        AllyLexer.Token name = consume(IDENTIFIER, "Expect identifier.");
        consume(ASSIGN, "Expect '=' after identifier.");
        AllyAst.Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new AllyAst.AssignStmt(name.value(), value);
    }

    private AllyAst.Expr expression() {
        return logicOr();
    }

    // 2. 論理和 (||)
    private AllyAst.Expr logicOr() {
        AllyAst.Expr expr = logicAnd();
        while (match(OR)) {
            String operator = previous().value();
            AllyAst.Expr right = logicAnd();
            expr = new AllyAst.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    // 3. 論理積 (&&)
    private AllyAst.Expr logicAnd() {
        AllyAst.Expr expr = equality();
        while (match(AND)) {
            String operator = previous().value();
            AllyAst.Expr right = equality();
            expr = new AllyAst.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    // 4. 等価判定 (==, !=)
    private AllyAst.Expr equality() {
        AllyAst.Expr expr = comparison();
        while (match(EQ)) { // != もここ
            String operator = previous().value();
            AllyAst.Expr right = comparison();
            expr = new AllyAst.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    // 5. 大小比較 (>, <)
    private AllyAst.Expr comparison() {
        AllyAst.Expr expr = term();
        while (match(GT, GTE, LT, LTE)) {
            String operator = previous().value();
            AllyAst.Expr right = term();
            expr = new AllyAst.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    // 6. 足し算・引き算 (+, -)
    private AllyAst.Expr term() {
        AllyAst.Expr expr = factor();
        while (match(PLUS, MINUS)) {
            String operator = previous().value();
            AllyAst.Expr right = factor();
            expr = new AllyAst.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    // 7. 掛け算・割り算 (*, /, %)
    private AllyAst.Expr factor() {
        AllyAst.Expr expr = unary(); // ★ここが変わりました (primary -> unary)
        while (match(STAR, SLASH, PERCENT)) {
            String operator = previous().value();
            AllyAst.Expr right = unary(); // ★ここも unary
            expr = new AllyAst.BinaryExpr(expr, operator, right);
        }
        return expr;
    }

    // 8. ★追加: 単項演算 (!, -)
    private AllyAst.Expr unary() {
        if (match(NOT, MINUS)) {
            String operator = previous().value();
            AllyAst.Expr right = unary(); // 再帰的に呼ぶ (!!true 対応)
            return new AllyAst.UnaryExpr(operator, right);
        }
        return primary(); // 記号がなければ primary へ
    }

    private AllyAst.Expr primary() {
        if(match(LPAREN)){
            AllyAst.Expr expr = expression();
            consume(RPAREN, "Expect ')' after expression.");
            return expr;
        }
        if (match(NUMBER)) return new AllyAst.LiteralExpr(Double.parseDouble(previous().value()));
        if (match(STRING)) {
            String raw = previous().value();
            // "abc" -> abc (長さが2以上なら中身だけ取り出す)
            String content = raw.length() >= 2 ? raw.substring(1, raw.length() - 1) : "";
            return new AllyAst.LiteralExpr(content);
        }

        // 変数処理: 先頭の $ を削除する (GlobalVarExprが名前だけを欲しがる場合)
        if (match(VARIABLE)) {
            String raw = previous().value();
            // $fps -> fps
            String name = raw.startsWith("$") ? raw.substring(1) : raw;
            return new AllyAst.GlobalVarExpr(name);
        }
        if (match(COLOR)) return new AllyAst.LiteralExpr(previous().value()); // Hex Stringとして扱う
        if (match(IDENTIFIER)){
            String name = previous().value();
            AllyAst.Expr expr;

            if (match(LPAREN)) {
                System.out.println("Making CallExpr for function: " + name);
                List<AllyAst.Expr> args = new ArrayList<>();
                if (!check(RPAREN)) {
                    do {
                        args.add(expression());
                    } while (match(COMMA));
                }
                consume(RPAREN, "Expect ')' after arguments.");
                System.out.println("Arguments : " + args);
                expr = new AllyAst.CallExpr(name, args);
            } else{
                expr = new AllyAst.LocalVarExpr(name);
            }

            while(match(LBRACKET)){
                AllyAst.Expr index = expression();
                consume(RBRACKET, "Expect ']' after index.");
                expr = new AllyAst.IndexExpr(expr, index);
            }

            // '(' がなければただの変数参照
            return expr;
        }
        if (match(LBRACKET)){
            List<AllyAst.Expr> elements = new ArrayList<>();
            if(!check(RBRACKET)){
                do{
                    elements.add(expression());
                }while(match(COMMA));
            }
            consume(RBRACKET, "Expect ']' after array elements.");
            return new AllyAst.ArrayExpr(elements);
        }
        if(match(LBRACE)){
            List<AllyAst.MapEntry> entries = new ArrayList<>();
            if(!check(RBRACE)){
                do{
                    AllyAst.Expr key = expression();
                    consume(COLON, "Expect ':' between key and value in map.");
                    AllyAst.Expr value = expression();
                    entries.add(new AllyAst.MapEntry(key, value));
                } while(match(COMMA));
                consume(RBRACE, "Expect '}' after map entries.");
                return new AllyAst.MapExpr(entries);
            }
        }
        throw new RuntimeException("Expect expression.");
    }

    private boolean match(AllyLexer.TokenType... types) {
        skipWhitespace();
        for (AllyLexer.TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    private AllyLexer.Token consume(AllyLexer.TokenType type, String msg) {
        skipWhitespace();
        if (check(type)) return advance();
        throw new RuntimeException(msg);
    }
    private boolean check(AllyLexer.TokenType type) {
        skipWhitespace();
        if (isAtEnd()) return false;
        return peek().type() == type;
    }
    private AllyLexer.Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    private void skipWhitespace(){
        while(!isAtEnd()){
            AllyLexer.TokenType type = peek().type();
            if(type == WHITESPACE || type == AllyLexer.TokenType.NEWLINE){
                current++;
            }else{
                break;
            }
        }
    }
    private boolean isAtEnd() { return peek().type() == EOF; }
    private AllyLexer.Token peek() { return tokens.get(current); }
    private AllyLexer.Token previous() { return tokens.get(current - 1); }
}
