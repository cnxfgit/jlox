package com.craftinginterpreters.lox.vm;


import com.craftinginterpreters.lox.objects.ObjClosure;
import com.craftinginterpreters.lox.value.Value;

import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class CallFrame {

    public ObjClosure closure;

    public List<Byte> ip;

    public List<Value> slots;

    public CallFrame(){

    }

}
