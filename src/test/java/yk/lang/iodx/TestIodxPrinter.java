package yk.lang.iodx;

import org.junit.Test;
import yk.lang.iodx.congocc.IodxCstParser;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static yk.ycollections.Tuple.tuple;
import static yk.ycollections.YArrayList.al;

/**
 * Tests for IodxPrinter with new value-based output
 */
public class TestIodxPrinter {

    @Test
    public void testIntegerOutput() throws Exception {
        IodxCstParser parser = new IodxCstParser("42");
        IodxCst result = parser.parseListBody();
        
        IodxPrinter output = new IodxPrinter();
        String outputStr = output.print(result);
        
        assertEquals("42", outputStr.trim());
    }

    @Test  
    public void testLongOutput() throws Exception {
        IodxCstParser parser = new IodxCstParser("123L");
        IodxCst result = parser.parseListBody();
        
        IodxPrinter output = new IodxPrinter();
        String outputStr = output.print(result);
        
        assertEquals("123", outputStr.trim());
    }

    @Test
    public void testFloatOutput() throws Exception {
        IodxCstParser parser = new IodxCstParser("3.14f");
        IodxCst result = parser.parseListBody();
        
        IodxPrinter output = new IodxPrinter();
        String outputStr = output.print(result);
        
        // Float is default, no suffix in output
        assertEquals("3.14", outputStr.trim());
    }

    @Test
    public void testDoubleOutput() throws Exception {
        IodxCstParser parser = new IodxCstParser("2.71d");
        IodxCst result = parser.parseListBody();
        
        IodxPrinter output = new IodxPrinter();
        String outputStr = output.print(result);
        
        assertEquals("2.71d", outputStr.trim());
    }

    @Test
    public void testStringOutput() throws Exception {
        IodxCstParser parser = new IodxCstParser("\"hello world\"");
        IodxCst result = parser.parseListBody();
        
        IodxPrinter output = new IodxPrinter();
        String outputStr = output.print(result);
        
        assertEquals("\"hello world\"", outputStr.trim());
    }

    @Test
    public void testStringWithEscapesOutput() throws Exception {
        IodxCstParser parser = new IodxCstParser("\"hello\\nworld\"");
        IodxCst result = parser.parseListBody();
        
        IodxPrinter output = new IodxPrinter();
        String outputStr = output.print(result);
        
        assertEquals("\"hello\nworld\"", outputStr.trim());
    }

    @Test
    public void testComplexExpression() throws Exception {
        IodxCstParser parser = new IodxCstParser("myClass(42 \"test\" 3.14)");
        IodxCst result = parser.parseListBody();
        
        IodxPrinter output = new IodxPrinter();
        String outputStr = output.print(result);
        
        // 3.14 without suffix parses as Float, outputs without suffix
        assertEquals("myClass(42 \"test\" 3.14)", outputStr.trim());
    }

    @Test
    public void testPrimitiveObjects() {
        IodxPrinter output = new IodxPrinter();
        
        // Test integers
        assertEquals("42", output.print(42).trim());
        assertEquals("42l", output.print(42L).trim());
        
        // Test floats
        assertEquals("3.14f", output.print(3.14f).trim());
        assertEquals("2.71d", output.print(2.71d).trim());
        assertEquals("3f", output.print(3.0f).trim());
        assertEquals("3d", output.print(3.0d).trim());
        
        // Test strings
        assertEquals("hello", output.print("hello").trim());
        assertEquals("'hello world'", output.print("hello world").trim());
        assertEquals("\"can't\"", output.print("can't").trim());
        assertEquals("'null'", output.print("null").trim());
        assertEquals("'true'", output.print("true").trim());
        assertEquals("'false'", output.print("false").trim());
        
        // Test boolean
        assertEquals("true", output.print(true).trim());
        assertEquals("false", output.print(false).trim());
        
        // Test null
        assertEquals("null", output.print(null).trim());
        
        // Test character
        assertEquals("a", output.print('a').trim());
        assertEquals("' '", output.print(' ').trim());
    }

    @Test
    public void testListSerialization() {
        IodxPrinter output = new IodxPrinter();
        
        // Empty list
        assertEquals("()", output.print(al()).trim());
        
        // List with primitives
        assertEquals("(1 2 3)", output.print(al(1, 2, 3)).trim());
        assertEquals("(hello world)", output.print(al("hello", "world")).trim());
        assertEquals("(42 'hello world' true)", output.print(al(42, "hello world", true)).trim());
        
        // Nested lists
        assertEquals("((1 2) (3 4))", output.print(al(al(1, 2), al(3, 4))).trim());
        
        // Mixed content
        assertEquals("(1 (2 3) 4)", output.print(al(1, al(2, 3), 4)).trim());

        assertEquals("(1 (hello , 3) 4)", output.print(al(1, al("hello", ",", 3), 4)).trim());
        assertEquals("(1 (hello , +) 4)", output.print(al(1, al("hello", ",", "+"), 4)).trim());

        assertEquals("(1 (hello ; 3) 4)", output.print(al(1, al("hello", ";", 3), 4)).trim());
        assertEquals("(1 (hello ; +) 4)", output.print(al(1, al("hello", ";", "+"), 4)).trim());
    }

