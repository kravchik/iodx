package yk.lang.iodx;

import junit.framework.TestCase;
import org.junit.Test;
import yk.lang.iodx.congocc.IodxCstParser;
import yk.lang.iodx.utils.Caret;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;
import static yk.ycollections.YArrayList.al;

/**
 * 13.07.2024
 */
public class TestIodxEntitySerialization {
    @Test
    public void testStructure() {
        assertEquals(al(), Iodx.readIodxEntities(""));

        testObj(entity(), "()");
        testObj(entity("foo"), "foo()");
        testObj(entity("foo", "bar"), "foo(bar)");
        testObj(entity("foo", entity()), "foo(())");
        testObj(entity("foo", entity("bar", "hello")), "foo(bar(hello))");
        testObj(entity("foo", "bar", entity(null, "hello")), "foo(bar (hello))");
    }

    @Test
    public void testNumbers() {
        testNumbers(al(0, -0, 1, -1, 100, -100), "0 0 1 -1 100 -100", "0 -0 1 -1 100 -100");
        testNumbers(al(0f, -0f, 1f, -1f, 100f, -100f, 0.1f, 1.1f, 100000f),
            "0f -0f 1f -1f 100f -100f 0.1f 1.1f 100000f",
            "0.0 -0.0 1.0 -1.0 100.0 -100.0 0.1 1.1 100000.0",
            "0F -0F 1F -1F 100F -100F 0.1F 1.1F 1e5F"
        );
        testNumbers(al(0d, -0d, 1d, -1d, 100d, -100d, 0.1d, 1.1d, 100000d),
            "0d -0d 1d -1d 100d -100d 0.1d 1.1d 100000d",
            "0D -0D 1D -1D 100D -100D 0.1D 1.1D 1e5D"
        );
    }

    private void testNumbers(YList<Object> j, String out, String... alts) {
        for (String alt : alts) {
            assertEquals(j, Iodx.readIodxEntities(alt));
        }
        assertEquals(out, Iodx.printIodxEntities(j));
    }

    @Test
    public void testEscapes() {
        testStrings("", "''", "\"\"");
        testStrings(" ", "' '", "\" \"");
        testStrings(" ' ", "\" ' \"",       "' \\' '", "\" \\' \"");
        testStrings(" \" ", "' \" '",       "\" \\\" \"", "' \\\" '");

        testStrings(" \\ ", "' \\\\ '",     "\" \\\\ \"");
        testStrings(" \t ", "' \\t '",      "\" \t \"", "' \t '", "\" \\t \"", "' \\t '");
        testStrings(" \b ", "' \\b '",      "\" \b \"", "' \b '", "\" \\b \"", "' \\b '");
        testStrings(" \n ", "' \n '",       "\" \n \"", "\" \\n \"", "' \\n '");

        // \r is removed to enforce platform independence
        testStrings(" \r ", "' \\r '");
        testStrings("--", "--", "'-\r-'", "\"-\r-\"");
        testStrings(" \n ", "' \n '", "' \n\r '", "\" \r\n \"");

        testStrings(" \f ", "' \\f '",      "\" \f \"", "' \f '", "\" \\f \"", "' \\f '");

        testStrings("hello", "hello", "\"hello\"", "'hello'");
        testStrings("hello world", "'hello world'", "\"hello world\"");
    }

    private static void testObj(Object expected, String s) {
        assertEquals(al(expected), Iodx.readIodxEntities(s));
        assertEquals(al(expected, expected), Iodx.readIodxEntities(s + " " + s));
        assertEquals(al(expected, expected), Iodx.readIodxEntities(s + "\n" + s));
        assertEquals(expected, Iodx.readIodxEntity(s));
    }

