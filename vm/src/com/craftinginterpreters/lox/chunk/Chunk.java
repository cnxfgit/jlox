package com.craftinginterpreters.lox.chunk;

import com.craftinginterpreters.lox.value.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author hlx
 * @date 2023-08-05
 */
public class Chunk {

    private List<Byte> codes;
    private List<Integer> lines;
    private List<Value> valueList;

    public Chunk(){
        codes = new ArrayList<>();
        lines = new ArrayList<>();
        valueList = new ArrayList<>();
    }
}
