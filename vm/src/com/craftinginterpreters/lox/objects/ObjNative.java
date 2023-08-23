package com.craftinginterpreters.lox.objects;

/**
 * @author hlx
 * @date 2023-08-17
 */
public class ObjNative implements Obj{

    private NativeFn function;

    @Override
    public ObjType getType() {
        return ObjType.NATIVE;
    }

    @Override
    public void print() {
        System.out.print("<native fn>");
    }

    public NativeFn getFunction() {
        return function;
    }

    public void setFunction(NativeFn function) {
        this.function = function;
    }
}
