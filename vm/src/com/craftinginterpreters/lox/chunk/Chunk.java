package com.craftinginterpreters.lox.chunk;

import com.craftinginterpreters.lox.Lox;
import com.craftinginterpreters.lox.value.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hlx
 * @date 2023-08-05
 */
public class Chunk {

    private final List<Byte> codes;
    private List<Integer> lines;
    private List<Value> constants;

    public Chunk() {
        codes = new ArrayList<>();
        lines = new ArrayList<>();
        constants = new ArrayList<>();
    }

    public int addConstant(Value value) {
        Lox.vm.push(value);
        constants.add(value);
        Lox.vm.pop();
        return constants.size() - 1;
    }

    public void write(byte b, int line) {
        this.codes.add(b);
        this.lines.add(line);
    }

    public List<Byte> getCodes() {
        return codes;
    }

    public List<Integer> getLines() {
        return lines;
    }

    public List<Value> getConstants() {
        return constants;
    }
}