    private static void testStrings(String data, String canonicForm, String... alternativeForms) {
        assertEquals(data, Iodx.readIodxEntity(canonicForm));
        assertEquals(al(data), Iodx.readIodxEntities(canonicForm));
        assertEquals(al(data, data), Iodx.readIodxEntities(canonicForm + " " + canonicForm));
        assertEquals(al(data, data), Iodx.readIodxEntities(canonicForm + "\n" + canonicForm));

        assertEquals(canonicForm, Iodx.printIodxEntity(data));
        assertEquals(canonicForm, Iodx.printIodxEntities(al(data)));
        assertEquals(canonicForm + " " + canonicForm, Iodx.printIodxEntities(al(data, data)));

        for (String alt : alternativeForms) {
            assertEquals(data, Iodx.readIodxEntity(alt));
            assertEquals(al(data), Iodx.readIodxEntities(alt));
            assertEquals(al(data, data), Iodx.readIodxEntities(alt + " " + alt));
            assertEquals(al(data, data), Iodx.readIodxEntities(alt + "\n" + alt));
        }
    }

    public static IodxEntity entity() {
        return new IodxEntity(null, al());
    }

    public static IodxEntity entity(String name, Object... values) {
        return new IodxEntity(name, al(values));
    }
    
    /**
     * Helper method to assert all caret fields with exact values
     */
    private void assertCaret(String description, Caret caret, 
                           int expectedBeginOffset, int expectedEndOffset,
                           int expectedBeginLine, int expectedBeginColumn,
                           int expectedEndLine, int expectedEndColumn) {
        assertNotNull(description + " should have caret", caret);
        assertEquals(description + " beginOffset", expectedBeginOffset, caret.beginOffset);
        assertEquals(description + " endOffset", expectedEndOffset, caret.endOffset);
        assertEquals(description + " beginLine", expectedBeginLine, caret.beginLine);
        assertEquals(description + " beginColumn", expectedBeginColumn, caret.beginColumn);
        assertEquals(description + " endLine", expectedEndLine, caret.endLine);
        assertEquals(description + " endColumn", expectedEndColumn, caret.endColumn);
    }

    @Test
    public void testSimpleLiterals() throws Exception {
        // Test integer
        IodxCstParser parser = new IodxCstParser("42");
        IodxCst result = parser.parseListBody();
        YList<Object> resolved = IodxEntityFromCst.translate(result.children);
        TestCase.assertEquals(1, resolved.size());
        TestCase.assertEquals(42, resolved.get(0));

        // Test float
        parser = new IodxCstParser("3.14f");
        result = parser.parseListBody();
        resolved = IodxEntityFromCst.translate(result.children);
        TestCase.assertEquals(1, resolved.size());
        TestCase.assertEquals(3.14f, (Float) resolved.get(0), 0.001f);

        // Test double
        parser = new IodxCstParser("2.71d");
        result = parser.parseListBody();
        resolved = IodxEntityFromCst.translate(result.children);
        TestCase.assertEquals(1, resolved.size());
        TestCase.assertEquals(2.71d, (Double) resolved.get(0), 0.001d);

        // Test string
        parser = new IodxCstParser("\"hello world\"");
        result = parser.parseListBody();
        resolved = IodxEntityFromCst.translate(result.children);
        TestCase.assertEquals(1, resolved.size());
        TestCase.assertEquals("hello world", resolved.get(0));
    }

    @Test
    public void testNamedClass() throws Exception {
        IodxCstParser parser = new IodxCstParser("Person(\"John\" 25)");
        IodxCst result = parser.parseListBody();
        YList<Object> resolved = IodxEntityFromCst.translate(result.children);
        TestCase.assertEquals(1, resolved.size());

        Object entity = resolved.get(0);
        assertTrue(entity instanceof IodxEntity);
        IodxEntity person = (IodxEntity) entity;
        TestCase.assertEquals("Person", person.name);
        TestCase.assertEquals(2, person.children.size());
        TestCase.assertEquals("John", person.children.get(0));
        TestCase.assertEquals(25, person.children.get(1));
        
        assertCaret("Person entity", person.caret, 0, 17, 1, 1, 1, 17);
        assertNotNull("Person entity should have childrenCarets", person.childrenCarets);
        assertEquals(2, person.childrenCarets.size());
        assertCaret("First child (\"John\")", person.childrenCarets.get(0), 7, 13, 1, 8, 1, 13);
        assertCaret("Second child (25)", person.childrenCarets.get(1), 14, 16, 1, 15, 1, 16);
    }

