package com.craftinginterpreters.lox.objects;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class Function implements ObjectType{

    @Override
    public ObjType getType() {
        return ObjType.FUNCTION;
    }

    public Function(){

    }


}
