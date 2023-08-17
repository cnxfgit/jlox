package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.value.Value;

/**
 * @author hlx
 * @date 2023-08-17
 */
public class ObjBoundMethod implements Obj {

    public Value receiver;

    public ObjClosure method;

    public ObjBoundMethod(Value receiver, ObjClosure method) {
        this.receiver = receiver;
        this.method = method;
    }

    @Override
    public ObjType getType() {
        return ObjType.BOUND_METHOD;
    }

    @Override
    public void print() {
        this.method.function.print();
    }
}
