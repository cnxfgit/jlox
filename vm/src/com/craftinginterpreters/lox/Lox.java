package com.craftinginterpreters.lox;

import com.craftinginterpreters.lox.vm.InterpretResult;
import com.craftinginterpreters.lox.vm.Vm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author hlx
 * @date 2023-07-25
 */
public class Lox {

    public static Vm vm;

    public static boolean DEBUG_PRINT_CODE = false;

    public static boolean DEBUG_TRACE_EXECUTION = true;

    public static void main(String[] args) throws IOException {
        vm = new Vm();

        if (args.length == 0) {
            repl();
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            System.err.println("Usage: jlox [path]\n");
            System.exit(64);
        }
    }

    private static void repl() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            vm.interpret(line);
        }
    }

    private static void runFile(String path) {
        byte[] bytes = null;
        try {
            bytes = Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            System.err.printf("Could not read file %s.%n", path);
            System.exit(74);
        }

        String source = new String(bytes, Charset.defaultCharset());
        InterpretResult result = vm.interpret(source);
        switch (result) {
            case COMPILE_ERROR:
                System.exit(65);
            case RUNTIME_ERROR:
                System.exit(70);
        }
    }

}
