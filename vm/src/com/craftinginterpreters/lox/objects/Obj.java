package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.value.Value;
import com.craftinginterpreters.lox.value.ValueType;

/**
 * @author hlx
 * @date 2023-07-27
 */
public interface Obj {

    ObjType getType();

    void print();

    default Value toValue() {
        return new Value(ValueType.OBJ, this);
    }
}
