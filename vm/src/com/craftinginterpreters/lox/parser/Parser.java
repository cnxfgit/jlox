package com.craftinginterpreters.lox.parser;

import com.craftinginterpreters.lox.scanner.Token;

/**
 * @author hlx
 * @date 2023-08-05
 */
public class Parser {
    public Token current;
    public Token previous;
    public boolean hadError;
    public boolean panicMode;

    public Parser() {

    }
}
