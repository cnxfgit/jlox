package com.craftinginterpreters.lox.compiler;

import com.craftinginterpreters.lox.compiler.ParseFn;
import com.craftinginterpreters.lox.compiler.Precedence;

/**
 * @author hlx
 * @date 2023-08-09
 */
public class ParseRule {

    private final ParseFn prefix;         // 前缀
    private final ParseFn infix;          // 中缀
    private final Precedence precedence;  // 优先级

    public ParseRule(ParseFn prefix, ParseFn infix, Precedence precedence) {
        this.prefix = prefix;
        this.infix = infix;
        this.precedence = precedence;
    }

    public ParseFn getPrefix() {
        return prefix;
    }

    public ParseFn getInfix() {
        return infix;
    }

    public Precedence getPrecedence() {
        return precedence;
    }
}
