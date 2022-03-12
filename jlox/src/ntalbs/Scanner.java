package ntalbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ntalbs.TokenType.AND;
import static ntalbs.TokenType.BANG;
import static ntalbs.TokenType.BANG_EQUAL;
import static ntalbs.TokenType.CLASS;
import static ntalbs.TokenType.COMMA;
import static ntalbs.TokenType.DOT;
import static ntalbs.TokenType.ELSE;
import static ntalbs.TokenType.EOF;
import static ntalbs.TokenType.EQUAL;
import static ntalbs.TokenType.EQUAL_EQUAL;
import static ntalbs.TokenType.FALSE;
import static ntalbs.TokenType.FOR;
import static ntalbs.TokenType.FUN;
import static ntalbs.TokenType.GREATER;
import static ntalbs.TokenType.GREATER_EQUAL;
import static ntalbs.TokenType.IDENTIFIER;
import static ntalbs.TokenType.IF;
import static ntalbs.TokenType.LEFT_BRACE;
import static ntalbs.TokenType.LEFT_PAREN;
import static ntalbs.TokenType.LESS;
import static ntalbs.TokenType.LESS_EQUAL;
import static ntalbs.TokenType.MINUS;
import static ntalbs.TokenType.NIL;
import static ntalbs.TokenType.NUMBER;
import static ntalbs.TokenType.OR;
import static ntalbs.TokenType.PLUS;
import static ntalbs.TokenType.PRINT;
import static ntalbs.TokenType.RETURN;
import static ntalbs.TokenType.RIGHT_BRACE;
import static ntalbs.TokenType.RIGHT_PAREN;
import static ntalbs.TokenType.SEMICOLON;
import static ntalbs.TokenType.STAR;
import static ntalbs.TokenType.STRING;
import static ntalbs.TokenType.SUPER;
import static ntalbs.TokenType.THIS;
import static ntalbs.TokenType.TRUE;
import static ntalbs.TokenType.VAR;
import static ntalbs.TokenType.WHILE;

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
