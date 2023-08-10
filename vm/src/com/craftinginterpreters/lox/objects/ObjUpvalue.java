package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.value.Value;

import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjUpvalue implements Obj {

    private List<Value> location;

    private Value closed;

    private ObjUpvalue next;

    public ObjUpvalue() {

    }

    @Override
    public ObjType getType() {
        return ObjType.UPVALUE;
    }

    @Override
    public void print() {
        System.out.print("upvalue");
    }
}
