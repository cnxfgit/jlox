package com.craftinginterpreters.lox.objects;

/**
 * @author hlx
 * @date 2023-08-17
 */
public class ObjNative implements Obj{

    private final NativeFn function;

    public ObjNative(NativeFn function){
        this.function = function;
    }

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

}
