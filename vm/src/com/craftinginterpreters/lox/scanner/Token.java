package com.craftinginterpreters.lox.scanner;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class Token {

    private TokenType type;

    private int start;

    private int length;

    private int line;

    private String message;

    public Token(TokenType type, int start, int length, int line, String message) {
        this.type = type;
        this.start = start;
        this.length = length;
        this.line = line;
        this.message = message;
    }

    public Token(String message, int line){
        this.type = TokenType.ERROR;
        this.message = message;
        this.line = line;
        this.length = message.length();
    }

}
