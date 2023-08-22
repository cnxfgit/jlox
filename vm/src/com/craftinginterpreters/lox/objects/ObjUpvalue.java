package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.value.Value;

import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjUpvalue implements Obj {

    public int location;

    public Value closed;

    public ObjUpvalue next;

    public ObjUpvalue(int location) {
        this.closed = new Value();
        this.location = location;
        this.next = null;
    }

    public ObjUpvalue(){

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
