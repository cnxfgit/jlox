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

    private boolean match(char expected) {
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

    private TokenType checkKeyword(int start, int length, String rest, TokenType type) {
        if (rest.equals(source.substring(start, start + length))) {
            return type;
        }
        return TokenType.IDENTIFIER;
    }

    private TokenType identifierType() {
        switch (source.charAt(start)) {
            case 'a':
                return checkKeyword(1, 2, "nd", TokenType.AND);
            case 'c':
                return checkKeyword(1, 4, "lass", TokenType.CLASS);
            case 'e':
                return checkKeyword(1, 3, "lse", TokenType.ELSE);
            case 'f':
                if (this.current - this.start > 1) {
                    switch (source.charAt(start + 1)) {
                        case 'a':
                            return checkKeyword(2, 3, "lse", TokenType.FALSE);
                        case 'o':
                            return checkKeyword(2, 1, "r", TokenType.FOR);
                        case 'u':
                            return checkKeyword(2, 1, "n", TokenType.FUN);
                    }
                }
                break;
            case 'i':
                return checkKeyword(1, 1, "f", TokenType.IF);
            case 'n':
                return checkKeyword(1, 2, "il", TokenType.NIL);
            case 'o':
                return checkKeyword(1, 1, "r", TokenType.OR);
            case 'p':
                return checkKeyword(1, 4, "rint", TokenType.PRINT);
            case 'r':
                return checkKeyword(1, 5, "eturn", TokenType.RETURN);
            case 's':
                return checkKeyword(1, 4, "uper", TokenType.SUPER);
            case 't':
                if (this.current - this.start > 1) {
                    switch (source.charAt(start + 1)) {
                        case 'h':
                            return checkKeyword(2, 2, "is", TokenType.THIS);
                        case 'r':
                            return checkKeyword(2, 2, "ue", TokenType.TRUE);
                    }
                }
                break;
            case 'v':
                return checkKeyword(1, 2, "ar", TokenType.VAR);
            case 'w':
                return checkKeyword(1, 4, "hile", TokenType.WHILE);
        }
        return TokenType.IDENTIFIER;
    }

    private Token identifier() {
        while (isAlpha(peek()) || isDigit(peek())) advance();
        return new Token(identifierType(), start, current, line, source.substring(start, current));
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
        return new Token(TokenType.STRING, start, current, line, source.substring(start, current));
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
