package com.craftinginterpreters.lox.compiler;

import com.craftinginterpreters.lox.objects.ObjFunction;
import com.craftinginterpreters.lox.objects.ObjString;
import com.craftinginterpreters.lox.parser.Parser;
import com.craftinginterpreters.lox.scanner.Scanner;
import com.craftinginterpreters.lox.scanner.Token;
import com.craftinginterpreters.lox.scanner.TokenType;

import static com.craftinginterpreters.lox.scanner.TokenType.*;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class Compiler {

    static Compiler current = null;

    public static Parser parser = new Parser();

    private Compiler enclosing;

    private ObjFunction function;

    private FunctionType type;

    private Local[] locals;

    private int localCount;

    private Upvalue[] upvalues;

    private int scopeDepth;

    private Scanner scanner;

    public Compiler(String source, FunctionType type) {
        this.enclosing = this;
        this.type = type;
        this.scanner = new Scanner(source);
        this.locals = new Local[Byte.MAX_VALUE];
        this.upvalues = new Upvalue[Byte.MAX_VALUE];
        this.localCount = 0;
        this.scopeDepth = 0;

        this.function = new ObjFunction();
        current = this;

        if (type != FunctionType.SCRIPT) {
            current.function.name = ObjString.copyString(parser.previous.message);
        }

        Local local = new Local(0, false);
        current.locals[current.localCount++] = local;
        if (type != FunctionType.FUNCTION) {
            local.name.message = "this";
            local.name.length = 4;
        } else {
            local.name.message = "";
            local.name.length = 0;
        }
    }

    public ObjFunction compile() {
        return new ObjFunction();
    }

    public void advance(){
        parser.previous = parser.current;

        for (;;) {
            parser.current = scanner.scanToken();

            if (parser.current.type != TokenType.ERROR) break;

            errorAtCurrent(parser.current.message);
        }
    }

    private void errorAt(Token token, String message) {
        if (parser.panicMode) return;
        parser.panicMode = true;
        System.err.printf("[line %d] Error%n", token.line);

        if (token.type == EOF) {
            System.err.println(" at end");
        } else if (token.type == ERROR) {
            // Nothing.
        } else {
            System.err.printf(" at '%s'", message);
        }

        System.err.printf(": %s\n", token.message);
        parser.hadError = true;
    }

    private void errorAtCurrent(String message) {
        errorAt(parser.current, message);
    }

}
