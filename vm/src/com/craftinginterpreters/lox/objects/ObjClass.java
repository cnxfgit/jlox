package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.value.Value;

import java.util.Map;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjClass implements Obj {

    private ObjString name;

    private Map<ObjString, Value> methods;

    @Override
    public ObjType getType() {
        return ObjType.CLASS;
    }

    @Override
    public void print() {
        System.out.printf("%s", this.name.getString());
    }
}
