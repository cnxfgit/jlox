package com.craftinginterpreters.lox.objects;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class Function implements ObjectType{

    @Override
    public ObjTypeEnum getType() {
        return ObjTypeEnum.FUNCTION;
    }

    public Function(){

    }


}