    @Test
    public void testUnnamedClass() throws Exception {
        IodxCstParser parser = new IodxCstParser("(\"data\" 123)");
        IodxCst result = parser.parseListBody();
        YList<Object> resolved = IodxEntityFromCst.translate(result.children);
        TestCase.assertEquals(1, resolved.size());

        Object entity = resolved.get(0);
        assertTrue(entity instanceof IodxEntity);
        IodxEntity unnamed = (IodxEntity) entity;
        assertNull(unnamed.name);
        assertEquals(2, unnamed.children.size());
        assertEquals("data", unnamed.children.get(0));
        assertEquals(123, unnamed.children.get(1));
        
        assertCaret("Unnamed entity", unnamed.caret, 0, 12, 1, 1, 1, 12);
        assertNotNull("Unnamed entity should have childrenCarets", unnamed.childrenCarets);
        assertEquals(2, unnamed.childrenCarets.size());
        assertCaret("First child (\"data\")", unnamed.childrenCarets.get(0), 1, 7, 1, 2, 1, 7);
        assertCaret("Second child (123)", unnamed.childrenCarets.get(1), 8, 11, 1, 9, 1, 11);
    }

    @Test
    public void testTupleConversion() throws Exception {
        // Test simple key=value
        IodxCstParser parser = new IodxCstParser("name = \"John\"");
        IodxCst result = parser.parseListBody();
        YList<Object> resolved = IodxEntityFromCst.translate(result.children);
        assertEquals(1, resolved.size());

        Object tuple = resolved.get(0);
        assertTrue(tuple instanceof Tuple);
        Tuple<?, ?> t = (Tuple<?, ?>) tuple;
        assertEquals("name", t.a);
        assertEquals("John", t.b);
    }

    @Test
    public void testComplexExample() throws Exception {
        // Test a complex structure with tuples and nested classes - multiline to test line tracking
        IodxCstParser parser = new IodxCstParser("Person(name = \"John\"\nage = 25\naddress = Address(\"123 Main St\"))");
        IodxCst result = parser.parseListBody();
        YList<Object> resolved = IodxEntityFromCst.translate(result.children);
        assertEquals(1, resolved.size());

        Object entity = resolved.get(0);
        assertTrue(entity instanceof IodxEntity);
        IodxEntity person = (IodxEntity) entity;
        assertEquals("Person", person.name);
        assertEquals(3, person.children.size());
        
        // Check caret positions for multiline "Person(name = \"John\"\nage = 25\naddress = Address(\"123 Main St\"))"
        assertNotNull("Person entity should have caret", person.caret);
        assertEquals(0, person.caret.beginOffset);
        assertEquals(63, person.caret.endOffset); // full string length with \n characters
        
        // Check children carets - multiline with line number verification
        assertNotNull("Person entity should have childrenCarets", person.childrenCarets);
        assertEquals(3, person.childrenCarets.size());
        
        // name = "John" tuple spans positions 7-20 (line 1, name to "John")
        assertCaret("First child (name tuple)", person.childrenCarets.get(0), 7, 20, 1, 8, 1, 20);
        
        // age = 25 tuple spans positions 21-29 (line 2, age to 25) 
        assertCaret("Second child (age tuple)", person.childrenCarets.get(1), 21, 29, 2, 1, 2, 8);
        
        // address = Address(...) tuple spans positions 30-62 (line 3, address to Address(...))
        assertCaret("Third child (address tuple)", person.childrenCarets.get(2), 30, 62, 3, 1, 3, 32);

        // Check name tuple
        Tuple<?, ?> nameTuple = (Tuple<?, ?>) person.children.get(0);
        assertEquals("name", nameTuple.a);
        assertEquals("John", nameTuple.b);

        // Check age tuple
        Tuple<?, ?> ageTuple = (Tuple<?, ?>) person.children.get(1);
        assertEquals("age", ageTuple.a);
        assertEquals(25, ageTuple.b);

        // Check address tuple with nested entity
        Tuple<?, ?> addressTuple = (Tuple<?, ?>) person.children.get(2);
        assertEquals("address", addressTuple.a);
        assertTrue(addressTuple.b instanceof IodxEntity);
        IodxEntity address = (IodxEntity) addressTuple.b;
        assertEquals("Address", address.name);
        assertEquals(1, address.children.size());
        assertEquals("123 Main St", address.children.get(0));
    }

