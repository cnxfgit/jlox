package com.craftinginterpreters.lox.compiler;

import com.craftinginterpreters.lox.scanner.Token;

/**
 * @author hlx
 * @date 2023-08-05
 */
public class Local {

    private Token name;

    private int depth;

    private boolean isCaptured;

    public Local(int depth, boolean isCaptured) {
        this.name = null;
        this.depth = depth;
        this.isCaptured = isCaptured;
    }

    public Token getName() {
        return name;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isCaptured() {
        return isCaptured;
    }

    public void setName(Token name) {
        this.name = name;
    }

    public void setCaptured(boolean captured) {
        isCaptured = captured;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