    @Test
    public void testMapSerialization() {
        IodxPrinter output = new IodxPrinter();
        
        // Empty map
        Map<String, Object> emptyMap = new HashMap<>();
        assertEquals("(=)", output.print(emptyMap).trim());
        
        // Simple map
        Map<String, Object> simpleMap = new HashMap<>();
        simpleMap.put("key", "value");
        simpleMap.put("count", 42);
        String result = output.print(simpleMap).trim();
        // Maps are unordered, so we need to check both possible orders
        assertTrue("Should contain key = value mapping", 
                   result.contains("key = value") || result.contains("count = 42"));
        assertTrue("Should be wrapped in parentheses", 
                   result.startsWith("(") && result.endsWith(")"));
    }

    @Test
    public void testTupleSerialization() {
        IodxPrinter output = new IodxPrinter();
        
        // Simple tuple
        assertEquals("key = value", output.print(tuple("key", "value")).trim());
        assertEquals("count = 42", output.print(tuple("count", 42)).trim());
        
        // Nested tuple - inner tuple is not wrapped in parentheses
        assertEquals("outer = inner = data", output.print(tuple("outer", tuple("inner", "data"))).trim());
    }

    @Test
    public void testIodxCommentSerialization() {
        IodxPrinter output = new IodxPrinter();
        
        // Single line comment
        IodxEntity.IodxComment singleComment = new IodxEntity.IodxComment(true, " This is a comment");
        assertEquals("// This is a comment", output.print(singleComment).trim());
        
        // Multi-line comment
        IodxEntity.IodxComment multiComment = new IodxEntity.IodxComment(false, " Multi line comment ");
        assertEquals("/* Multi line comment */", output.print(multiComment).trim());
        
        // Comment in list - single line comments prevent compact format
        String expected = "(\n  42\n  // comment\n  hello\n)";
        assertEquals(expected, output.print(al(42, new IodxEntity.IodxComment(true, " comment"), "hello")));
    }

    @Test
    public void testIodxEntitySerialization() {
        IodxPrinter output = new IodxPrinter();
        
        // Unnamed entity
        IodxEntity unnamedEntity = new IodxEntity(null, al(1, 2, 3));
        assertEquals("(1 2 3)", output.print(unnamedEntity).trim());
        
        // Named entity  
        IodxEntity namedEntity = new IodxEntity("Vec2", al("x", "y"));
        assertEquals("Vec2(x y)", output.print(namedEntity).trim());
        
        // Entity with tuples
        IodxEntity entityWithTuples = new IodxEntity("Config", al(
            tuple("width", 800),
            tuple("height", 600),
            "visible"
        ));
        String result = output.print(entityWithTuples).trim();
        assertTrue("Should contain Config(...)", result.startsWith("Config("));
        assertTrue("Should contain width = 800", result.contains("width = 800"));
        assertTrue("Should contain height = 600", result.contains("height = 600"));
        
        // Nested entities
        IodxEntity innerEntity = new IodxEntity("Point", al(10, 20));
        IodxEntity outerEntity = new IodxEntity("Shape", al(innerEntity, "red"));
        assertEquals("Shape(Point(10 20) red)", output.print(outerEntity).trim());
    }

    @Test
    public void testUnsupportedObjectType() {
        IodxPrinter output = new IodxPrinter();
        
        // Try to serialize an unsupported object
        Object unsupported = new java.util.Date();
        
        try {
            output.print(unsupported);
            fail("Expected RuntimeException for unsupported object type");
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            assertTrue("Should mention unsupported object type", 
                      e.getMessage().contains("Unsupported const type. Class: class java.util.Date"));
        }
    }

    @Test
    public void testComplexKeys() {
        // Parse the complex key case and verify proper formatting
        String input = "complexKey((" +
                       "'00000000-0000-0000-0000-000000000000' " + 
                       "'FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF'" +
                       ") = npc(name = Foo))";

        IodxCst parsed = IodxCstParser.parse(input);
        Object resolved = IodxEntityFromCst.translate(parsed.children).get(0);
        
        IodxPrinter output = new IodxPrinter();
        output.maxWidth = 8;
        String result = output.print(resolved);
        
        // Check that UUID strings have quotes (they should need quotes due to special characters)
        assertTrue("UUID strings should have quotes", result.contains("'00000000-0000-0000-0000-000000000000'"));
        assertTrue("UUID strings should have quotes", result.contains("'FFFFFFFF-FFFF-FFFF-FFFF-FFFFFFFFFFFF'"));
        
        // Check overall structure
        assertTrue("Should contain complexKey", result.contains("complexKey"));
        assertTrue("Should contain npc", result.contains("npc"));
        assertTrue("Should contain name = Foo", result.contains("name = Foo"));
    }
}