    @Test
    public void testMixedContent() throws Exception {
        // Test mix of comments, literals, and classes
        IodxCstParser parser = new IodxCstParser("//header comment\nPerson(\"John\") 42 //inline comment");
        IodxCst result = parser.parseListBody();
        YList<Object> resolved = IodxEntityFromCst.translate(result.children);
        assertEquals(4, resolved.size());

        // Header comment
        assertTrue(resolved.get(0) instanceof IodxEntity.IodxComment);
        IodxEntity.IodxComment headerComment = (IodxEntity.IodxComment) resolved.get(0);
        assertTrue(headerComment.isOneLine);
        assertEquals("header comment", headerComment.text);

        // Person entity
        assertTrue(resolved.get(1) instanceof IodxEntity);
        IodxEntity person = (IodxEntity) resolved.get(1);
        assertEquals("Person", person.name);
        assertEquals("John", person.children.get(0));

        // Number literal
        assertEquals(42, resolved.get(2));

        // Inline comment
        assertTrue(resolved.get(3) instanceof IodxEntity.IodxComment);
        IodxEntity.IodxComment inlineComment = (IodxEntity.IodxComment) resolved.get(3);
        assertTrue(inlineComment.isOneLine);
        assertEquals("inline comment", inlineComment.text);
    }

    @Test
    public void test1() {
        assertEquals("IodxEntity{children=[]}", getIodxList("()"));
        assertEquals("IodxEntity{children=[a]}", getIodxList("(a)"));
        assertEquals("IodxEntity{children=[a, b]}", getIodxList("(a b)"));
        assertEquals("IodxEntity{children=[tuple(a b)]}", getIodxList("(a=b)"));
        assertEquals("IodxEntity{children=[a, tuple(b c)]}", getIodxList("(a b=c)"));
        assertEquals("IodxEntity{children=[tuple(b c), d]}", getIodxList("(b=c d)"));
        assertEquals("IodxEntity{children=[tuple(a b), tuple(c d)]}", getIodxList("(a=b c=d)"));
        assertEquals("IodxEntity{children=[tuple(a b), e, tuple(c d)]}", getIodxList("(a=b e c=d)"));
        assertEquals("IodxEntity{children=[tuple(a b), e, tuple(c d), f]}", getIodxList("(a=b e c=d f)"));
        assertEquals("IodxEntity{children=[tuple(a b), tuple(c d), f]}", getIodxList("(a=b c=d f)"));

        assertEquals("IodxEntity{children=[tuple(a =)]}", getIodxList("(a='=')"));
        assertEquals("IodxEntity{children=[tuple(null value)]}", getIodxList("(null=value)"));
        assertEquals("IodxEntity{children=[tuple(key null)]}", getIodxList("(key=null)"));
        assertEquals("IodxEntity{children=[tuple(key false)]}", getIodxList("(key=false)"));
        assertEquals("IodxEntity{children=[tuple(true false)]}", getIodxList("(true=false)"));

        assertEquals("IodxEntity{name='name', children=[]}", getIodxList("name()"));
        assertEquals("IodxEntity{name='name', children=[a]}", getIodxList("name(a)"));

        assertEquals("IodxEntity{children=[a, ,, b]}", getIodxList("(a,b)"));
        assertEquals("IodxEntity{children=[a, ,, b]}", getIodxList("(a, b)"));
        assertEquals("IodxEntity{children=[a, ,, +]}", getIodxList("(a,+)"));

        assertEquals("IodxEntity{children=[a, ;, b]}", getIodxList("(a;b)"));
        assertEquals("IodxEntity{children=[a, ;, b]}", getIodxList("(a; b)"));
        assertEquals("IodxEntity{children=[a, ;, +]}", getIodxList("(a;+)"));
    }

