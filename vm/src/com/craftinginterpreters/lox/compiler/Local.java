package com.craftinginterpreters.lox.compiler;

import com.craftinginterpreters.lox.scanner.Token;

/**
 * @author hlx
 * @date 2023-08-05
 */
public class Local {

    Token name;

    int depth;

    boolean isCaptured;

    public Local(int depth, boolean isCaptured) {
        this.name = null;
        this.depth = depth;
        this.isCaptured = isCaptured;
    }

}
