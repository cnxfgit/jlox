package com.craftinginterpreters.lox.vm;

import com.craftinginterpreters.lox.compiler.Compiler;
import com.craftinginterpreters.lox.compiler.FunctionType;
import com.craftinginterpreters.lox.objects.ObjFunction;
import com.craftinginterpreters.lox.objects.ObjString;
import com.craftinginterpreters.lox.objects.ObjUpvalue;
import com.craftinginterpreters.lox.parser.Parser;
import com.craftinginterpreters.lox.value.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class Vm {

    private static final int FRAMES_MAX = 64;

    private static final int STACK_MAX = (FRAMES_MAX * Byte.MAX_VALUE);

    private CallFrame[] frames;

    private int frameCount;

    private Value stack[];

    private int stackTop;

    private Map<ObjString, Value> globals;

    public Map<ObjString, Value> strings;

    private String initString;

    private ObjUpvalue[] openUpvalues;

    public Vm() {
        resetStack();
        this.frames = new CallFrame[FRAMES_MAX];
        this.stack = new Value[STACK_MAX];
        this.globals = new HashMap<>();
        this.strings = new HashMap<>();
        this.initString = "init";
    }

    private void resetStack() {
        this.stackTop = 0;
        this.frameCount = 0;
        this.openUpvalues = null;
    }

    public InterpretResult interpret(String source) {
        Compiler compiler = new Compiler(source, FunctionType.SCRIPT);

        Compiler.parser.hadError = false;
        Compiler.parser.panicMode = false;

        compiler.advance();

        ObjFunction function = compiler.compile();
        return InterpretResult.OK;
    }


}
