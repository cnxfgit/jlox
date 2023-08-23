package com.craftinginterpreters.lox.debug;

import com.craftinginterpreters.lox.chunk.Chunk;
import com.craftinginterpreters.lox.chunk.OpCode;
import com.craftinginterpreters.lox.objects.ObjFunction;

import java.util.Objects;

/**
 * @author hlx
 * @date 2023-08-08
 */
public class Debug {

    public static void disassembleChunk(Chunk chunk, String name) {
        System.out.printf("== %s ==\n", name); // 打印字节码块名

        // 遍历字节码块中的字节码
        for (int offset = 0; offset < chunk.getCodes().size(); ) {
            offset = disassembleInstruction(chunk, offset);
        }
    }

    public static int disassembleInstruction(Chunk chunk, int offset) {
        System.out.printf("%04d ", offset);    // 字节码偏移量
        // 行号打印
        if (offset > 0 && Objects.equals(chunk.getLines().get(offset), chunk.getLines().get(offset - 1))) {
            System.out.print("   | ");
        } else {
            System.out.printf("%4d ", chunk.getLines().get(offset));
        }

        // 反汇编当前字节码
        OpCode instruction = OpCode.intOf((int) chunk.getCodes().get(offset));
        switch (instruction) {
            case CONSTANT:
                return constantInstruction("CONSTANT", chunk, offset);
            case NIL:
                return simpleInstruction("NIL", offset);
            case TRUE:
                return simpleInstruction("TRUE", offset);
            case FALSE:
                return simpleInstruction("FALSE", offset);
            case POP:
                return simpleInstruction("POP", offset);
            case GET_LOCAL:
                return byteInstruction("GET_LOCAL", chunk, offset);
            case SET_LOCAL:
                return byteInstruction("SET_LOCAL", chunk, offset);
            case GET_GLOBAL:
                return constantInstruction("GET_GLOBAL", chunk, offset);
            case DEFINE_GLOBAL:
                return constantInstruction("DEFINE_GLOBAL", chunk, offset);
            case SET_GLOBAL:
                return constantInstruction("SET_GLOBAL", chunk, offset);
            case GET_UPVALUE:
                return byteInstruction("GET_UPVALUE", chunk, offset);
            case SET_UPVALUE:
                return byteInstruction("SET_UPVALUE", chunk, offset);
            case GET_PROPERTY:
                return constantInstruction("GET_PROPERTY", chunk, offset);
            case SET_PROPERTY:
                return constantInstruction("SET_PROPERTY", chunk, offset);
            case GET_SUPER:
                return constantInstruction("GET_SUPER", chunk, offset);
            case EQUAL:
                return simpleInstruction("EQUAL", offset);
            case GREATER:
                return simpleInstruction("GREATER", offset);
            case LESS:
                return simpleInstruction("LESS", offset);
            case ADD:
                return simpleInstruction("ADD", offset);
            case SUBTRACT:
                return simpleInstruction("SUBTRACT", offset);
            case MULTIPLY:
                return simpleInstruction("MULTIPLY", offset);
            case DIVIDE:
                return simpleInstruction("DIVIDE", offset);
            case NOT:
                return simpleInstruction("NOT", offset);
            case NEGATE:
                return simpleInstruction("NEGATE", offset);
            case PRINT:
                return simpleInstruction("PRINT", offset);
            case JUMP:
                return jumpInstruction("JUMP", 1, chunk, offset);
            case JUMP_IF_FALSE:
                return jumpInstruction("JUMP_IF_FALSE", 1, chunk, offset);
            case LOOP:
                return jumpInstruction("LOOP", -1, chunk, offset);
            case CALL:
                return byteInstruction("CALL", chunk, offset);
            case INVOKE:
                return invokeInstruction("INVOKE", chunk, offset);
            case SUPER_INVOKE:
                return invokeInstruction("SUPER_INVOKE", chunk, offset);
            case CLOSURE: {
                offset++;
                byte constant = chunk.getCodes().get(offset++);
                System.out.printf("%-16s %4d ", "CLOSURE", constant);
                chunk.getConstants().get(constant).print();
                System.out.println();

                ObjFunction function = (ObjFunction) chunk.getConstants().get(constant).getObj();
                for (int j = 0; j < function.getUpvalueCount(); j++) {
                    int isLocal = chunk.getCodes().get(offset++);
                    int index = chunk.getCodes().get(offset++);
                    System.out.printf("%04d      |                     %s %d\n",
                            offset - 2, isLocal != 0 ? "local" : "upvalue", index);
                }

                return offset;
            }
            case CLOSE_UPVALUE:
                return simpleInstruction("CLOSE_UPVALUE", offset);
            case RETURN:
                return simpleInstruction("RETURN", offset);
            case CLASS:
                return constantInstruction("CLASS", chunk, offset);
            case INHERIT:
                return simpleInstruction("INHERIT", offset);
            case METHOD:
                return constantInstruction("METHOD", chunk, offset);
            default:
                System.out.printf("Unknown opcode %d\n", instruction.ordinal());
                return offset + 1;
        }
    }

    private static int constantInstruction(String name, Chunk chunk, int offset) {
        byte constant = chunk.getCodes().get(offset + 1);
        System.out.printf("%-16s %4d '", name, constant);
        chunk.getConstants().get(constant).print();
        System.out.println();
        return offset + 2;
    }

    private static int simpleInstruction(String name, int offset) {
        System.out.printf("%s\n", name);
        return offset + 1;
    }

    private static int byteInstruction(String name, Chunk chunk, int offset) {
        byte slot = chunk.getCodes().get(offset + 1);
        System.out.printf("%-16s %4d\n", name, slot);
        return offset + 2;
    }

    private static int jumpInstruction(String name, int sign, Chunk chunk, int offset) {
        short jump = (short) (chunk.getCodes().get(offset + 1) << 8);
        jump |= chunk.getCodes().get(offset + 2);
        System.out.printf("%-16s %4d -> %d\n", name, offset, offset + 3 + sign * jump);
        return offset + 3;
    }

    private static int invokeInstruction(String name, Chunk chunk, int offset) {
        byte constant = chunk.getCodes().get(offset + 1);
        byte argCount = chunk.getCodes().get(offset + 2);
        System.out.printf("%-16s (%d args) %4d '", name, argCount, constant);
        chunk.getConstants().get(constant).print();
        System.out.print("'\n");
        return offset + 3;
    }
}
