package com.craftinginterpreters.lox.compiler;

/**
 * @author hlx
 * @date 2023-08-08
 */
public class ClassCompiler {

    public static ClassCompiler currentClass;

    public ClassCompiler enclosing;
    public boolean hasSuperclass;

    public ClassCompiler(){

    }
}
