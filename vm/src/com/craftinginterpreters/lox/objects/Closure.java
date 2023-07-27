package com.craftinginterpreters.lox.objects;

import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class Closure implements ObjectType {

    private Function function;

    private List<Upvalue> upvalues;

    public Closure(Function function) {
        this.function = function;
    }

    @Override
    public ObjTypeEnum getType() {
        return ObjTypeEnum.CLOSURE;
    }

}
