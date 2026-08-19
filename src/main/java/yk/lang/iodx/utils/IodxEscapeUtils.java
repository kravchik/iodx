package yk.lang.iodx.utils;

import yk.ycollections.YMap;

import static yk.ycollections.YHashMap.hm;

public class IodxEscapeUtils {
    private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();
    private static final YMap<Character, Character> ESCAPES = hm('\t', 't', '\b', 'b', '\r', 'r', '\f', 'f', '\\', '\\');
    public static final YMap<Character, Character> ESCAPES_SQ = ESCAPES.with('\'', '\'');
    public static final YMap<Character, Character> ESCAPES_DQ = ESCAPES.with('\"', '\"');
    public static final YMap<Character, Character> UNESCAPES = ESCAPES.map((k, v) -> v, (k, v) -> k)
        //unescapes handle more permissive symbols than escapes produce, hence non-symmetry
        .with('n', '\n', 's', ' ', '\"', '\"', '\'', '\'');

    private static String stripQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    public static String unescapeQuoted(String s) {
        return unescape(stripQuotes(s));
    }

    public static String unescapeDoubleQuotes(String s) {
        return unescapeQuoted(s);
    }

    public static String unescapeSingleQuotes(String s) {
        return unescapeQuoted(s);
    }

    public static String escapeDoubleQuotes(String s) {
        return escape(s, ESCAPES_DQ);
    }

    public static String escapeSingleQuotes(String s) {
        return escape(s, ESCAPES_SQ);
    }

    public static String unescape(String input) {
        StringBuilder out = new StringBuilder(input.length());

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\r') continue;
            if (c == '\\') {
                int escapeOffset = i;
                if (++i >= input.length()) {
                    throw new IodxEscapeException("Uncompleted escape sequence", escapeOffset, 1);
                }
                char escapeSymbol = input.charAt(i);
                if (escapeSymbol == 'u') {
                    char unicode = parseUnicodeEscape(input, escapeOffset);
                    i = escapeOffset + 5;

                    if (Character.isHighSurrogate(unicode)) {
                        int lowSurrogateOffset = i + 1;
                        if (!startsUnicodeEscape(input, lowSurrogateOffset)) {
                            throw new IodxEscapeException(
                                "High surrogate must be followed by a low surrogate escape",
                                escapeOffset, 6);
                        }
                        char lowSurrogate = parseUnicodeEscape(input, lowSurrogateOffset);
                        if (!Character.isLowSurrogate(lowSurrogate)) {
                            throw new IodxEscapeException(
                                "Expected a low surrogate escape", lowSurrogateOffset, 6);
                        }
                        out.append(unicode);
                        out.append(lowSurrogate);
                        i = lowSurrogateOffset + 5;
                        continue;
                    }
                    if (Character.isLowSurrogate(unicode)) {
                        throw new IodxEscapeException("Unexpected low surrogate", escapeOffset, 6);
                    }
                    c = unicode;
                } else {
                    Character result = UNESCAPES.get(escapeSymbol);
                    if (result == null) {
                        throw new IodxEscapeException(
                            "Unknown escape symbol: " + escapeSymbol, escapeOffset, 2);
                    }
                    c = result;
                }
            } else if (Character.isHighSurrogate(c)) {
                if (i + 1 >= input.length() || !Character.isLowSurrogate(input.charAt(i + 1))) {
                    throw new IodxEscapeException("Lone high surrogate", i, 1);
                }
                out.append(c);
                out.append(input.charAt(++i));
                continue;
            } else if (Character.isLowSurrogate(c)) {
                throw new IodxEscapeException("Lone low surrogate", i, 1);
            }
            out.append(c);
        }
        return out.toString();
    }

    public static String escape(String input, YMap<Character, Character> toEscape) {
        StringBuilder out = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isHighSurrogate(c)) {
                if (i + 1 >= input.length() || !Character.isLowSurrogate(input.charAt(i + 1))) {
                    throw new IllegalArgumentException("Lone high surrogate at offset " + i);
                }
                out.append(c);
                out.append(input.charAt(++i));
            } else if (Character.isLowSurrogate(c)) {
                throw new IllegalArgumentException("Lone low surrogate at offset " + i);
            } else if (toEscape.containsKey(c)) {
                out.append('\\').append(toEscape.get(c));
            } else if (c != '\n' && Character.isISOControl(c)) {
                writeUnicodeEscape(out, c);
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static boolean startsUnicodeEscape(String input, int offset) {
        return offset + 1 < input.length() && input.charAt(offset) == '\\' && input.charAt(offset + 1) == 'u';
    }

    private static char parseUnicodeEscape(String input, int offset) {
        int availableLength = input.length() - offset;
        if (availableLength < 6) {
            throw new IodxEscapeException("Incomplete Unicode escape", offset, availableLength);
        }

        int value = 0;
        for (int i = offset + 2; i < offset + 6; i++) {
            int digit = hexToInt(input.charAt(i));
            if (digit < 0) {
                throw new IodxEscapeException("Invalid hexadecimal digit in Unicode escape", offset, 6);
            }
            value = value * 16 + digit;
        }
        return (char) value;
    }

    private static int hexToInt(char c) {
        if (c >= '0' && c <= '9') return c - '0';
        if (c >= 'a' && c <= 'f') return c - 'a' + 10;
        if (c >= 'A' && c <= 'F') return c - 'A' + 10;
        return -1;
    }

    private static void writeUnicodeEscape(StringBuilder out, char c) {
        out.append('\\');
        out.append('u');
        out.append(HEX_DIGITS[(c >>> 12) & 0xF]);
        out.append(HEX_DIGITS[(c >>> 8) & 0xF]);
        out.append(HEX_DIGITS[(c >>> 4) & 0xF]);
        out.append(HEX_DIGITS[c & 0xF]);
    }
}
