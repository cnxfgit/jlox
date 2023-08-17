package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.Lox;

/**
 * @author hlx
 * @date 2023-08-05
 */
public class ObjString implements Obj {

    private final String string;

    public ObjString(String string){
        this.string = string;
    }

    public static ObjString copyString(String string) {
        return new ObjString(string);
    }

    @Override
    public int hashCode() {
        return string.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof  ObjString)) {
            throw new RuntimeException();
        }
        return string.equals(((ObjString) obj).string);
    }

    @Override
    public ObjType getType() {
        return ObjType.STRING;
    }

    @Override
    public void print() {
        System.out.print(this.string);
    }

    public String getString() {
        return string;
    }
}