    @Test
    public void testComments() {
        // Test single line comment
        IodxCstParser parser = new IodxCstParser("//this is a comment");
        IodxCst result = parser.parseListBody();
        YList<Object> resolved = IodxEntityFromCst.translate(result.children);
        assertEquals(1, resolved.size());

        Object comment = resolved.get(0);
        assertTrue(comment instanceof IodxEntity.IodxComment);
        IodxEntity.IodxComment yadsComment = (IodxEntity.IodxComment) comment;
        assertTrue(yadsComment.isOneLine);
        assertEquals("this is a comment", yadsComment.text);

        // Test multi-line comment
        parser = new IodxCstParser("/*multi\nline\ncomment*/");
        result = parser.parseListBody();
        resolved = IodxEntityFromCst.translate(result.children);
        assertEquals(1, resolved.size());

        comment = resolved.get(0);
        assertTrue(comment instanceof IodxEntity.IodxComment);
        yadsComment = (IodxEntity.IodxComment) comment;
        assertFalse(yadsComment.isOneLine);
        assertEquals("multi\nline\ncomment", yadsComment.text);

        assertEquals("IodxComment{isOneLine=true, text=''}", getIodxList("//"));
        testComment("IodxComment{isOneLine=true, text=''}", "//\n");
        testComment("IodxComment{isOneLine=false, text=' '}", "/* */");

        assertException("(a//\n = b)", "Comment instead of key at 1:3");
        assertException("(a = //\nb)", "Comment instead of value at 1:6");

        assertException("(a/**/ = b)", "Comment instead of key at 1:3");
        assertException("(a = /**/b)", "Comment instead of value at 1:6");

        testObj(new IodxEntity.IodxComment(false, "comment"), "/*comment*/");
        Object oneLine = new IodxEntity.IodxComment(true, "comment");
        assertEquals(al(oneLine), Iodx.readIodxEntities("//comment"));
        assertEquals(al(oneLine, oneLine), Iodx.readIodxEntities("//comment" + "\n" + "//comment"));
        assertEquals(oneLine, Iodx.readIodxEntity("//comment"));
    }

    private static void testComment(String expectedComment, String srcComment) {
        assertEquals("IodxEntity{children=[" + expectedComment + "]}", getIodxList("(" + srcComment + ")"));
        assertEquals("IodxEntity{children=[a, " + expectedComment + "]}", getIodxList("(a " + srcComment + ")"));
        assertEquals("IodxEntity{children=[" + expectedComment + ", a]}", getIodxList("(" + srcComment + " a)"));
        assertEquals("IodxEntity{children=[a, " + expectedComment + ", b]}", getIodxList("(a " + srcComment + " b)"));

        assertEquals("IodxEntity{children=[tuple(a b), " + expectedComment + "]}", getIodxList("(a = b" + srcComment + ")"));
        assertEquals("IodxEntity{children=[" + expectedComment + ", tuple(a b)]}", getIodxList("(" + srcComment + "a = b)"));
        assertEquals("IodxEntity{children=[tuple(a b), " + expectedComment + ", tuple(c d)]}", getIodxList("(a = b" + srcComment + "c = d)"));
    }

    @Test
    public void testKeyValueErrors() {
        assertException("(= a)", "Expected key before '=' at 1:2");
        assertException("(a =)", "Expected value after '=' at 1:4");
        assertException("(= =)", "Expected key before '=' at 1:2");
        assertException("(a = =)", "Expected value at 1:6");
        assertException("(a = b = c)", "Expected key before '=' at 1:8");
        assertException("(a = b =)", "Expected key before '=' at 1:8");
    }

    public static void assertException(String src, String errorText) {
        try {
            String result = getIodxList(src);
            fail(result);
        } catch (Exception e) {
            assertEquals(errorText, e.getMessage());
        }
    }

    private static String getIodxList(String s) {
        return IodxEntityFromCst.resolve(IodxCstParser.parse(s).children.first()).toString();
    }

}
