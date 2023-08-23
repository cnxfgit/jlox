package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.value.Value;

import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjUpvalue implements Obj {

    private int location;

    private Value closed;

    private ObjUpvalue next;

    public ObjUpvalue(int location) {
        this.closed = new Value();
        this.location = location;
        this.next = null;
    }

    public ObjUpvalue(){}

    @Override
    public ObjType getType() {
        return ObjType.UPVALUE;
    }

    @Override
    public void print() {
        System.out.print("upvalue");
    }

    public int getLocation() {
        return location;
    }

    public void setLocation(int location) {
        this.location = location;
    }

    public Value getClosed() {
        return closed;
    }

    public void setClosed(Value closed) {
        this.closed = closed;
    }

    public ObjUpvalue getNext() {
        return next;
    }

    public void setNext(ObjUpvalue next) {
        this.next = next;
    }
}
