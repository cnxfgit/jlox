package com.craftinginterpreters.lox.vm;

import com.craftinginterpreters.lox.Lox;
import com.craftinginterpreters.lox.chunk.OpCode;
import com.craftinginterpreters.lox.compiler.Compiler;
import com.craftinginterpreters.lox.compiler.FunctionType;
import com.craftinginterpreters.lox.debug.Debug;
import com.craftinginterpreters.lox.objects.*;
import com.craftinginterpreters.lox.scanner.Scanner;
import com.craftinginterpreters.lox.value.Value;
import com.craftinginterpreters.lox.value.ValueType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class Vm {

    private static final int FRAMES_MAX = 64;

    private static final int STACK_MAX = (FRAMES_MAX * Byte.MAX_VALUE);

    private final CallFrame[] frames;

    private int frameCount;

    private final Value[] stack;

    private int stackTop;

    private final Map<ObjString, Value> globals;

    public Map<ObjString, Value> strings;

    private final String initString;

    private ObjUpvalue openUpvalues;

    public Vm() {
        resetStack();
        this.frames = new CallFrame[FRAMES_MAX];
        for (int i = 0; i < FRAMES_MAX; i++) {
            this.frames[i] = new CallFrame();
        }

        this.stack = new Value[STACK_MAX];
        for (int i = 0; i < STACK_MAX; i++) {
            this.stack[i] = new Value();
        }

        this.globals = new HashMap<>();
        this.strings = new HashMap<>();
        this.initString = "init";

        defineNative("clock", (argCount, args) -> {
            return new Value(ValueType.NUMBER, System.currentTimeMillis());
        });
    }

    private void resetStack() {
        this.stackTop = 0;
        this.frameCount = 0;
        this.openUpvalues = null;
    }

    private void defineNative(String name, NativeFn function) {
        push(new ObjString(name).toValue());
        push(new ObjNative(function).toValue());
        this.globals.put(this.stack[0].asString(), this.stack[1]);
        pop();
        pop();
    }

    public void push(Value value) {
        this.stack[stackTop] = value;
        this.stackTop++;
    }

    public Value pop() {
        this.stackTop--;
        return stackTop < 0 ? null : this.stack[stackTop];
    }

    private Value peek(int distance) {
        return this.stack[this.stackTop - 1 - distance];
    }

    private void runtimeError(String message, Object... args) {
        System.err.printf(message + "%n", args);

        for (int i = this.frameCount - 1; i >= 0; i--) {
            CallFrame frame = this.frames[i];
            ObjFunction function = frame.getClosure().getFunction();
            int instruction = frame.getIp() - 1;
            System.err.printf("[line %d] in ", function.getChunk().getLines().get(instruction));
            if (function.getName() == null) {
                System.err.print("script\n");
            } else {
                System.err.printf("%s()\n", function.getName());
            }
        }
        resetStack();
    }

    private boolean call(ObjClosure closure, int argCount) {
        if (argCount != closure.getFunction().getArity()) {
            runtimeError("Expected %d arguments but got %d.", closure.getFunction().getArity(), argCount);
            return false;
        }
        // 调用栈过长
        if (this.frameCount == FRAMES_MAX) {
            runtimeError("Stack overflow.");
            return false;
        }
        // 记录新函数栈帧
        CallFrame frame = this.frames[this.frameCount++];
        frame.setClosure(closure);

        frame.setIp(0);
        frame.setSlots(this.stackTop - argCount - 1);
        return true;
    }

    private byte readByte(CallFrame frame) {
        List<Byte> codes = frame.getClosure().getFunction().getChunk().getCodes();
        int index = frame.getIp();
        frame.setIp(index + 1);
        return codes.get(index);
    }

    private short readShort(CallFrame frame) {
        List<Byte> codes = frame.getClosure().getFunction().getChunk().getCodes();
        frame.setIp(frame.getIp() + 2);
        return (short) ((codes.get(frame.getIp() - 2) << 8) | codes.get(frame.getIp() - 1));
    }

    private Value readConstant(CallFrame frame) {
        List<Value> constants = frame.getClosure().getFunction().getChunk().getConstants();
        return constants.get(readByte(frame));
    }

    private ObjString readString(CallFrame frame) {
        return (ObjString) readConstant(frame).getObj();
    }

    private void binaryOp(ValueType type, String op) throws Exception {
        if (!peek(0).isNumber() && !peek(1).isNumber()) {
            runtimeError("Operands must be numbers.");
            throw new Exception("INTERPRET_RUNTIME_ERROR");
        }
        double b = pop().asNumber();
        double a = pop().asNumber();

        switch (op) {
            case "-":
                push(new Value(type, a - b));
                break;
            case "*":
                push(new Value(type, a * b));
                break;
            case "/":
                push(new Value(type, a / b));
                break;
            case ">":
                push(new Value(type, a > b));
                break;
            case "<":
                push(new Value(type, a < b));
                break;
            default:
                runtimeError("operator must be - * / > <.");
                throw new Exception("INTERPRET_RUNTIME_ERROR");
        }
    }


    private InterpretResult run() {
        CallFrame frame = this.frames[this.frameCount - 1];

        for (; ; ) {
            if (Lox.DEBUG_TRACE_EXECUTION) {
                System.out.print("          ");
                for (int slot = 0; slot < this.stackTop; slot++) {
                    System.out.print("[ ");
                    this.stack[slot].print();
                    System.out.print(" ]");
                }
                System.out.println();
                Debug.disassembleInstruction(frame.getClosure().getFunction().getChunk(), frame.getIp());
            }

            byte instruction = readByte(frame);
            switch (OpCode.intOf(instruction)) {
                case CONSTANT: {
                    Value constant = readConstant(frame);
                    push(constant);
                    break;
                }
                case NIL:
                    push(new Value());
                    break;
                case TRUE:
                    push(new Value(ValueType.BOOL, true));
                    break;
                case FALSE:
                    push(new Value(ValueType.BOOL, false));
                    break;
                case POP:
                    pop();
                    break;
                case GET_LOCAL: {
                    byte slot = readByte(frame);
                    push(this.stack[frame.getSlots() + slot]);
                    break;
                }
                case SET_LOCAL: {
                    byte slot = readByte(frame);
                    this.stack[frame.getSlots() + slot] = peek(0);
                    break;
                }
                case GET_GLOBAL: {
                    ObjString name = readString(frame);

                    Value value = this.globals.get(name);
                    if (value == null) {
                        runtimeError("Undefined variable '%s'.", name.getString());
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    push(value);
                    break;
                }
                case DEFINE_GLOBAL: {
                    ObjString name = readString(frame);
                    this.globals.put(name, peek(0));
                    pop();
                    break;
                }
                case SET_GLOBAL: {
                    ObjString name = readString(frame);
                    if (this.globals.put(name, peek(0)) == null) {
                        globals.remove(name);
                        runtimeError("Undefined variable '%s'.", name.getString());
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    break;
                }
                case GET_UPVALUE: {
                    byte slot = readByte(frame);
                    ObjUpvalue upvalue = frame.getClosure().getUpvalues().get(slot);
                    int location = upvalue.getLocation();
                    if (!upvalue.getClosed().isNil()) {
                        push(upvalue.getClosed());
                    } else {
                        push(this.stack[location]);
                    }
                    break;
                }
                case SET_UPVALUE: {
                    byte slot = readByte(frame);
                    ObjUpvalue upvalue = frame.getClosure().getUpvalues().get(slot);
                    upvalue.setLocation(this.stackTop - 1);
                    upvalue.setClosed(this.stack[this.stackTop - 1]);
                    break;
                }
                case GET_PROPERTY: {
                    if (!peek(0).isInstance()) {
                        runtimeError("Only instances have properties.");
                        return InterpretResult.RUNTIME_ERROR;
                    }

                    ObjInstance instance = peek(0).asInstance();
                    ObjString name = readString(frame);

                    Value value = instance.getFields().get(name);

                    if (value != null) {
                        pop(); // Instance.
                        push(value);
                        break;
                    }

                    if (!bindMethod(instance.getKlass(), name)) {
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    break;
                }
                case SET_PROPERTY: {
                    if (!peek(1).isInstance()) {
                        runtimeError("Only instances have fields.");
                        return InterpretResult.RUNTIME_ERROR;
                    }

                    ObjInstance instance = peek(1).asInstance();
                    instance.getFields().put(readString(frame), peek(0));
                    Value value = pop();
                    pop();
                    push(value);
                    break;
                }
                case GET_SUPER: {
                    ObjString name = readString(frame);
                    ObjClass superclass = pop().asClass();

                    if (!bindMethod(superclass, name)) {
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    break;
                }
                case EQUAL: {
                    Value b = pop();
                    Value a = pop();
                    push(new Value(ValueType.BOOL, a.equals(b)));
                    break;
                }
                case GREATER:
                    try {
                        binaryOp(ValueType.BOOL, ">");
                    } catch (Exception e) {
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    break;
                case LESS:
                    try {
                        binaryOp(ValueType.BOOL, "<");
                    } catch (Exception e) {
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    break;
                case ADD: {
                    if (peek(0).isString() && peek(1).isString()) {
                        concatenate();
                    } else if (peek(0).isNumber() && peek(1).isNumber()) {
                        double b = pop().asNumber();
                        double a = pop().asNumber();
                        push(new Value(ValueType.NUMBER, a + b));
                    } else {
                        runtimeError("Operands must be two numbers or two strings.");
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    break;
                }
                case SUBTRACT:
                    try {
                        binaryOp(ValueType.NUMBER, "-");
                    } catch (Exception e) {
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    break;
                case MULTIPLY:
                    try {
                        binaryOp(ValueType.NUMBER, "*");
                    } catch (Exception e) {
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    break;
                case DIVIDE:
                    try {
                        binaryOp(ValueType.NUMBER, "/");
                    } catch (Exception e) {
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    break;
                case NOT:
                    push(new Value(ValueType.BOOL, isFalsey(pop())));
                    break;
                case NEGATE:
                    if (!peek(0).isNumber()) {
                        runtimeError("Operand must be a number.");
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    push(new Value(ValueType.NUMBER, -pop().asNumber()));
                    break;
                case PRINT: {
                    pop().print();
                    System.out.println();
                    break;
                }
                case JUMP: {
                    short offset = readShort(frame);
                    frame.setIp(frame.getIp() + offset);
                    break;
                }
                case JUMP_IF_FALSE: {
                    short offset = readShort(frame);
                    if (isFalsey(peek(0))) frame.setIp(frame.getIp() + offset);
                    break;
                }
                case LOOP: {
                    short offset = readShort(frame);
                    frame.setIp(frame.getIp() - offset);
                    break;
                }
                case CALL: {
                    int argCount = readByte(frame);
                    if (!callValue(peek(argCount), argCount)) {
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    // 调用成功后将栈帧还回去
                    frame = this.frames[this.frameCount - 1];
                    break;
                }
                case INVOKE: {
                    ObjString method = readString(frame);
                    int argCount = readByte(frame);
                    if (!invoke(method, argCount)) {
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    frame = this.frames[this.frameCount - 1];
                    break;
                }
                case SUPER_INVOKE: {
                    ObjString method = readString(frame);
                    int argCount = readByte(frame);
                    ObjClass superclass = pop().asClass();
                    if (!invokeFromClass(superclass, method, argCount)) {
                        return InterpretResult.RUNTIME_ERROR;
                    }
                    frame = this.frames[frameCount - 1];
                    break;
                }
                case CLOSURE: {
                    ObjFunction function = readConstant(frame).asFunction();
                    ObjClosure closure = new ObjClosure(function);
                    push(closure.toValue());
                    for (int i = 0; i < closure.getUpvalueCount(); i++) {
                        byte isLocal = readByte(frame);
                        byte index = readByte(frame);
                        if (isLocal != 0) {
                            closure.getUpvalues().set(i, captureUpvalue(frame.getSlots() + index));
                        } else {
                            closure.getUpvalues().set(i, frame.getClosure().getUpvalues().get(index));
                        }
                    }
                    break;
                }
                case CLOSE_UPVALUE:
                    closeUpvalues(stackTop - 1);
                    pop();
                    break;
                case RETURN: {
                    Value result = pop();
                    closeUpvalues(frame.getSlots());
                    this.frameCount--;
                    if (this.frameCount == 0) {
                        pop();
                        return InterpretResult.OK;
                    }

                    this.stackTop = frame.getSlots();
                    push(result);
                    frame = this.frames[this.frameCount - 1];
                    break;
                }
                case CLASS:
                    push(new ObjClass(readString(frame)).toValue());
                    break;
                case INHERIT: {
                    Value superclass = peek(1);
                    if (!superclass.isClass()) {
                        runtimeError("Superclass must be a class.");
                        return InterpretResult.RUNTIME_ERROR;
                    }

                    ObjClass subclass = peek(0).asClass();
                    subclass.getMethods().putAll(superclass.asClass().getMethods());
                    pop(); // Subclass.
                    break;
                }
                case METHOD:
                    defineMethod(readString(frame));
                    break;
            }
        }
    }

    private void defineMethod(ObjString name) {
        Value method = peek(0);
        ObjClass klass = peek(1).asClass();
        klass.getMethods().put(name, method);
        pop();
    }

    private void closeUpvalues(int last) {
        while (this.openUpvalues != null && this.openUpvalues.getLocation() >= last) {
            ObjUpvalue upvalue = this.openUpvalues;
            upvalue.setClosed(this.stack[upvalue.getLocation()]);
            this.openUpvalues = upvalue.getNext();
        }
    }

    private ObjUpvalue captureUpvalue(int local) {
        ObjUpvalue prevUpvalue = null;
        ObjUpvalue upvalue = this.openUpvalues;
        while (upvalue != null && upvalue.getLocation() > local) {
            prevUpvalue = upvalue;
            upvalue = upvalue.getNext();
        }

        if (upvalue != null && upvalue.getClosed() == this.stack[local]) {
            return upvalue;
        }

        ObjUpvalue createdUpvalue = new ObjUpvalue(local);

        createdUpvalue.setNext(upvalue);

        if (prevUpvalue == null) {
            this.openUpvalues = createdUpvalue;
        } else {
            prevUpvalue.setNext(createdUpvalue);
        }

        return createdUpvalue;
    }


    private boolean invokeFromClass(ObjClass klass, ObjString name, int argCount) {
        Value method = klass.getMethods().get(name);
        if (method == null) {
            runtimeError("Undefined property '%s'.", name);
            return false;
        }
        return call(method.asClosure(), argCount);
    }

    private boolean invoke(ObjString name, int argCount) {
        Value receiver = peek(argCount);

        if (!receiver.isInstance()) {
            runtimeError("Only instances have methods.");
            return false;
        }

        ObjInstance instance = receiver.asInstance();

        Value value = instance.getFields().get(name);

        if (value != null) {
            this.stack[stackTop - argCount - 1] = value;
            return callValue(value, argCount);
        }

        return invokeFromClass(instance.getKlass(), name, argCount);
    }

    private boolean callValue(Value callee, int argCount) {
        if (callee.isObj()) {
            switch (callee.getObj().getType()) {
                case BOUND_METHOD: {
                    ObjBoundMethod bound = callee.asBoundMethod();
                    this.stack[stackTop - argCount - 1] = bound.getReceiver();
                    return call(bound.getMethod(), argCount);
                }
                case CLASS: {
                    ObjClass klass = callee.asClass();
                    this.stack[stackTop - argCount - 1] = new ObjInstance(klass).toValue();
                    Value initializer = klass.getMethods().get(new ObjString(initString));
                    if (initializer != null) {
                        return call(initializer.asClosure(), argCount);
                    } else if (argCount != 0) {
                        runtimeError("Expected 0 arguments but got %d.", argCount);
                        return false;
                    }
                    return true;
                }
                case CLOSURE:
                    return call(callee.asClosure(), argCount);
                case NATIVE: {
                    NativeFn nativefn = callee.asNative().getFunction();
                    Value result = nativefn.call(argCount, this.stackTop - argCount);
                    this.stackTop -= argCount + 1;
                    push(result);
                    return true;
                }
                default:
                    break; // Non-callable object type.
            }
        }
        runtimeError("Can only call functions and classes.");
        return false;
    }

    private boolean isFalsey(Value value) {
        return value.isNil() || (value.isBool() && !value.asBool());
    }

    private void concatenate() {
        ObjString b = peek(0).asString();
        ObjString a = peek(1).asString();

        ObjString result = new ObjString(a.getString() + b.getString());

        pop();
        pop();

        push(result.toValue());
    }

    private boolean bindMethod(ObjClass klass, ObjString name) {
        Value method = klass.getMethods().get(name);
        if (method == null) {
            runtimeError("Undefined property '%s'.", name);
            return false;
        }

        ObjBoundMethod bound = new ObjBoundMethod(peek(0), method.asClosure());
        pop();
        push(bound.toValue());
        return true;
    }

    public InterpretResult interpret(String source) {
        Compiler compiler = new Compiler(new Scanner(source), FunctionType.SCRIPT);

        ObjFunction function = compiler.compile();
        if (function == null) return InterpretResult.COMPILE_ERROR;

        push(function.toValue());
        ObjClosure closure = new ObjClosure(function);
        pop();
        call(closure, 0);

        return run();
    }


}
