package com.craftinginterpreters.lox.objects;

/**
 * @author hlx
 * @date 2023-08-17
 */
public class ObjNative implements Obj{

    public NativeFn function;

    @Override
    public ObjType getType() {
        return ObjType.NATIVE;
    }

    @Override
    public void print() {
        System.out.print("<native fn>");
    }
}
