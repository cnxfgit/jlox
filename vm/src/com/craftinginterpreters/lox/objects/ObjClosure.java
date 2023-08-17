package com.craftinginterpreters.lox.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjClosure implements Obj {

    public final ObjFunction function;

    public List<ObjUpvalue> upvalues;

    public int upvalueCount;

    public ObjClosure(ObjFunction function) {
        this.function = function;
        this.upvalueCount = function.upvalueCount;
        this.upvalues = new ArrayList<>();
        for (int i = 0; i < upvalueCount; i++) {
            this.upvalues.add(new ObjUpvalue());
        }
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
