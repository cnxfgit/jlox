package com.craftinginterpreters.lox.compiler;

/**
 * @author hlx
 * @date 2023-08-05
 */
public class Upvalue {

    private byte index;

    private boolean isLocal;

    public Upvalue() {}

    public byte getIndex() {
        return index;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public void setIndex(byte index) {
        this.index = index;
    }

    public void setLocal(boolean local) {
        isLocal = local;
    }
}
