package com.craftinginterpreters.lox.vm;

import com.craftinginterpreters.lox.objects.Closure;

import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class CallFrame {

    public Closure closure;

    public List<Byte> ip;

    public List<Long> slots;

    public CallFrame(){

    }

}
