package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.Lox;

/**
 * @author hlx
 * @date 2023-08-05
 */
public class ObjString {

    private String string;

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
}
