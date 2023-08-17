package com.craftinginterpreters.lox.objects;

import com.craftinginterpreters.lox.value.Value;

/**
 * @author hlx
 * @date 2023-08-17
 */
public interface NativeFn {

    Value call(int argCount, int args);

}
