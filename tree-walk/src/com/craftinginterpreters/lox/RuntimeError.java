package com.craftinginterpreters.lox;

/**
 * @author hlx
 * @date 2023-08-07
 */
class RuntimeError extends RuntimeException {

    final Token token;

    public RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }

}
