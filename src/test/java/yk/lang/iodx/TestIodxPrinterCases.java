package yk.lang.iodx;

import org.junit.Test;
import yk.lang.iodx.congocc.IodxCstParser;
import yk.lang.iodx.utils.BadException;
import yk.lang.iodx.utils.Reflector;
import yk.ycollections.Tuple;
import yk.ycollections.YList;
import yk.ycollections.YMap;
import yk.ycollections.YSet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static yk.ycollections.YHashSet.hs;

public class TestIodxPrinterCases {
    private static final YSet<String> INT_SETTINGS = hs("maxWidth", "maxLocalWidth", "compactFromLevel");

    private enum ScanState {
        NORMAL, SINGLE_QUOTED, DOUBLE_QUOTED, LINE_COMMENT, BLOCK_COMMENT
    }

    @Test
    public void testCases() {
        YMap<String, Integer> settings = INT_SETTINGS.toMap(s -> s, s -> Reflector.get(new IodxPrinter(), s));

        YList<Object> yl = Iodx.readIodxEntities(UtilsForTests.readResource("formatting.cases.sql.style.iodx"));
        for (Object o : yl) {
            if (o instanceof Tuple) {
                Tuple t = (Tuple) o;
                if (INT_SETTINGS.contains(t.a)) settings.put((String) t.a, extractInt(t));
                else BadException.notImplemented(o + "");
            } else if (o instanceof String) {
                String canonical = (String) o;
                assertEquals("Canonical formatting should be stable", canonical, format(canonical, settings));

                String[] variants = {
                    replaceWhitespaceOutsideLiteralsAndComments(canonical, " "),
                    replaceWhitespaceOutsideLiteralsAndComments(canonical, "\n    ")
                };
                for (int i = 0; i < variants.length; i++) {
                    assertEquals("Whitespace variant " + (i + 1) + " should use canonical formatting",
                        canonical, format(variants[i], settings));
                }
            }
        }
    }

    private static String format(String source, YMap<String, Integer> settings) {
        IodxPrinter printer = new IodxPrinter();
        for (Map.Entry<String, Integer> entry : settings.entrySet()) {
            Reflector.set(printer, entry.getKey(), entry.getValue());
        }
        Object entity = IodxEntityFromCst.translate(IodxCstParser.parse(source).children)
            .assertSize(1).first();
        return "\n" + printer.print(entity) + "\n";
    }

    private static String replaceWhitespaceOutsideLiteralsAndComments(String text, String replacement) {
        StringBuilder result = new StringBuilder();
        ScanState state = ScanState.NORMAL;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (state == ScanState.SINGLE_QUOTED || state == ScanState.DOUBLE_QUOTED) {
                result.append(c);
                if (c == '\\' && i + 1 < text.length()) result.append(text.charAt(++i));
                else if (state == ScanState.SINGLE_QUOTED && c == '\'') state = ScanState.NORMAL;
                else if (state == ScanState.DOUBLE_QUOTED && c == '"') state = ScanState.NORMAL;
                continue;
            }

            if (state == ScanState.LINE_COMMENT) {
                result.append(c);
                if (c == '\n' || c == '\r') state = ScanState.NORMAL;
                continue;
            }

            if (state == ScanState.BLOCK_COMMENT) {
                result.append(c);
                if (c == '*' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                    result.append(text.charAt(++i));
                    state = ScanState.NORMAL;
                }
                continue;
            }

            if (c == '\'' || c == '"') {
                state = c == '\'' ? ScanState.SINGLE_QUOTED : ScanState.DOUBLE_QUOTED;
                result.append(c);
            } else if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
                state = ScanState.LINE_COMMENT;
                result.append(c).append(text.charAt(++i));
            } else if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
                state = ScanState.BLOCK_COMMENT;
                result.append(c).append(text.charAt(++i));
            } else if (Character.isWhitespace(c)) {
                while (i + 1 < text.length() && Character.isWhitespace(text.charAt(i + 1))) i++;
                result.append(replacement);
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private static int extractInt(Tuple t) {
        return ((Number) t.b).intValue();
    }

    public static String resourceAsString(String name) {
        return streamToString(resourceAsStream(name));
    }
    public static InputStream resourceAsStream(String name) {
        return TestIodxPrinterCases.class.getClassLoader().getResourceAsStream(name);
    }
    public static String streamToString(InputStream in) {
        if (in == null) return null;
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String l;
        StringBuilder sb = new StringBuilder();
        try {
            while((l = br.readLine()) != null) sb.append(l).append("\n");
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
