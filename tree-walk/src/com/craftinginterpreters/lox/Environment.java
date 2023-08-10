package com.craftinginterpreters.lox;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hlx
 * @date 2023-08-10
 */
class Environment {

    private final Map<String, Object> values = new HashMap<>();

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }
        throw new RuntimeError(name,
                "Undefined variable '" + name.lexeme + "'.");
    }

    void define(String name, Object value) {
        values.put(name, value);
    }


}
