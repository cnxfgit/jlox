package com.craftinginterpreters.lox.compiler;

/**
 * @author hlx
 * @date 2023-08-08
 */
public class ClassCompiler {

    public static ClassCompiler currentClass;
    private ClassCompiler enclosing;
    private boolean hasSuperclass;

    public ClassCompiler(){
        hasSuperclass = false;
        enclosing = ClassCompiler.currentClass;
        ClassCompiler.currentClass = this;
    }

    public boolean isHasSuperclass() {
        return hasSuperclass;
    }

    public ClassCompiler getEnclosing() {
        return enclosing;
    }

    public void setHasSuperclass(boolean hasSuperclass) {
        this.hasSuperclass = hasSuperclass;
    }

    public void setEnclosing(ClassCompiler enclosing) {
        this.enclosing = enclosing;
    }
}
