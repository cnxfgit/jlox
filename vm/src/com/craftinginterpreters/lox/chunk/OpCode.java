package com.craftinginterpreters.lox.chunk;

/**
 * @author hlx
 * @date 2023-08-08
 */
public enum OpCode {

    CONSTANT,        // 写入常量
    NIL,             // 空指令 nil
    TRUE,            // true指令
    FALSE,           // false指令
    POP,             // 弹出指令
    GET_LOCAL,       // 获取局部变量
    SET_LOCAL,       // 赋值局部变量
    GET_GLOBAL,      // 获取全局变量
    DEFINE_GLOBAL,   // 定义全局变量
    SET_GLOBAL,      // 赋值全局变量
    GET_UPVALUE,     // 获取升值指令
    SET_UPVALUE,     // 赋值升值指令
    GET_PROPERTY,    // 获取属性指令
    SET_PROPERTY,    // 赋值属性指令
    GET_SUPER,       // 获取父类指令
    EQUAL,           // 赋值指令 =
    GREATER,         // 大于指令 >
    LESS,            // 小于指令 <
    ADD,             // 加指令 +
    SUBTRACT,        // 减指令 -
    MULTIPLY,        // 乘指令 *
    DIVIDE,          // 除指令 /
    NOT,             // 非指令 !
    NEGATE,          // 负指令 -
    PRINT,           // 打印指令
    JUMP,            // 分支跳转指令
    JUMP_IF_FALSE,   // if false分支跳转指令
    LOOP,            // 循环指令
    CALL,            // 调用指令
    INVOKE,          // 执行指令
    SUPER_INVOKE,    // 父类执行指令
    CLOSURE,         // 闭包指令
    CLOSE_UPVALUE,   // 关闭提升值
    RETURN,          // 返回指令
    CLASS,           // 类指令
    INHERIT,         // 继承指令
    METHOD;           // 方法指令

    public static OpCode intOf(int i) {
        OpCode[] values = OpCode.values();
        for (OpCode value : values) {
            if (value.ordinal() == i) {
                return value;
            }
        }
        throw new RuntimeException("OpCode exception");
    }

}
