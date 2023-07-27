package com.craftinginterpreters.lox.objects;

/**
 * @author hlx
 * @date 2023-07-27
 */
public enum ObjTypeEnum {
    BOUND_METHOD,   // 绑定方法对象
    CLASS,          // 类对象
    CLOSURE,        // 闭包对象
    FUNCTION,       // 函数对象
    INSTANCE,       // 实例对象
    NATIVE,         // 原生函数对象
    STRING,         // 字符串对象
    UPVALUE,        // 闭包提升值对象
}
