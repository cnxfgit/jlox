package com.craftinginterpreters.lox.objects;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjClosure implements Obj {

    private final ObjFunction function;

    private List<ObjUpvalue> upvalues;

    private int upvalueCount;

    public ObjClosure(ObjFunction function) {
        this.function = function;
        this.upvalueCount = function.getUpvalueCount();
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

    public ObjFunction getFunction() {
        return function;
    }

    public List<ObjUpvalue> getUpvalues() {
        return upvalues;
    }

    public void setUpvalues(List<ObjUpvalue> upvalues) {
        this.upvalues = upvalues;
    }

    public int getUpvalueCount() {
        return upvalueCount;
    }

    public void setUpvalueCount(int upvalueCount) {
        this.upvalueCount = upvalueCount;
    }
}
