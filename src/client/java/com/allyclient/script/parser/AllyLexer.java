package com.allyclient.script.parser;

import java.util.ArrayList;
import java.util.List;

public class AllyLexer {
    public enum TokenType{
        FUNC, LET, RETURN, LPAREN, RPAREN, COMMA,
        IF, ELSE, IDENTIFIER, VARIABLE, STRING, NUMBER, COLOR,
        LBRACE, RBRACE, ASSIGN, GT, LT, GTE, LTE, EQ, SEMICOLON, WHITESPACE, NEWLINE, EOF,
        PLUS, MINUS, STAR, SLASH, PERCENT, AND, OR, NOT, LBRACKET, RBRACKET, STATIC,
        WHILE, FOR, IN, BREAK, CONTINUE, COLON
    };

    public record Token(TokenType type, String value){}

    private final String src;
    private int pos = 0;

    public AllyLexer(String src) {this.src = src;}

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (pos < src.length()) {
            char current = peek();
            if (Character.isWhitespace(current)) {
                if(current == '\n' || current == '\r'){
                    tokens.add(lexNewline());
                } else {
                    tokens.add(lexWhiteSpace());
                }
            } else if (Character.isLetter(current)) {
                tokens.add(lexIdentifierOrKeyword());
            } else if (current == '$') {
                tokens.add(lexVariable());
            } else if (current == '"') {
                tokens.add(lexString());
            } else if (current == '#') {
                tokens.add(lexColor());
            } else if (Character.isDigit(current)) {
                tokens.add(lexNumber());
            } else {
                tokens.add(lexSymbol());
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }

    private char peek() {return pos < src.length() ? src.charAt(pos) : '\0';}
    private char next() {return pos < src.length() ? src.charAt(pos++) : '\0';}

    private Token lexNewline(){
        char c = next();
        if(c == '\r' && peek() == '\n'){
            next();
        }
        return new Token(TokenType.NEWLINE, "\n");
    }

    private Token lexWhiteSpace(){
        StringBuilder sb = new StringBuilder();
        while(pos < src.length() && Character.isWhitespace(peek()) && peek() != '\n' && peek() != '\r'){
            sb.append(next());
        }
        return new Token(TokenType.WHITESPACE, sb.toString());
    }

    private Token lexIdentifierOrKeyword() {
        StringBuilder sb = new StringBuilder();
        while (Character.isLetterOrDigit(peek()) || peek() == '_') sb.append(next());
        String text = sb.toString();
        return switch (text) {
            case "if" -> new Token(TokenType.IF, text);
            case "else" -> new Token(TokenType.ELSE, text);
            case "func" -> new Token(TokenType.FUNC, text);
            case "let" -> new Token(TokenType.LET, text);
            case "return" -> new Token(TokenType.RETURN, text);
            case "static" -> new Token(TokenType.STATIC, text);
            case "while" -> new Token(TokenType.WHILE, text);
            case "for" -> new Token(TokenType.FOR, text);
            case "in" -> new Token(TokenType.IN, text);
            case "break" -> new Token(TokenType.BREAK, text);
            case "continue" -> new Token(TokenType.CONTINUE, text);
            default -> new Token(TokenType.IDENTIFIER, text);
        };
    }

    private Token lexVariable() {
        StringBuilder sb = new StringBuilder();
        sb.append(next()); // '$' を保存
        while (Character.isLetterOrDigit(peek()) || peek() == '_') sb.append(next());
        return new Token(TokenType.VARIABLE, sb.toString());
    }

    private Token lexString() {
        StringBuilder sb = new StringBuilder();
        sb.append(next()); // 開始の '"' を保存
        while (peek() != '"' && peek() != '\0') sb.append(next());
        if (peek() == '"') sb.append(next()); // 終了の '"' を保存
        return new Token(TokenType.STRING, sb.toString());
    }

    private Token lexNumber() {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(peek()) || peek() == '.') sb.append(next());
        return new Token(TokenType.NUMBER, sb.toString());
    }

    private Token lexColor() { // #RRGGBB
        StringBuilder sb = new StringBuilder();
        sb.append(next()); // #
        while (Character.isLetterOrDigit(peek())) sb.append(next());
        return new Token(TokenType.COLOR, sb.toString());
    }

    private Token lexSymbol() {
        char c = next();
        return switch (c) {
            case '{' -> new Token(TokenType.LBRACE, "{");
            case '}' -> new Token(TokenType.RBRACE, "}");
            case ';' -> new Token(TokenType.SEMICOLON, ";");
            case '(' -> new Token(TokenType.LPAREN, "(");
            case ')' -> new Token(TokenType.RPAREN, ")");
            case ',' -> new Token(TokenType.COMMA, ",");
            case '+' -> new Token(TokenType.PLUS, "+");
            case '-' -> new Token(TokenType.MINUS, "-");
            case '*' -> new Token(TokenType.STAR, "*");
            case '/' -> new Token(TokenType.SLASH, "/");
            case '%' -> new Token(TokenType.PERCENT, "%");
            case ':' -> new Token(TokenType.COLON, ":");
            case '&' -> {
                if(peek() == '&'){
                    next();
                    yield new Token(TokenType.AND, "&&");
                }
                throw new RuntimeException("Unknown char: " + c);
            }
            case '|' -> {
                if(peek() == '|'){
                    next();
                    yield new Token(TokenType.OR, "||");
                }
                throw new RuntimeException("Unknown char: " + c);
            }
            case '!' -> {
                if(peek() == '='){
                    next();
                    // TODO: !=
                }
                yield new Token(TokenType.NOT, "!");
            }
            case '[' -> new Token(TokenType.LBRACKET, "[");
            case ']' -> new Token(TokenType.RBRACKET, "]");

            // == または =
            case '=' -> {
                if (peek() == '=') {
                    next(); // 次の '=' を消費
                    yield new Token(TokenType.EQ, "==");
                }
                yield new Token(TokenType.ASSIGN, "=");
            }

            // >= または >
            case '>' -> {
                if (peek() == '=') {
                    next(); // 次の '=' を消費
                    yield new Token(TokenType.GTE, ">=");
                }
                yield new Token(TokenType.GT, ">");
            }

            // <= または <
            case '<' -> {
                if (peek() == '=') {
                    next(); // 次の '=' を消費
                    yield new Token(TokenType.LTE, "<=");
                }
                yield new Token(TokenType.LT, "<");
            }

            default -> throw new RuntimeException("Unknown char: " + c);
        };
    }
}
