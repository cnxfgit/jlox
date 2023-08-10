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

    public List<Byte> codes;
    public List<Integer> lines;
    public List<Value> valueList;

    public Chunk() {
        codes = new ArrayList<>();
        lines = new ArrayList<>();
        valueList = new ArrayList<>();
    }

    public int addConstant(Value value) {
        Lox.vm.push(value);
        valueList.add(value);
        Lox.vm.pop();
        return valueList.size() - 1;
    }

    public void write(byte b, int line) {
        this.codes.add(b);
        this.lines.add(line);
    }

}
