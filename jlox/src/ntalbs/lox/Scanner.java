package ntalbs.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ntalbs.lox.TokenType.AND;
import static ntalbs.lox.TokenType.BANG;
import static ntalbs.lox.TokenType.BANG_EQUAL;
import static ntalbs.lox.TokenType.CLASS;
import static ntalbs.lox.TokenType.COMMA;
import static ntalbs.lox.TokenType.DOT;
import static ntalbs.lox.TokenType.ELSE;
import static ntalbs.lox.TokenType.EOF;
import static ntalbs.lox.TokenType.EQUAL;
import static ntalbs.lox.TokenType.EQUAL_EQUAL;
import static ntalbs.lox.TokenType.FALSE;
import static ntalbs.lox.TokenType.FOR;
import static ntalbs.lox.TokenType.FUN;
import static ntalbs.lox.TokenType.GREATER;
import static ntalbs.lox.TokenType.GREATER_EQUAL;
import static ntalbs.lox.TokenType.IDENTIFIER;
import static ntalbs.lox.TokenType.IF;
import static ntalbs.lox.TokenType.LEFT_BRACE;
import static ntalbs.lox.TokenType.LEFT_PAREN;
import static ntalbs.lox.TokenType.LESS;
import static ntalbs.lox.TokenType.LESS_EQUAL;
import static ntalbs.lox.TokenType.MINUS;
import static ntalbs.lox.TokenType.NIL;
import static ntalbs.lox.TokenType.NUMBER;
import static ntalbs.lox.TokenType.OR;
import static ntalbs.lox.TokenType.PLUS;
import static ntalbs.lox.TokenType.PRINT;
import static ntalbs.lox.TokenType.RETURN;
import static ntalbs.lox.TokenType.RIGHT_BRACE;
import static ntalbs.lox.TokenType.RIGHT_PAREN;
import static ntalbs.lox.TokenType.SEMICOLON;
import static ntalbs.lox.TokenType.STAR;
import static ntalbs.lox.TokenType.STRING;
import static ntalbs.lox.TokenType.SUPER;
import static ntalbs.lox.TokenType.THIS;
import static ntalbs.lox.TokenType.TRUE;
import static ntalbs.lox.TokenType.VAR;
import static ntalbs.lox.TokenType.WHILE;

public class Scanner {
  private static final Map<String, TokenType> keywards;
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0;
  private int current = 0;
  private int line = 1;

  static {
    keywards = new HashMap<>();
    keywards.put("and", AND);
    keywards.put("class", CLASS);
    keywards.put("else", ELSE);
    keywards.put("false", FALSE);
    keywards.put("for", FOR);
    keywards.put("fun", FUN);
    keywards.put("if", IF);
    keywards.put("nil", NIL);
    keywards.put("or", OR);
    keywards.put("print", PRINT);
    keywards.put("return", RETURN);
    keywards.put("super", SUPER);
    keywards.put("this", THIS);
    keywards.put("true", TRUE);
    keywards.put("var", VAR);
    keywards.put("while", WHILE);
  }

  public Scanner(String source) {
    this.source = source;
  }

  public List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }
    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(LEFT_BRACE);
        break;
      case '}':
        addToken(RIGHT_BRACE);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        addToken(DOT);
        break;
      case '-':
        addToken(MINUS);
        break;
      case '+':
        addToken(PLUS);
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '*':
        addToken(STAR);
        break;
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      case '/':
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        }
      case ' ':
      case '\t':
      case '\r':
        // Ignore whitespace
        break;
      case '\n':
        line++;
        break;
      case '"':
        string();
        break;
      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
        }
        break;
    }
  }

  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z')
      || (c >= 'A' && c <= 'Z')
      || c == '_';
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private void identifier() {
    while (isAlpha(peek())) advance();
    String text = source.substring(start, current);
    TokenType type = keywards.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }

  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }
    if (isAtEnd()) {
      Lox.error(line, "Unterminated string.");
    }
    advance();
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }

  private void number() {
    while (isDigit(peek())) advance();
    if (peek() == '.' && isDigit(peekNext())) {
      advance();
    }
    while (isDigit(peek())) advance();
    addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }

  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;
    current++;
    return true;
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

  private boolean isAtEnd() {
    return current >= source.length();
  }
}
