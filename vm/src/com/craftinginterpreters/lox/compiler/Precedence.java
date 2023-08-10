package com.craftinginterpreters.lox.compiler;

/**
 * @author hlx
 * @date 2023-08-09
 */
public enum Precedence {
    NONE,
    ASSIGNMENT,  // =
    OR,          // or
    AND,         // and
    EQUALITY,    // == !=
    COMPARISON,  // < > <= >=
    TERM,        // + -
    FACTOR,      // * /
    UNARY,       // ! -
    CALL,        // . ()
    PRIMARY;

    public Precedence offset(int num) {
        int current = this.ordinal();
        Precedence[] precedences = values();
        for (int i = 0; i < precedences.length; i++) {
            if (num+current == i) {
                return precedences[i];
            }
        }
        throw new RuntimeException("Precedence exception");
    }
}
