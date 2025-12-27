package com.allyclient.script;

import com.allyclient.script.ast.AllyAst;
import com.allyclient.script.parser.AllyLexer;
import com.allyclient.script.parser.AllyParser;

import java.util.List;

public class ScriptCompiler {
    public static List<AllyAst.Stmt> compile(String source) throws RuntimeException{
         String cleanSource = source.replace("\r", "").replace("\t", " ").trim();
         AllyLexer lexer = new AllyLexer(cleanSource);
         List<AllyLexer.Token> tokens = lexer.tokenize();
         AllyParser parser = new AllyParser(tokens);
         return parser.parse();
    }
}
