package com.craftinginterpreters.lox.objects;

import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class Upvalue implements ObjectType {

    private List<Long> location;

    private Long closed;

    private Upvalue next;

    public Upvalue() {

    }

    @Override
    public ObjType getType() {
        return ObjType.UPVALUE;
    }
}
