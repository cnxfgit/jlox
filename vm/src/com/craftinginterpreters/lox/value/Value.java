package com.craftinginterpreters.lox.value;

import com.craftinginterpreters.lox.objects.Obj;

/**
 * @author hlx
 * @date 2023-08-05
 */
public class Value {

    private final ValueType type;

    private boolean bool;

    private Double number;

    public Obj obj;

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

    public void print() {
        switch (this.type) {
            case BOOL:
                System.out.printf(this.bool ? "true" : "false");
                break;
            case NIL:
                System.out.print("nil");
                break;
            case NUMBER:
                System.out.printf("%g", this.number);
                break;
            case OBJ:
                this.obj.print();
                break;
        }
    }

}
