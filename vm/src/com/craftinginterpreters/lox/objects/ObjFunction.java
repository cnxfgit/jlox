package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.chunk.Chunk;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjFunction implements Obj {

    private int arity;
    private int upvalueCount;
    private final Chunk chunk;
    private ObjString name;

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

    public int getArity() {
        return arity;
    }

    public void setArity(int arity) {
        this.arity = arity;
    }

    public int getUpvalueCount() {
        return upvalueCount;
    }

    public void setUpvalueCount(int upvalueCount) {
        this.upvalueCount = upvalueCount;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public ObjString getName() {
        return name;
    }

    public void setName(ObjString name) {
        this.name = name;
    }
}
