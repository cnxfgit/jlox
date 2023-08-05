package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.chunk.Chunk;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjFunction implements ObjectType {

    private int arity;
    private int upvalueCount;
    private Chunk chunk;
    public ObjString name;

    @Override
    public ObjType getType() {
        return ObjType.FUNCTION;
    }

    public ObjFunction() {
        this.arity = 0;
        this.upvalueCount = 0;
        this.name = null;
        this.chunk = new Chunk();
    }


}
