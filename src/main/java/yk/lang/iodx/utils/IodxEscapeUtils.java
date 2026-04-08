package yk.lang.iodx.utils;

import yk.ycollections.YMap;

import java.io.StringWriter;

import static yk.ycollections.YHashMap.hm;

public class IodxEscapeUtils {
    private static final YMap<Character, Character> ESCAPES = hm('\t', 't', '\b', 'b', '\r', 'r', '\f', 'f', '\\', '\\');
    public static final YMap<Character, Character> ESCAPES_SQ = ESCAPES.with('\'', '\'');
    public static final YMap<Character, Character> ESCAPES_DQ = ESCAPES.with('\"', '\"');
    public static final YMap<Character, Character> UNESCAPES = ESCAPES.map((k, v) -> v, (k, v) -> k)
        //unescapes handle more permissive symbols than escapes produce, hence non-symmetry
        .with('n', '\n', '\"', '\"', '\'', '\'');

    private static String stripQuotes(String s) {
        return s.substring(1, s.length() - 1);
    }

    public static String unescapeDoubleQuotes(String s) {
        return unescape(stripQuotes(s));
    }

    public static String unescapeSingleQuotes(String s) {
        return unescape(stripQuotes(s));
    }

    public static String escapeDoubleQuotes(String s) {
        return escape(s, ESCAPES_DQ);
    }

    public static String escapeSingleQuotes(String s) {
        return escape(s, ESCAPES_SQ);
    }

    public static String unescape(String input) {
        StringWriter out = new StringWriter();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\r') continue;
            if (c == '\\') {
                if (++i >= input.length()) throw new RuntimeException("Uncompleted escape sequence");
                c = input.charAt(i);
                Character result = UNESCAPES.get(c);
                if (result == null) throw new RuntimeException("Unknown escape symbol: " + (int)c);
                c = result;
            }
            out.write(c);
        }
        return out.toString();
    }

    public static String escape(String input, YMap<Character, Character> toEscape) {
        StringWriter out = new StringWriter();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (toEscape.containsKey(c)) out.write("\\" + toEscape.get(c));
            else out.write(c);
        }
        return out.toString();
    }
}
