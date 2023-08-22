package com.craftinginterpreters.lox.compiler;

import com.craftinginterpreters.lox.Lox;
import com.craftinginterpreters.lox.chunk.Chunk;
import com.craftinginterpreters.lox.chunk.OpCode;
import com.craftinginterpreters.lox.debug.Debug;
import com.craftinginterpreters.lox.objects.Obj;
import com.craftinginterpreters.lox.objects.ObjFunction;
import com.craftinginterpreters.lox.objects.ObjString;
import com.craftinginterpreters.lox.parser.Parser;
import com.craftinginterpreters.lox.scanner.Scanner;
import com.craftinginterpreters.lox.scanner.Token;
import com.craftinginterpreters.lox.scanner.TokenType;
import com.craftinginterpreters.lox.value.Value;
import com.craftinginterpreters.lox.value.ValueType;

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

    private final ParseRule rules[];

    {
        rules = new ParseRule[TokenType.values().length];
        rules[TokenType.LEFT_PAREN.ordinal()] = new ParseRule(grouping(), call(), Precedence.CALL);
        rules[TokenType.RIGHT_PAREN.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.LEFT_BRACE.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.RIGHT_BRACE.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.COMMA.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.DOT.ordinal()] = new ParseRule(null, dot(), Precedence.CALL);
        rules[TokenType.MINUS.ordinal()] = new ParseRule(unary(), binary(), Precedence.TERM);
        rules[TokenType.PLUS.ordinal()] = new ParseRule(null, binary(), Precedence.TERM);
        rules[TokenType.SEMICOLON.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.SLASH.ordinal()] = new ParseRule(null, binary(), Precedence.FACTOR);
        rules[TokenType.STAR.ordinal()] = new ParseRule(null, binary(), Precedence.FACTOR);
        rules[TokenType.BANG.ordinal()] = new ParseRule(unary(), null, Precedence.NONE);
        rules[TokenType.BANG_EQUAL.ordinal()] = new ParseRule(null, binary(), Precedence.EQUALITY);
        rules[TokenType.EQUAL.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.EQUAL_EQUAL.ordinal()] = new ParseRule(null, binary(), Precedence.EQUALITY);
        rules[TokenType.GREATER.ordinal()] = new ParseRule(null, binary(), Precedence.COMPARISON);
        rules[TokenType.GREATER_EQUAL.ordinal()] = new ParseRule(null, binary(), Precedence.COMPARISON);
        rules[TokenType.LESS.ordinal()] = new ParseRule(null, binary(), Precedence.COMPARISON);
        rules[TokenType.LESS_EQUAL.ordinal()] = new ParseRule(null, binary(), Precedence.COMPARISON);
        rules[TokenType.IDENTIFIER.ordinal()] = new ParseRule(variable(), null, Precedence.NONE);
        rules[TokenType.STRING.ordinal()] = new ParseRule(string(), null, Precedence.NONE);
        rules[TokenType.NUMBER.ordinal()] = new ParseRule(number(), null, Precedence.NONE);
        rules[TokenType.AND.ordinal()] = new ParseRule(null, and_(), Precedence.AND);
        rules[TokenType.CLASS.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.ELSE.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.FALSE.ordinal()] = new ParseRule(literal(), null, Precedence.NONE);
        rules[TokenType.FOR.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.FUN.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.IF.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.NIL.ordinal()] = new ParseRule(literal(), null, Precedence.NONE);
        rules[TokenType.OR.ordinal()] = new ParseRule(null, or_(), Precedence.OR);
        rules[TokenType.PRINT.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.RETURN.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.SUPER.ordinal()] = new ParseRule(super_(), null, Precedence.NONE);
        rules[TokenType.THIS.ordinal()] = new ParseRule(this_(), null, Precedence.NONE);
        rules[TokenType.TRUE.ordinal()] = new ParseRule(literal(), null, Precedence.NONE);
        rules[TokenType.VAR.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.WHILE.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.ERROR.ordinal()] = new ParseRule(null, null, Precedence.NONE);
        rules[TokenType.EOF.ordinal()] = new ParseRule(null, null, Precedence.NONE);
    }

    public Compiler(Scanner scanner, FunctionType type) {
        this.enclosing = current;
        this.type = type;
        this.scanner = scanner;
        this.locals = new Local[Byte.MAX_VALUE];
        for (int i = 0; i < locals.length; i++) {
            locals[i] = new Local(0, false);
        }

        this.upvalues = new Upvalue[Byte.MAX_VALUE];
        for (int i = 0; i < upvalues.length; i++) {
            upvalues[i] = new Upvalue();
        }

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
            local.name = new Token(TokenType.IDENTIFIER, 0, 4, 0, "this");
        } else {
            local.name = new Token(TokenType.IDENTIFIER, 0, 0, 0, "");
        }
    }

    private static Chunk currentChunk() {
        return current.function.chunk;
    }


    private byte makeConstant(Value value) {
        int constant = currentChunk().addConstant(value);
        if (constant > Byte.MAX_VALUE) {
            error("Too many constants in one chunk.");
            return 0;
        }
        return (byte) constant;
    }

    private byte identifierConstant(Token name) {
        return makeConstant(ObjString.copyString(name.message).toValue());
    }

    private boolean identifiersEqual(Token a, Token b) {
        return a.message.equals(b.message);
    }

    private void consume(TokenType type, String message) {
        if (parser.current.type == type) {
            advance();
            return;
        }

        errorAtCurrent(message);
    }

    private boolean check(TokenType type) {
        return parser.current.type == type;
    }

    private boolean match(TokenType type) {
        if (!check(type)) return false;
        advance();
        return true;
    }

    private void emitByte(OpCode code) {
        currentChunk().write((byte) code.ordinal(), parser.previous.line);
    }

    private void emitByte(byte b) {
        currentChunk().write(b, parser.previous.line);
    }

    private void emitBytes(OpCode op1, OpCode op2) {
        emitByte(op1);
        emitByte(op2);
    }

    private void emitBytes(byte b1, byte b2) {
        emitByte(b1);
        emitByte(b2);
    }

    private void emitBytes(OpCode op, byte b) {
        emitByte(op);
        emitByte(b);
    }

    private void emitLoop(int loopStart) {
        emitByte(OpCode.LOOP);

        int offset = currentChunk().codes.size() - loopStart + 2;
        if (offset > Short.MAX_VALUE) error("Loop body too large.");

        emitByte((byte) ((offset >> 8) & 0xff));
        emitByte((byte) (offset & 0xff));
    }


    private void emitConstant(Value value) {
        emitBytes(OpCode.CONSTANT, makeConstant(value));
    }

    private void patchJump(int offset) {
        // -offset得到 字节指令的位置  -2 再得到then语句的位置
        int jump = currentChunk().codes.size() - offset - 2;

        // 最大只能跳转两个字节的字节码
        if (jump > Short.MAX_VALUE) {
            error("Too much code to jump over.");
        }

        // 回写需要跳过的大小
        currentChunk().codes.set(offset, (byte) ((jump >> 8) & 0xff));
        currentChunk().codes.set(offset + 1, (byte) (jump & 0xff));
    }

    private void emitReturn() {
        if (current.type == FunctionType.INITIALIZER) {
            emitBytes(OpCode.GET_LOCAL, (byte) 0);
        } else {
            emitByte(OpCode.NIL);
        }
        emitByte(OpCode.RETURN);
    }

    private int emitJump(OpCode instruction) {
        emitByte(instruction);
        emitByte((byte) 0xff);
        emitByte((byte) 0xff);
        return currentChunk().codes.size() - 2;
    }

    public ObjFunction compile() {
        parser.hadError = false;
        parser.panicMode = false;

        advance();

        while (!match(TokenType.EOF)) {
            declaration();
        }

        ObjFunction objFunction = endCompiler();
        return parser.hadError ? null : objFunction;
    }

    public void advance() {
        parser.previous = parser.current;

        for (; ; ) {
            parser.current = scanner.scanToken();

            if (parser.current.type != TokenType.ERROR) break;

            errorAtCurrent(parser.current.message);
        }
    }

    private void error(String message) {
        errorAt(parser.previous, message);
    }

    private void errorAt(Token token, String message) {
        if (parser.panicMode) return;
        parser.panicMode = true;
        System.err.printf("[line %d] Error%n", token.line);

        if (token.type == TokenType.EOF) {
            System.err.println(" at end");
        } else if (token.type == TokenType.ERROR) {
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

    private void declaration() {
        if (match(TokenType.CLASS)) {
            classDeclaration();
        } else if (match(TokenType.FUN)) {
            funDeclaration();
        } else if (match(TokenType.VAR)) {
            varDeclaration();
        } else {
            statement();
        }

        // 如果处于异常模式  则同步掉异常继续编译
        if (parser.panicMode) synchronize();
    }

    private void synchronize() {
        parser.panicMode = false;

        while (parser.current.type != TokenType.EOF) {
            if (parser.previous.type == TokenType.SEMICOLON) return;
            switch (parser.current.type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;

                default:
                    ; // Do nothing.
            }

            advance();
        }
    }

    private void statement() {
        if (match(TokenType.PRINT)) {
            printStatement();
        } else if (match(TokenType.FOR)) {
            forStatement();
        } else if (match(TokenType.IF)) {
            ifStatement();
        } else if (match(TokenType.RETURN)) {
            returnStatement();
        } else if (match(TokenType.WHILE)) {
            whileStatement();
        } else if (match(TokenType.LEFT_BRACE)) {
            beginScope();
            block();
            endScope();
        } else {
            expressionStatement();
        }
    }

    private void forStatement() {
        beginScope();
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");
        // for 第一语句 只执行一次
        if (match(TokenType.SEMICOLON)) {
            // No initializer.
        } else if (match(TokenType.VAR)) {
            varDeclaration();
        } else {
            expressionStatement();
        }
        // 循环起点
        int loopStart = currentChunk().codes.size();
        // for的第二语句  表达式语句
        int exitJump = -1;
        if (!match(TokenType.SEMICOLON)) {
            expression();
            consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

            // Jump out of the loop if the condition is false.
            exitJump = emitJump(OpCode.JUMP_IF_FALSE);
            emitByte(OpCode.POP); // Condition.
        }

        // for的第三语句 增量子句
        if (!match(TokenType.RIGHT_PAREN)) {
            int bodyJump = emitJump(OpCode.JUMP);
            int incrementStart = currentChunk().codes.size();
            expression();
            emitByte(OpCode.POP);
            consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

            emitLoop(loopStart);
            loopStart = incrementStart;
            patchJump(bodyJump);
        }

        // for 主体
        statement();
        emitLoop(loopStart);

        // 修复跳跃
        if (exitJump != -1) {
            patchJump(exitJump);
            emitByte(OpCode.POP);
        }

        endScope();
    }

    private void ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");

        // then 分支跳转点
        int thenJump = emitJump(OpCode.JUMP_IF_FALSE);
        // 如果为false 这个 pop不会被执行  会执行下面的pop
        // 如果为 true 执行这个pop之后 跳过实体else 或者空else(只有一个pop)
        // 弹出条件表达式
        emitByte(OpCode.POP);
        statement();

        // else 分支跳转点
        int elseJump = emitJump(OpCode.JUMP);
        // 回写then分支跳转的长度回写
        patchJump(thenJump);

        // 弹出条件表达式
        emitByte(OpCode.POP);
        // then 分支过后探查 是否有else 这个if不触发的话则跳转一个 空else
        if (match(TokenType.ELSE)) statement();
        // else分支跳转长度回写
        patchJump(elseJump);
    }


    private void printStatement() {
        expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        emitByte(OpCode.PRINT);
    }


    private void returnStatement() {
        if (current.type == FunctionType.SCRIPT) {
            error("Can't return from top-level code.");
        }

        if (match(TokenType.SEMICOLON)) {
            emitReturn();
        } else {
            if (current.type == FunctionType.INITIALIZER) {
                error("Can't return a value from an initializer.");
            }

            expression();
            consume(TokenType.SEMICOLON, "Expect ';' after return value.");
            emitByte(OpCode.RETURN);
        }
    }


    private void whileStatement() {
        // 循环起点
        int loopStart = currentChunk().codes.size();
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");

        // 如果为false直接跳到下面的pop
        int exitJump = emitJump(OpCode.JUMP_IF_FALSE);
        emitByte(OpCode.POP);
        statement();
        // 循环节点
        emitLoop(loopStart);

        patchJump(exitJump);
        // false的跳入点
        emitByte(OpCode.POP);
    }


    private void expressionStatement() {
        expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        emitByte(OpCode.POP);
    }

    private void addLocal(Token name) {
        if (current.localCount == Byte.MAX_VALUE) {
            error("Too many local variables in function.");
            return;
        }

        Local local = current.locals[current.localCount++];
        local.name = name;
        local.depth = -1;
        local.isCaptured = false;
    }

    private void declareVariable() {
        if (current.scopeDepth == 0) return;

        Token name = parser.previous;

        for (int i = current.localCount - 1; i >= 0; i--) {
            Local local = current.locals[i];
            if (local.depth != -1 && local.depth < current.scopeDepth) {
                break;
            }

            if (identifiersEqual(name, local.name)) {
                error("Already a variable with this name in this scope.");
            }
        }
        addLocal(name);
    }

    private byte parseVariable(String errorMessage) {
        consume(TokenType.IDENTIFIER, errorMessage);

        declareVariable();
        if (current.scopeDepth > 0) return 0;

        return identifierConstant(parser.previous);
    }

    private void markInitialized() {
        if (current.scopeDepth == 0) return;
        current.locals[current.localCount - 1].depth = current.scopeDepth;
    }


    private byte argumentList() {
        byte argCount = 0;
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                expression();
                if (argCount == 127) {
                    error("Can't have more than 127 arguments.");
                }
                argCount++;
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");
        return argCount;
    }

    private void defineVariable(byte global) {
        if (current.scopeDepth > 0) {
            markInitialized();
            return;
        }
        emitBytes(OpCode.DEFINE_GLOBAL, global);
    }

    private int resolveLocal(Compiler compiler, Token name) {
        for (int i = compiler.localCount - 1; i >= 0; i--) {
            Local local = compiler.locals[i];
            if (identifiersEqual(name, local.name)) {
                if (local.depth == -1) {
                    error("Can't read local variable in its own initializer.");
                }
                return i;
            }
        }

        return -1;
    }

    private int addUpvalue(Compiler compiler, byte index, boolean isLocal) {
        int upvalueCount = compiler.function.upvalueCount;

        for (int i = 0; i < upvalueCount; i++) {
            Upvalue upvalue = compiler.upvalues[i];
            if (upvalue.index == index && upvalue.isLocal == isLocal) {
                return i;
            }
        }

        if (upvalueCount == Byte.MAX_VALUE) {
            error("Too many closure variables in function.");
            return 0;
        }

        compiler.upvalues[upvalueCount].isLocal = isLocal;
        compiler.upvalues[upvalueCount].index = index;
        return compiler.function.upvalueCount++;
    }

    private int resolveUpvalue(Compiler compiler, Token name) {
        if (compiler.enclosing == null) return -1;
        int local = resolveLocal(compiler.enclosing, name);
        if (local != -1) {
            compiler.enclosing.locals[local].isCaptured = true;
            return addUpvalue(compiler, (byte) local, true);
        }

        int upvalue = resolveUpvalue(compiler.enclosing, name);
        if (upvalue != -1) {
            return addUpvalue(compiler, (byte) upvalue, false);
        }

        return -1;
    }

    private void namedVariable(Token name, boolean canAssign) {
        OpCode getOp, setOp;
        int arg = resolveLocal(current, name);
        if (arg != -1) {
            getOp = OpCode.GET_LOCAL;
            setOp = OpCode.SET_LOCAL;
        } else if ((arg = resolveUpvalue(current, name)) != -1) {
            getOp = OpCode.GET_UPVALUE;
            setOp = OpCode.SET_UPVALUE;
        } else {
            arg = identifierConstant(name);
            getOp = OpCode.GET_GLOBAL;
            setOp = OpCode.SET_GLOBAL;
        }

        // 接等号为赋值  反之为取值
        if (canAssign && match(TokenType.EQUAL)) {
            expression();
            emitBytes((byte) setOp.ordinal(), (byte) arg);
        } else {
            emitBytes((byte) getOp.ordinal(), (byte) arg);
        }
    }

    private void variable(boolean canAssign) {
        namedVariable(parser.previous, canAssign);
    }

    private Token syntheticToken(String text) {
        return new Token(TokenType.IDENTIFIER, 0, text.length(), 0, text);
    }

    private ObjFunction endCompiler() {

        emitReturn();
        ObjFunction function = current.function;

        if (Lox.DEBUG_PRINT_CODE) {
            if (!parser.hadError) {
                Debug.disassembleChunk(currentChunk(), function.name != null
                        ? function.name.getString() : "<script>");
            }
        }

        current = current.enclosing;
        return function;
    }

    private void beginScope() {
        current.scopeDepth++;
    }

    // 结束作用域
    private void endScope() {
        current.scopeDepth--;

        while (current.localCount > 0 &&
                current.locals[current.localCount - 1].depth > current.scopeDepth) {
            // 被捕获的需要推送到闭包
            if (current.locals[current.localCount - 1].isCaptured) {
                emitByte(OpCode.CLOSE_UPVALUE);
            } else {
                emitByte(OpCode.POP);
            }
            current.localCount--;
        }
    }

    private void block() {
        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            declaration();
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
    }

    private void function(FunctionType type) {
        Compiler compiler = new Compiler(current.scanner, type);
        beginScope();
        // 函数参数
        consume(TokenType.LEFT_PAREN, "Expect '(' after function name.");
        if (!check(TokenType.RIGHT_PAREN)) {
            do {
                current.function.arity++;
                if (current.function.arity > 255) {
                    errorAtCurrent("Can't have more than 255 parameters.");
                }
                byte constant = parseVariable("Expect parameter name.");
                defineVariable(constant);
            } while (match(TokenType.COMMA));
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.");
        consume(TokenType.LEFT_BRACE, "Expect '{' before function body.");
        block();

        ObjFunction function = endCompiler();
        emitBytes(OpCode.CLOSURE, makeConstant(function.toValue()));

        for (int i = 0; i < function.upvalueCount; i++) {
            emitByte((byte) (compiler.upvalues[i].isLocal ? 1 : 0));
            emitByte(compiler.upvalues[i].index);
        }
    }

    private void method() {
        consume(TokenType.IDENTIFIER, "Expect method name.");
        byte constant = identifierConstant(parser.previous);

        FunctionType type = FunctionType.METHOD;
        if (parser.previous.length == 4 && "init".equals(parser.previous.message)) {
            type = FunctionType.INITIALIZER;
        }
        function(type);
        emitBytes(OpCode.METHOD, constant);
    }

    private void funDeclaration() {
        byte global = parseVariable("Expect function name.");
        markInitialized();
        function(FunctionType.FUNCTION);
        defineVariable(global);
    }

    private void classDeclaration() {
        consume(TokenType.IDENTIFIER, "Expect class name.");
        Token className = parser.previous;
        byte nameConstant = identifierConstant(parser.previous);
        declareVariable();

        emitBytes(OpCode.CLASS, nameConstant);
        defineVariable(nameConstant);

        ClassCompiler classCompiler = new ClassCompiler();
        classCompiler.hasSuperclass = false;
        classCompiler.enclosing = ClassCompiler.currentClass;
        ClassCompiler.currentClass = classCompiler;

        if (match(TokenType.LESS)) {
            consume(TokenType.IDENTIFIER, "Expect superclass name.");
            variable(false);

            if (identifiersEqual(className, parser.previous)) {
                error("A class can't inherit from itself.");
            }

            beginScope();
            addLocal(syntheticToken("super"));
            defineVariable((byte) 0);

            namedVariable(className, false);
            emitByte(OpCode.INHERIT);
            classCompiler.hasSuperclass = true;
        }

        namedVariable(className, false);
        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.");
        while (!check(TokenType.RIGHT_BRACE) && !check(TokenType.EOF)) {
            method();
        }
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body.");
        emitByte(OpCode.POP);

        if (classCompiler.hasSuperclass) {
            endScope();
        }

        ClassCompiler.currentClass = ClassCompiler.currentClass.enclosing;
    }

    private void varDeclaration() {
        byte global = parseVariable("Expect variable name.");

        if (match(TokenType.EQUAL)) {
            expression();
        } else {
            emitByte(OpCode.NIL);
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");

        defineVariable(global);
    }

    private void expression() {
        parsePrecedence(Precedence.ASSIGNMENT);
    }

    private void parsePrecedence(Precedence precedence) {
        advance();
        // 获取上一格token的前缀表达式 为null的话错误
        ParseFn prefixRule = getRule(parser.previous.type).prefix;
        if (prefixRule == null) {
            error("Expect expression.");
            return;
        }
        // 执行前缀表达式  传入等号的优先级表示是否能赋值
        boolean canAssign = precedence.ordinal() <= Precedence.ASSIGNMENT.ordinal();
        prefixRule.parseFn(canAssign);
        // 获取当前token优先级 比较传递进的优先级 传递小于等于当前的话 执行中缀表达式
        while (precedence.ordinal() <= getRule(parser.current.type).precedence.ordinal()) {
            advance();
            ParseFn infixRule = getRule(parser.previous.type).infix;
            infixRule.parseFn(canAssign);
        }

        // 可以赋值且后接等号
        if (canAssign && match(TokenType.EQUAL)) {
            error("Invalid assignment target.");
        }
    }

    private ParseRule getRule(TokenType type) {
        return this.rules[type.ordinal()];
    }

    private ParseFn and_() {
        return (canAssign) -> {
            int endJump = emitJump(OpCode.JUMP_IF_FALSE);

            emitByte(OpCode.POP);
            parsePrecedence(Precedence.AND);

            patchJump(endJump);
        };
    }


    private ParseFn binary() {
        return (canAssign) -> {
            TokenType operatorType = parser.previous.type;
            ParseRule rule = getRule(operatorType);
            parsePrecedence(rule.precedence.offset(1));

            switch (operatorType) {
                case BANG_EQUAL:
                    emitBytes(OpCode.EQUAL, OpCode.NOT);
                    break;
                case EQUAL_EQUAL:
                    emitByte(OpCode.EQUAL);
                    break;
                case GREATER:
                    emitByte(OpCode.GREATER);
                    break;
                case GREATER_EQUAL:
                    emitBytes(OpCode.LESS, OpCode.NOT);
                    break;
                case LESS:
                    emitByte(OpCode.LESS);
                    break;
                case LESS_EQUAL:
                    emitBytes(OpCode.GREATER, OpCode.NOT);
                    break;
                case PLUS:
                    emitByte(OpCode.ADD);
                    break;
                case MINUS:
                    emitByte(OpCode.SUBTRACT);
                    break;
                case STAR:
                    emitByte(OpCode.MULTIPLY);
                    break;
                case SLASH:
                    emitByte(OpCode.DIVIDE);
                    break;
                default:
                    return; // Unreachable.
            }
        };
    }


    private ParseFn call() {
        return (canAssign) -> {
            byte argCount = argumentList();
            emitBytes(OpCode.CALL, argCount);
        };
    }


    private ParseFn dot() {
        return (canAssign) -> {
            consume(TokenType.IDENTIFIER, "Expect property name after '.'.");
            byte name = identifierConstant(parser.previous);

            if (canAssign && match(TokenType.EQUAL)) {
                expression();
                emitBytes(OpCode.SET_PROPERTY, name);
            } else if (match(TokenType.LEFT_PAREN)) {
                byte argCount = argumentList();
                emitBytes(OpCode.INVOKE, name);
                emitByte(argCount);
            } else {
                emitBytes(OpCode.GET_PROPERTY, name);
            }
        };
    }


    private ParseFn literal() {
        return (canAssign) -> {
            switch (parser.previous.type) {
                case FALSE:
                    emitByte(OpCode.FALSE);
                    break;
                case NIL:
                    emitByte(OpCode.NIL);
                    break;
                case TRUE:
                    emitByte(OpCode.TRUE);
                    break;
                default:
                    return; // Unreachable.
            }
        };
    }


    private ParseFn grouping() {
        return (canAssign) -> {
            expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
        };
    }


    private ParseFn number() {
        return (canAssign) -> {
            double value = Double.parseDouble(parser.previous.message);
            emitConstant(new Value(ValueType.NUMBER, value));
        };
    }


    private ParseFn or_() {
        return (canAssign) -> {
            int elseJump = emitJump(OpCode.JUMP_IF_FALSE);
            int endJump = emitJump(OpCode.JUMP);

            patchJump(elseJump);
            emitByte(OpCode.POP);

            parsePrecedence(Precedence.OR);
            patchJump(endJump);
        };
    }


    private ParseFn string() {
        return (canAssign) -> {
            emitConstant(ObjString.copyString(parser.previous.message).toValue());
        };
    }

    private ParseFn variable() {
        return (canAssign) -> {
            namedVariable(parser.previous, canAssign);
        };
    }


    private ParseFn super_() {
        return (canAssign) -> {
            if (ClassCompiler.currentClass == null) {
                error("Can't use 'super' outside of a class.");
            } else if (!ClassCompiler.currentClass.hasSuperclass) {
                error("Can't use 'super' in a class with no superclass.");
            }

            consume(TokenType.DOT, "Expect '.' after 'super'.");
            consume(TokenType.IDENTIFIER, "Expect superclass method name.");
            byte name = identifierConstant(parser.previous);

            namedVariable(syntheticToken("this"), false);
            if (match(TokenType.LEFT_PAREN)) {
                byte argCount = argumentList();
                namedVariable(syntheticToken("super"), false);
                emitBytes(OpCode.SUPER_INVOKE, name);
                emitByte(argCount);
            } else {
                namedVariable(syntheticToken("super"), false);
                emitBytes(OpCode.GET_SUPER, name);
            }
        };
    }

    private ParseFn this_() {
        return (canAssign) -> {
            if (ClassCompiler.currentClass == null) {
                error("Can't use 'this' outside of a class.");
                return;
            }
            variable(false);
        };
    }

    private ParseFn unary() {
        return (canAssign) -> {
            TokenType operatorType = parser.previous.type;

            // Compile the operand.
            parsePrecedence(Precedence.UNARY);

            // Emit the operator instruction.
            switch (operatorType) {
                case BANG:
                    emitByte(OpCode.NOT);
                    break;
                case MINUS:
                    emitByte(OpCode.NEGATE);
                    break;
                default:
                    return; // Unreachable.
            }
        };
    }
}

