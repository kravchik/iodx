package yk.lang.iodx;

import org.junit.Test;
import yk.lang.iodx.congocc.IodxCstParser;
import yk.lang.iodx.congocc.ParseException;
import yk.lang.iodx.congocc.Token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class TestIodxUnicodeEscapes {
    private static final String UNICODE_ESCAPE_PREFIX = "\\" + "u";

    @Test
    public void testJsonStyleEscapesInBothQuoteStyles() {
        assertEquals("A", parseString(doubleQuoted(unicodeEscape("0041"))));
        assertEquals("Ж", parseString(singleQuoted(unicodeEscape("0416"))));
        assertEquals("é", parseString(doubleQuoted(unicodeEscape("00e9"))));
        assertEquals("é", parseString(doubleQuoted(unicodeEscape("00E9"))));

        assertEquals("A1", parseString(doubleQuoted(unicodeEscape("0041") + "1")));
        assertEquals(new String(new char[]{0x0000, 0x000D}),
            parseString(doubleQuoted(unicodeEscape("0000") + unicodeEscape("000D"))));
    }

    @Test
    public void testEscapedControlsDoNotChangeTokenStructure() {
        String source = doubleQuoted(
            "before" + unicodeEscape("0022") + unicodeEscape("005C") + unicodeEscape("000A") + "after");
        IodxCst parsed = IodxCstParser.parse(source);

        assertEquals(1, parsed.children.size());
        assertEquals("before\"\\\nafter", parsed.children.first().value);
        assertPosition(parsed.children.first(), 0, source.length());
    }

    @Test
    public void testUnicodeEscapesAreLimitedToQuotedStrings() {
        String escapedA = unicodeEscape("0041");
        IodxCst comment = IodxCstParser.parse("// " + escapedA).children.first();
        assertEquals("// " + escapedA, comment.value);

        assertThrows(ParseException.class, () -> IodxCstParser.parse(escapedA));
    }

    @Test
    public void testRawPrintableUnicodeRemainsUnchanged() {
        String value = "Кириллица и emoji 😀";
        assertEquals(value, parseString(singleQuoted(value)));
    }

    @Test
    public void testSupplementaryCodePointFromSurrogatePair() {
        String escapedEmoji = unicodeEscape("D83D") + unicodeEscape("DE00");
        assertEquals("😀", parseString(doubleQuoted(escapedEmoji)));
    }

    @Test
    public void testInvalidSurrogateEscapesAreRejected() {
        assertUnicodeError(doubleQuoted(unicodeEscape("D83D")), 1, 6, 1, 2,
            "High surrogate must be followed by a low surrogate escape");
        assertUnicodeError(doubleQuoted(unicodeEscape("DE00")), 1, 6, 1, 2,
            "Unexpected low surrogate");

        String reversed = unicodeEscape("DE00") + unicodeEscape("D83D");
        assertUnicodeError(doubleQuoted(reversed), 1, 6, 1, 2, "Unexpected low surrogate");

        String invalidPair = unicodeEscape("D83D") + unicodeEscape("0041");
        assertUnicodeError(doubleQuoted(invalidPair), 7, 6, 1, 8,
            "Expected a low surrogate escape");

        assertUnicodeError(doubleQuoted(unicodeEscape("D83D") + "x"), 1, 6, 1, 2,
            "High surrogate must be followed by a low surrogate escape");

        String malformedPair = unicodeEscape("D83D") + unicodeEscape("12G4");
        assertUnicodeError(doubleQuoted(malformedPair), 7, 6, 1, 8,
            "Invalid hexadecimal digit in Unicode escape");

        String incompletePair = unicodeEscape("D83D") + unicodeEscape("12");
        assertUnicodeError(doubleQuoted(incompletePair), 7, 4, 1, 8,
            "Incomplete Unicode escape");
    }

    @Test
    public void testRawLoneSurrogatesAreRejectedByParser() {
        assertUnicodeError(doubleQuoted(String.valueOf((char) 0xD800)), 1, 1, 1, 2,
            "Lone high surrogate");
        assertUnicodeError(doubleQuoted(String.valueOf((char) 0xDC00)), 1, 1, 1, 2,
            "Lone low surrogate");
    }

    @Test
    public void testEscapedBackslashKeepsUnicodeLookingTextLiteral() {
        String escapedA = unicodeEscape("0041");
        assertEquals(escapedA, parseString(doubleQuoted("\\" + escapedA)));
    }

    @Test
    public void testMalformedUnicodeEscapesAreRejectedWithExactPositions() {
        assertUnicodeError(doubleQuoted(unicodeEscape("12")), 1, 4, 1, 2,
            "Incomplete Unicode escape");
        assertUnicodeError(doubleQuoted(unicodeEscape("12G4")), 1, 6, 1, 2,
            "Invalid hexadecimal digit in Unicode escape");
        assertUnicodeError(doubleQuoted("\\U0041"), 1, 2, 1, 2,
            "Unknown escape symbol: U");
        assertUnicodeError(doubleQuoted("\\uu0041"), 1, 6, 1, 2,
            "Invalid hexadecimal digit in Unicode escape");

        String prefix = "Foo(\n  \"ok";
        String source = prefix + unicodeEscape("12G4") + "\")";
        assertUnicodeError(source, prefix.length(), 6, 2, 6,
            "Invalid hexadecimal digit in Unicode escape");
    }

    @Test
    public void testValidEscapePreservesOriginalTokenOffsets() {
        String source = "\n  " + doubleQuoted("x" + unicodeEscape("0041") + "y");
        IodxCst string = IodxCstParser.parse(source).children.first();

        assertEquals("xAy", string.value);
        assertPosition(string, 3, source.length());
        assertEquals(2, string.caret.beginLine);
        assertEquals(3, string.caret.beginColumn);
    }

    @Test
    public void testPrinterKeepsPrintableUnicodeLiteral() {
        assertEquals("'Ж 😀'", Iodx.printIodxEntity("Ж 😀"));
    }

    @Test
    public void testPrinterKeepsExistingCanonicalShortEscapesAndLiteralNewlines() {
        assertEquals("'\\t\\b\\r\\f'", Iodx.printIodxEntity("\t\b\r\f"));
        assertEquals("'first\nsecond'", Iodx.printIodxEntity("first\nsecond"));
    }

    @Test
    public void testPrinterUsesUnicodeEscapesForOtherControlCharacters() {
        String value = new String(new char[]{0x0000, 0x0001, 0x007F, 0x0085});
        String expected = "'" + unicodeEscape("0000") + unicodeEscape("0001")
            + unicodeEscape("007F") + unicodeEscape("0085") + "'";
        assertEquals(expected, Iodx.printIodxEntity(value));
    }

    @Test
    public void testPrinterRejectsLoneSurrogateCodeUnits() {
        IllegalArgumentException high = assertThrows(IllegalArgumentException.class,
            () -> Iodx.printIodxEntity(String.valueOf((char) 0xD800)));
        assertTrue(high.getMessage().contains("Lone high surrogate"));

        IllegalArgumentException low = assertThrows(IllegalArgumentException.class,
            () -> Iodx.printIodxEntity(String.valueOf((char) 0xDC00)));
        assertTrue(low.getMessage().contains("Lone low surrogate"));
    }

    @Test
    public void testUnicodeRoundTrip() {
        String value = new String(new char[]{0x0000, 0x0001}) + " Ж 😀\n\t";
        String printed = Iodx.printIodxEntity(value);

        assertEquals(value, Iodx.readIodxEntity(printed));
        assertEquals(printed, Iodx.printIodxEntity(Iodx.readIodxEntity(printed)));
    }

    private static String parseString(String source) {
        IodxCst parsed = IodxCstParser.parse(source);
        assertEquals(1, parsed.children.size());
        return (String) parsed.children.first().value;
    }

    private static String unicodeEscape(String digits) {
        return UNICODE_ESCAPE_PREFIX + digits;
    }

    private static String singleQuoted(String content) {
        return "'" + content + "'";
    }

    private static String doubleQuoted(String content) {
        return "\"" + content + "\"";
    }

    private static void assertPosition(IodxCst cst, int beginOffset, int endOffset) {
        assertEquals(beginOffset, cst.caret.beginOffset);
        assertEquals(endOffset, cst.caret.endOffset);
    }

    private static void assertUnicodeError(String source, int offset, int length, int line, int column,
                                           String messagePart) {
        ParseException error = assertThrows(ParseException.class, () -> IodxCstParser.parse(source));
        Token token = error.getToken();

        assertNotNull(token);
        assertEquals(offset, token.getBeginOffset());
        assertEquals(offset + length, token.getEndOffset());
        assertEquals(line, token.getBeginLine());
        assertEquals(column, token.getBeginColumn());
        assertTrue(error.getMessage(), error.getMessage().contains(messagePart));
        assertTrue(error.getMessage(), error.getMessage().contains("offset " + offset));
    }
}
