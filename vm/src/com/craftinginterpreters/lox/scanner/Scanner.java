package com.craftinginterpreters.lox.scanner;

/**
 * @author hlx
 * @date 2023-07-27
 */
public class Scanner {

    private int start;
    private int current;
    private int line;
    private final String source;

    public Scanner(String source) {
        start = 0;
        current = 0;
        line = 1;
        this.source = source;
    }

    private static boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    public boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private void skipWhitespace() {
        for (; ; ) {
            char c = peek();
            switch (c) {
                case ' ':
                case '\r':
                case '\t':
                    advance();
                    break;
                case '\n':
                    this.line++;
                    advance();
                    break;
                case '/':
                    if (peekNext() == '/') {
                        // A comment goes until the end of the line.
                        while (peek() != '\n' && !isAtEnd()) advance();
                    } else {
                        return;
                    }
                    break;
                default:
                    return;
            }
        }
    }

    private TokenType checkKeyword(String rest, TokenType type) {
        if (rest.length() == current - start && rest.equals(source.substring(start, start + rest.length()))) {
            return type;
        }
        return TokenType.IDENTIFIER;
    }

    private TokenType identifierType() {
        switch (source.charAt(start)) {
            case 'a':
                return checkKeyword("and", TokenType.AND);
            case 'c':
                return checkKeyword("class", TokenType.CLASS);
            case 'e':
                return checkKeyword("else", TokenType.ELSE);
            case 'f':
                if (this.current - this.start > 1) {
                    switch (source.charAt(start + 1)) {
                        case 'a':
                            return checkKeyword("false", TokenType.FALSE);
                        case 'o':
                            return checkKeyword("for", TokenType.FOR);
                        case 'u':
                            return checkKeyword("fun", TokenType.FUN);
                    }
                }
                break;
            case 'i':
                return checkKeyword("if", TokenType.IF);
            case 'n':
                return checkKeyword("nil", TokenType.NIL);
            case 'o':
                return checkKeyword("or", TokenType.OR);
            case 'p':
                return checkKeyword("print", TokenType.PRINT);
            case 'r':
                return checkKeyword("return", TokenType.RETURN);
            case 's':
                return checkKeyword("super", TokenType.SUPER);
            case 't':
                if (this.current - this.start > 1) {
                    switch (source.charAt(start + 1)) {
                        case 'h':
                            return checkKeyword("this", TokenType.THIS);
                        case 'r':
                            return checkKeyword("true", TokenType.TRUE);
                    }
                }
                break;
            case 'v':
                return checkKeyword("var", TokenType.VAR);
            case 'w':
                return checkKeyword("while", TokenType.WHILE);
        }
        return TokenType.IDENTIFIER;
    }

    private Token identifier() {
        while (isAlpha(peek()) || isDigit(peek())) advance();
        return new Token(identifierType(), start, current - start, line, source.substring(start, current));
    }

    private Token number() {
        while (isDigit(peek())) advance();

        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the ".".
            advance();

            while (isDigit(peek())) advance();
        }

        return new Token(TokenType.NUMBER, start, current, line, source.substring(start, current));
    }

    private Token string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') this.line++;
            advance();
        }

        if (isAtEnd()) return new Token("Unterminated string.", line);

        // The closing quote.
        advance();
        return new Token(TokenType.STRING, start, current - start - 2, line, source.substring(start + 1, current - 1));
    }

    public Token scanToken() {
        skipWhitespace();

        this.start = this.current;
        // 重新标记扫描仪起点并检查源代码是否结束
        if (isAtEnd()) return new Token(TokenType.EOF, start, current, line, source.substring(start, current));

        char c = advance();
        if (isAlpha(c)) return identifier();
        if (isDigit(c)) return number();

        switch (c) {
            case '(':
                return new Token(TokenType.LEFT_PAREN, start, current, line, source.substring(start, current));
            case ')':
                return new Token(TokenType.RIGHT_PAREN, start, current, line, source.substring(start, current));
            case '{':
                return new Token(TokenType.LEFT_BRACE, start, current, line, source.substring(start, current));
            case '}':
                return new Token(TokenType.RIGHT_BRACE, start, current, line, source.substring(start, current));
            case ';':
                return new Token(TokenType.SEMICOLON, start, current, line, source.substring(start, current));
            case ',':
                return new Token(TokenType.COMMA, start, current, line, source.substring(start, current));
            case '.':
                return new Token(TokenType.DOT, start, current, line, source.substring(start, current));
            case '-':
                return new Token(TokenType.MINUS, start, current, line, source.substring(start, current));
            case '+':
                return new Token(TokenType.PLUS, start, current, line, source.substring(start, current));
            case '/':
                return new Token(TokenType.SLASH, start, current, line, source.substring(start, current));
            case '*':
                return new Token(TokenType.STAR, start, current, line, source.substring(start, current));
            case '!':
                return new Token(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG,
                        start, current, line, source.substring(start, current));
            case '=':
                return new Token(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL,
                        start, current, line, source.substring(start, current));
            case '<':
                return new Token(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS,
                        start, current, line, source.substring(start, current));
            case '>':
                return new Token(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER,
                        start, current, line, source.substring(start, current));
            case '"':
                return string();
        }

        return new Token("Unexpected character.", line);
    }

}
