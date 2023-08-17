package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.value.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class ObjClass implements Obj {

    public ObjString name;

    public Map<ObjString, Value> methods;

    public ObjClass(ObjString name) {
        this.name = name;
        methods = new HashMap<>();
    }

    @Override
    public ObjType getType() {
        return ObjType.CLASS;
    }

    @Override
    public void print() {
        System.out.printf("%s", this.name.getString());
    }
}
