package com.craftinginterpreters.lox.value;

import com.craftinginterpreters.lox.objects.*;

/**
 * @author hlx
 * @date 2023-08-05
 */
public class Value {

    private final ValueType type;

    private boolean bool;

    private Double number;

    private Obj obj;

    public Obj getObj() {
        return obj;
    }

    public void setObj(Obj obj) {
        this.obj = obj;
    }

    public Value(ValueType type, Obj obj) {
        this.type = type;
        this.obj = obj;
    }

    public Value(ValueType type, double number) {
        this.type = type;
        this.number = number;
    }

    public Value(ValueType type, boolean bool) {
        this.type = type;
        this.bool = bool;
    }

    public Value() {
        this.type = ValueType.NIL;
    }

    public void print() {
        switch (this.type) {
            case BOOL:
                System.out.printf(this.bool ? "true" : "false");
                break;
            case NIL:
                System.out.print("nil");
                break;
            case NUMBER:
                System.out.print(this.number);
                break;
            case OBJ:
                this.obj.print();
                break;
        }
    }

    public boolean isNumber() {
        return this.type == ValueType.NUMBER;
    }

    public boolean isBool() {
        return this.type == ValueType.BOOL;
    }

    public boolean asBool() {
        return this.bool;
    }

    public boolean isInstance() {
        return this.type == ValueType.OBJ && this.obj.getType() == ObjType.INSTANCE;
    }

    public ObjInstance asInstance() {
        return (ObjInstance) this.obj;
    }

    public boolean isClosure() {
        return this.type == ValueType.OBJ && this.obj.getType() == ObjType.CLOSURE;
    }

    public ObjClosure asClosure() {
        return (ObjClosure) this.obj;
    }

    public boolean isClass() {
        return this.type == ValueType.OBJ && this.obj.getType() == ObjType.CLASS;
    }

    public ObjClass asClass() {
        return (ObjClass) this.obj;
    }

    public boolean isString() {
        return this.type == ValueType.OBJ && this.obj.getType() == ObjType.STRING;
    }

    public ObjString asString() {
        return (ObjString) this.obj;
    }

    public double asNumber() {
        return this.number;
    }

    public boolean isNil() {
        return this.type == ValueType.NIL;
    }

    public boolean isObj() {
        return this.type == ValueType.OBJ;
    }

    public ObjBoundMethod asBoundMethod() {
        return (ObjBoundMethod) this.obj;
    }

    public ObjNative asNative() {
        return (ObjNative) this.obj;
    }

    public ObjFunction asFunction(){
        return  (ObjFunction) this.obj;
    }

    public boolean equals(Value value) {
        if (this.type != value.type) return false;
        switch (this.type) {
            case BOOL:
                return this.asBool() == value.asBool();
            case NIL:
                return true;
            case NUMBER:
                return this.asNumber() == value.asNumber();
            case OBJ: {
                return this.obj == value.obj;
            }
            default:
                return false; // Unreachable.
        }
    }
}
