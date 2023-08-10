package com.craftinginterpreters.lox.compiler;

/**
 * @author hlx
 * @date 2023-08-09
 */
@FunctionalInterface
public interface ParseFn {

    void parseFn(boolean canAssign);

}
