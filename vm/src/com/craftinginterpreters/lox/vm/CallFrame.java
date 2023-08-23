package com.craftinginterpreters.lox.vm;


import com.craftinginterpreters.lox.objects.ObjClosure;
import com.craftinginterpreters.lox.value.Value;

import java.util.List;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class CallFrame {

    private ObjClosure closure;

    private int ip;

    private int slots;

    public CallFrame(){}

    public ObjClosure getClosure() {
        return closure;
    }

    public void setClosure(ObjClosure closure) {
        this.closure = closure;
    }

    public int getIp() {
        return ip;
    }

    public void setIp(int ip) {
        this.ip = ip;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }
}
