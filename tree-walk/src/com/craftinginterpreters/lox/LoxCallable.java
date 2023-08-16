package com.craftinginterpreters.lox;

import java.util.List;

/**
 * @author hlx
 * @date 2023-08-16
 */
public interface LoxCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);

}
