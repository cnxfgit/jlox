package com.craftinginterpreters.lox.vm;

import com.craftinginterpreters.lox.compiler.Compiler;
import com.craftinginterpreters.lox.objects.Function;
import com.craftinginterpreters.lox.scanner.Scanner;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class Vm {

    public InterpretResultEnum interpret(String source) {
        Scanner scanner = new Scanner(source);
        Compiler compiler = new Compiler();
        Function function = compiler.compile();
        return InterpretResultEnum.OK;
    }

}
