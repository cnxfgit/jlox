package com.craftinginterpreters.lox.objects;

import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjClosure implements Obj {

    private final ObjFunction function;

    private List<ObjUpvalue> upvalues;

    public ObjClosure(ObjFunction function) {
        this.function = function;
    }

    @Override
    public ObjType getType() {
        return ObjType.CLOSURE;
    }

    @Override
    public void print() {
        this.function.print();
    }

}
