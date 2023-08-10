package com.craftinginterpreters.lox.scanner;

import com.craftinginterpreters.lox.compiler.ParseRule;

/**
 * @author hlx
 * @date 2023-08-01
 */
public enum TokenType {
    // 单字符标记
    LEFT_PAREN, RIGHT_PAREN,
    LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS,
    SEMICOLON, SLASH, STAR,
    // 一个或者两个的字符标记
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    // 字面量
    IDENTIFIER, STRING, NUMBER,
    // 关键字
    AND, CLASS, ELSE, FALSE,
    FOR, FUN, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS,
    TRUE, VAR, WHILE,
    // 错误令牌或者结束符
    ERROR, EOF;
}
