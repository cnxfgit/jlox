package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.chunk.Chunk;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjFunction implements Obj {

    public int arity;
    public int upvalueCount;
    public Chunk chunk;
    public ObjString name;

    @Override
    public ObjType getType() {
        return ObjType.FUNCTION;
    }

    @Override
    public void print() {
        if (this.name == null) {
            System.out.print("<script>");
            return;
        }
        System.out.printf("<fn %s>", this.name.getString());
    }

    public ObjFunction() {
        this.arity = 0;
        this.upvalueCount = 0;
        this.name = null;
        this.chunk = new Chunk();
    }


}
