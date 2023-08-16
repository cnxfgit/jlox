package com.craftinginterpreters.lox;

/**
 * @author hlx
 * @date 2023-08-16
 */
class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}
