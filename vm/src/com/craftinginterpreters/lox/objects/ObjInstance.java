package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.value.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hlx
 * @date 2023-08-17
 */
public class ObjInstance implements Obj {

    private final ObjClass klass;

    private final Map<ObjString, Value> fields;

    public ObjInstance(ObjClass klass){
        this.klass = klass;
        this.fields = new HashMap<>();
    }

    @Override
    public ObjType getType() {
        return ObjType.INSTANCE;
    }

    @Override
    public void print() {
        System.out.printf("%s instance", this.klass.getName());
    }

    public ObjClass getKlass() {
        return klass;
    }

    public Map<ObjString, Value> getFields() {
        return fields;
    }

}
