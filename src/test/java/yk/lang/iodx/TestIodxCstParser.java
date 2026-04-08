package yk.lang.iodx;

import org.junit.Test;
import yk.lang.iodx.congocc.IodxCstParser;

import static org.junit.Assert.*;

/**
 * Tests for CongoCC-based IodxCst parser
 */
public class TestIodxCstParser {

    private IodxCst parseList(String input) {
        return IodxCstParser.parse(input);
    }

    private IodxCst parseClass(String input) {
        return new IodxCstParser(input).parseClass();
    }

    private void assertCstType(String expectedType, IodxCst cst) {
        assertNotNull("CST should not be null", cst);
        assertEquals("CST type mismatch", expectedType, cst.type);
    }

    private void assertCstPosition(IodxCst cst, int expectedBegin, int expectedEnd) {
        assertNotNull("CST should have caret", cst.caret);
        assertEquals("Begin offset should match", expectedBegin, cst.caret.beginOffset);
        assertEquals("End offset should match", expectedEnd, cst.caret.endOffset);
    }

    @Test
    public void testEmptyListBody() {
        IodxCst result = parseList("");
        assertCstType("LIST_BODY", result);
        assertCstPosition(result, 0, 0); // empty string has positions 0-0
        assertTrue("Empty list should have no children", result.children.isEmpty());
    }

    @Test
    public void testSingleLiterals() {
        // Integer literal
        IodxCst result = parseList("42");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have one child", 1, result.children.size());
        
        IodxCst child = result.children.get(0);
        assertCstType("INTEGER_LITERAL", child);
        assertCstPosition(child, 0, 2); // "42" positions 0-2
        assertEquals("Integer value should be parsed correctly", 42, child.value);

        // Floating point literal
        result = parseList("3.14");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child, 0, 4); // "3.14" positions 0-4
        assertEquals("Float value should be parsed correctly", 3.14f, (Float)child.value, 0.001f);

        // String literals
        result = parseList("\"hello\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertCstPosition(child, 0, 7); // "\"hello\"" positions 0-7
        assertEquals("Double-quoted string value should be parsed correctly", "hello", child.value);

        result = parseList("'world'");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_SQ", child);
        assertCstPosition(child, 0, 7); // "'world'" positions 0-7
        assertEquals("Single-quoted string value should be parsed correctly", "world", child.value);

        // Any literal
        result = parseList("identifier");
        child = result.children.get(0);
        assertCstType("ANY_LITERAL", child);
        assertCstPosition(child, 0, 10); // "identifier" positions 0-10
        assertEquals("Identifier value should be parsed correctly", "identifier", child.value);

        // Operator
        result = parseList("+");
        child = result.children.get(0);
        assertCstType("ANY_OPERATOR", child);
        assertCstPosition(child, 0, 1); // "+" positions 0-1
        assertEquals("Operator value should be parsed correctly", "+", child.value);
    }

    @Test
    public void testMultipleLiterals() {
        IodxCst result = parseList("42 hello \"world\"");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have three children", 3, result.children.size());

        assertCstType("INTEGER_LITERAL", result.children.get(0));
        assertCstType("ANY_LITERAL", result.children.get(1));
        assertCstType("STRING_LITERAL_DQ", result.children.get(2));

        // Verify all children have exact positions
        assertCstPosition(result.children.get(0), 0, 2);   // "42" positions 0-2
        assertCstPosition(result.children.get(1), 3, 8);   // "hello" positions 3-8
        assertCstPosition(result.children.get(2), 9, 16);  // "\"world\"" positions 9-16
    }

    @Test
    public void testUnnamedClass() {
        String input = "(hello world)";
        IodxCst clazz = parseClass(input);


        System.out.println(new IodxPrinter()
            .setMaxWidth(200)
            .print(new IodxJavaToEntity().setAllClassesAvailable(true).serialize(clazz)));


        assertCstType("UNNAMED_CLASS", clazz);
        assertCstPosition(clazz, 0, 13); // entire "(hello world)" 
        
        // Check class structure: left paren + body + right paren
        assertEquals("Class should have 3 children", 3, clazz.children.size());
        assertCstType("LEFT_PAREN", clazz.children.get(0));
        assertCstType("LIST_BODY", clazz.children.get(1));
        assertCstType("RIGHT_PAREN", clazz.children.get(2));

        // Check inner list body
        IodxCst body = clazz.children.get(1);
        assertEquals("Body should have 2 children", 2, body.children.size());
        assertCstType("ANY_LITERAL", body.children.get(0));
        assertCstPosition(body.children.get(0), 1, 6); // "hello"
        assertCstType("ANY_LITERAL", body.children.get(1));
        assertCstPosition(body.children.get(1), 7, 12); // "world"
        
        // Test field map for unnamed class
        assertEquals("Should have 1 field", 1, clazz.childByField.size());
        assertTrue("Should contain body field", clazz.childByField.containsKey("body"));
        assertFalse("Should not contain name field", clazz.childByField.containsKey("name"));
        
        // Test that field points to same object as child
        assertSame("Body field should point to LIST_BODY child", 
                  clazz.children.get(1), clazz.childByField.get("body"));
        assertEquals("Body field should be LIST_BODY", "LIST_BODY", clazz.childByField.get("body").type);
    }

    @Test
    public void testNamedClass() {
        String input = "Vec2(x y)";
        IodxCst clazz = parseClass(input);
        assertCstType("NAMED_CLASS", clazz);
        assertCstPosition(clazz, 0, 9); // entire "Vec2(x y)"

        // Check class structure: name + left paren + body + right paren
        assertEquals("Class should have 4 children", 4, clazz.children.size());
        assertCstType("ANY_LITERAL", clazz.children.get(0));
        assertCstPosition(clazz.children.get(0), 0, 4); // "Vec2"
        assertCstType("LEFT_PAREN", clazz.children.get(1));
        assertCstType("LIST_BODY", clazz.children.get(2));
        assertCstType("RIGHT_PAREN", clazz.children.get(3));

        // Check inner list body
        IodxCst body = clazz.children.get(2);
        assertEquals("Body should have 2 children", 2, body.children.size());
        assertCstType("ANY_LITERAL", body.children.get(0));
        assertCstPosition(body.children.get(0), 5, 6); // "x"
        assertCstType("ANY_LITERAL", body.children.get(1));
        assertCstPosition(body.children.get(1), 7, 8); // "y"
        
        // Test field map for named class
        assertEquals("Should have 2 fields", 2, clazz.childByField.size());
        assertTrue("Should contain name field", clazz.childByField.containsKey("name"));
        assertTrue("Should contain body field", clazz.childByField.containsKey("body"));
        
        // Test that fields point to same objects as children
        assertSame("Name field should point to ANY_LITERAL child", 
                  clazz.children.get(0), clazz.childByField.get("name"));
        assertSame("Body field should point to LIST_BODY child", 
                  clazz.children.get(2), clazz.childByField.get("body"));
        
        // Verify field types
        assertEquals("Name field should be ANY_LITERAL", "ANY_LITERAL", clazz.childByField.get("name").type);
        assertEquals("Body field should be LIST_BODY", "LIST_BODY", clazz.childByField.get("body").type);
        
        // Verify field values
        assertEquals("Name field should have correct value", "Vec2", clazz.childByField.get("name").value);
    }

    @Test
    public void testNestedClasses() {
        IodxCst outerClass = parseClass("outer(inner(value))");
        assertCstType("NAMED_CLASS", outerClass);
        assertCstPosition(outerClass, 0, 19); // "outer(inner(value))" positions 0-19
        
        IodxCst outerBody = outerClass.children.get(2);
        assertCstType("LIST_BODY", outerBody);
        assertEquals("Outer body should have one child", 1, outerBody.children.size());
        
        IodxCst innerClass = outerBody.children.get(0);
        assertCstType("NAMED_CLASS", innerClass);
        assertCstPosition(innerClass, 6, 18); // "inner(value)" positions 6-18
        
        IodxCst innerBody = innerClass.children.get(2);
        assertCstType("LIST_BODY", innerBody);
        assertEquals("Inner body should have one child", 1, innerBody.children.size());
        assertCstType("ANY_LITERAL", innerBody.children.get(0));
        assertCstPosition(innerBody.children.get(0), 12, 17); // "value" positions 12-17
        
        // Test field maps for nested classes
        // Outer class fields
        assertEquals("Outer class should have 2 fields", 2, outerClass.childByField.size());
        assertTrue("Outer class should contain name field", outerClass.childByField.containsKey("name"));
        assertTrue("Outer class should contain body field", outerClass.childByField.containsKey("body"));
        assertSame("Outer body field should point to correct child", 
                  outerBody, outerClass.childByField.get("body"));
        
        // Inner class fields  
        assertEquals("Inner class should have 2 fields", 2, innerClass.childByField.size());
        assertTrue("Inner class should contain name field", innerClass.childByField.containsKey("name"));
        assertTrue("Inner class should contain body field", innerClass.childByField.containsKey("body"));
        assertSame("Inner body field should point to correct child", 
                  innerBody, innerClass.childByField.get("body"));
        
        // Verify field types
        assertEquals("Outer name field should be ANY_LITERAL", "ANY_LITERAL", outerClass.childByField.get("name").type);
        assertEquals("Outer body field should be LIST_BODY", "LIST_BODY", outerClass.childByField.get("body").type);
        assertEquals("Inner name field should be ANY_LITERAL", "ANY_LITERAL", innerClass.childByField.get("name").type);
        assertEquals("Inner body field should be LIST_BODY", "LIST_BODY", innerClass.childByField.get("body").type);
    }

    @Test
    public void testComments() {
        // Single line comment
        IodxCst result = parseList("//this is a comment");
        assertCstType("LIST_BODY", result);
        assertCstPosition(result, 0, 19); // "//this is a comment" positions 0-19
        assertEquals("Should have one child", 1, result.children.size());
        
        IodxCst comment = result.children.get(0);
        assertCstType("COMMENT_SINGLE_LINE", comment);
        assertCstPosition(comment, 0, 19); // "//this is a comment" positions 0-19

        // Multi-line comment
        result = parseList("/*multi line comment*/");
        comment = result.children.get(0);
        assertCstType("COMMENT_MULTI_LINE", comment);
        assertCstPosition(comment, 0, 22); // "/*multi line comment*/" positions 0-22

        // Comment with other elements
        result = parseList("value //comment");
        assertEquals("Should have two children", 2, result.children.size());
        assertCstType("ANY_LITERAL", result.children.get(0));
        assertCstPosition(result.children.get(0), 0, 5); // "value" positions 0-5
        assertCstType("COMMENT_SINGLE_LINE", result.children.get(1));
        assertCstPosition(result.children.get(1), 6, 15); // "//comment" positions 6-15
    }

    @Test
    public void testComplexNumbers() {
        // Integer without suffix (should be Integer)
        IodxCst result = parseList("42");
        IodxCst child = result.children.get(0);
        assertCstType("INTEGER_LITERAL", child);
        assertCstPosition(child, 0, 2); // "42" positions 0-2
        assertEquals("Integer without suffix should be Integer", 42, child.value);

        // Long with suffix (should be Long)
        result = parseList("42L");
        child = result.children.get(0);
        assertCstType("INTEGER_LITERAL", child);
        assertCstPosition(child, 0, 3); // "42L" positions 0-3
        assertEquals("Integer with L suffix should be Long", 42L, child.value);

        // Float without suffix (should be Float)
        result = parseList("3.14");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child, 0, 4); // "3.14" positions 0-4
        assertEquals("Floating point without suffix should be Float", 3.14f, (Float)child.value, 0.001f);

        // Float with F suffix (should be Float)
        result = parseList("3.14F");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child, 0, 5); // "3.14F" positions 0-5
        assertEquals("Floating point with F suffix should be Float", 3.14f, (Float)child.value, 0.001f);

        // Double with D suffix (should be Double)
        result = parseList("3.14D");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child, 0, 5); // "3.14D" positions 0-5
        assertEquals("Floating point with D suffix should be Double", 3.14, (Double)child.value, 0.001);

        // Negative integers
        result = parseList("-42");
        child = result.children.get(0);
        assertCstType("INTEGER_LITERAL", child);
        assertCstPosition(child, 0, 3); // "-42" positions 0-3
        assertEquals("Negative integer should be parsed correctly", -42, child.value);

        // Negative floats
        result = parseList("-3.14");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child, 0, 5); // "-3.14" positions 0-5
        assertEquals("Negative float should be parsed correctly", -3.14f, (Float)child.value, 0.001f);

        // Scientific notation
        result = parseList("1.5e10");
        child = result.children.get(0);
        assertCstType("FLOATING_POINT_LITERAL", child);
        assertCstPosition(child, 0, 6); // "1.5e10" positions 0-6
        assertEquals("Scientific notation should be parsed correctly", 1.5e10f, (Float)child.value, 1000f);
    }

    @Test
    public void testOperators() {
        IodxCst result = parseList("+ - * / == < > ! & |");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have 10 operators", 10, result.children.size());
        
        // Test each operator value with exact positions
        String[] expectedOperators = {"+", "-", "*", "/", "==", "<", ">", "!", "&", "|"};
        int[] startPositions = {0, 2, 4, 6, 8, 11, 13, 15, 17, 19};
        int[] endPositions = {1, 3, 5, 7, 10, 12, 14, 16, 18, 20};
        for (int i = 0; i < expectedOperators.length; i++) {
            IodxCst child = result.children.get(i);
            assertCstType("ANY_OPERATOR", child);
            assertCstPosition(child, startPositions[i], endPositions[i]);
            assertEquals("Operator " + i + " should have correct value", expectedOperators[i], child.value);
        }
    }

    @Test
    public void testSeparators() {
        assertEquals(3, parseList("(a , b)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a,b)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a, b)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a ,b)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a , +)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a,+)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a, +)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a ,+)").children.first().childByField.get("body").children.size());

        assertEquals(3, parseList("(a ; b)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a;b)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a; b)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a ;b)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a ; +)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a;+)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a; +)").children.first().childByField.get("body").children.size());
        assertEquals(3, parseList("(a ;+)").children.first().childByField.get("body").children.size());

        assertEquals(4, parseList("(a,;+)").children.first().childByField.get("body").children.size());

        assertEquals("ANY_SEPARATOR", parseList("(a,+)").children.first().childByField.get("body").children.get(1).type);
        assertEquals(",", parseList("(a,+)").children.first().childByField.get("body").children.get(1).value);
    }

    //c-style escapes
    @Test
    public void testStringLiterals() {
        // Double quoted with escapes
        IodxCst result = parseList("\"hello\\nworld\"");
        IodxCst child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertCstPosition(child, 0, 14); // "\"hello\\nworld\"" positions 0-14
        assertEquals("hello\nworld", child.value); // Should be unescaped

        // Single quoted with escapes
        result = parseList("'hello\\tworld'");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_SQ", child);
        assertCstPosition(child, 0, 14); // "'hello\\tworld'" positions 0-14
        assertEquals("hello\tworld", child.value); // Should be unescaped

        // Double quoted with quote escape
        result = parseList("\"Say \\\"Hello\\\"\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertEquals("Say \"Hello\"", child.value); // Should be unescaped

        // Single quoted with quote escape
        result = parseList("'Don\\'t'");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_SQ", child);
        assertEquals("Don't", child.value); // Should be unescaped

        // String with backslash escape
        result = parseList("\"Path\\\\to\\\\file\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertEquals("Path\\to\\file", child.value); // Should be unescaped

        // String with multiple escapes
        result = parseList("\"Line1\\nTab\\tQuote\\\"End\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertEquals("Line1\nTab\tQuote\"End", child.value); // Should be unescaped

        // String without escapes
        result = parseList("\"Simple string\"");
        child = result.children.get(0);
        assertCstType("STRING_LITERAL_DQ", child);
        assertEquals("Simple string", child.value);
    }

    @Test
    public void testWhitespaceHandling() {
        // Whitespace should be skipped but positions should be correct
        IodxCst result = parseList("  a    b  ");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have two children", 2, result.children.size());
        
        IodxCst first = result.children.get(0);
        IodxCst second = result.children.get(1);
        
        assertCstType("ANY_LITERAL", first);
        assertCstType("ANY_LITERAL", second);
        
        // Second element should start after the first
        assertTrue("Second element should start after first", 
                   second.caret.beginOffset > first.caret.endOffset);
    }

    @Test
    public void testNewlineWhitespace() {
        // Test that actual newline character is treated as whitespace
        IodxCst result = parseList("\ntrue");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have one child", 1, result.children.size());
        
        IodxCst child = result.children.get(0);
        assertCstType("ANY_LITERAL", child);
        assertCstPosition(child, 1, 5); // "true" after newline, positions 1-5
        assertEquals(Boolean.TRUE, child.value);
        
        // Test multiple whitespace types including newlines
        result = parseList("hello\n\t world\r\n42");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have three children", 3, result.children.size());
        
        assertEquals("hello", result.children.get(0).value);
        assertEquals("world", result.children.get(1).value);
        assertEquals(42, result.children.get(2).value);
    }

    @Test
    public void testBooleanAndNullLiterals() {
        // Test true literal
        IodxCst result = parseList("true");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have one child", 1, result.children.size());
        
        IodxCst trueNode = result.children.get(0);
        assertCstType("ANY_LITERAL", trueNode);
        assertEquals("true value should be Boolean.TRUE", Boolean.TRUE, trueNode.value);
        
        // Test false literal
        result = parseList("false");
        IodxCst falseNode = result.children.get(0);
        assertCstType("ANY_LITERAL", falseNode);
        assertEquals("false value should be Boolean.FALSE", Boolean.FALSE, falseNode.value);
        
        // Test null literal
        result = parseList("null");
        IodxCst nullNode = result.children.get(0);
        assertCstType("ANY_LITERAL", nullNode);
        assertEquals("null value should be null", null, nullNode.value);
        
        // Test mixed with other literals
        result = parseList("42 true 'hello' false null");
        assertEquals("Should have five children", 5, result.children.size());
        
        assertEquals(42, result.children.get(0).value);
        assertEquals(Boolean.TRUE, result.children.get(1).value);
        assertEquals("hello", result.children.get(2).value);
        assertEquals(Boolean.FALSE, result.children.get(3).value);
        assertEquals(null, result.children.get(4).value);
    }

    @Test
    public void testStaticParseMethod() {
        // Test the convenient static parse method
        IodxCst result = IodxCstParser.parse("hello 42 true");
        assertCstType("LIST_BODY", result);
        assertEquals("Should have three children", 3, result.children.size());
        
        assertEquals("hello", result.children.get(0).value);
        assertEquals(42, result.children.get(1).value);
        assertEquals(Boolean.TRUE, result.children.get(2).value);
    }

    @Test
    public void testPositionAccuracy() {
        String input = "hello(world)";
        IodxCst clazz = parseClass(input);
        
        // Check that positions correspond to actual text
        assertCstType("NAMED_CLASS", clazz);
        
        // The class should span the entire input
        assertEquals("Class should start at beginning", 0, clazz.caret.beginOffset);
        assertEquals("Class should end at end of input", input.length(), clazz.caret.endOffset);
    }

    @Test
    public void testComplexValueParsing() {
        // Test a complex example with various value types in a named class
        String input = "MyClass(42 \"hello\" world)";
        IodxCst clazz = parseClass(input);
        assertCstType("NAMED_CLASS", clazz);
        assertCstPosition(clazz, 0, 25); // entire "MyClass(42 \"hello\" world)" (length 25)
        
        // Test class name value
        assertEquals("Class name should be parsed correctly", "MyClass", clazz.childByField.get("name").value);
        assertCstPosition(clazz.childByField.get("name"), 0, 7); // "MyClass"
        
        // Test body values
        IodxCst body = clazz.childByField.get("body");
        assertEquals("Body should have 3 children", 3, body.children.size());
        
        // Test integer value
        IodxCst intNode = body.children.get(0);
        assertCstType("INTEGER_LITERAL", intNode);
        assertCstPosition(intNode, 8, 10); // "42"
        assertEquals("Integer value should be parsed correctly", 42, intNode.value);
        
        // Test string value
        IodxCst stringNode = body.children.get(1);
        assertCstType("STRING_LITERAL_DQ", stringNode);
        assertCstPosition(stringNode, 11, 18); // "\"hello\""
        assertEquals("String value should be parsed correctly", "hello", stringNode.value);
        
        // Test identifier value
        IodxCst identifierNode = body.children.get(2);
        assertCstType("ANY_LITERAL", identifierNode);
        assertCstPosition(identifierNode, 19, 24); // "world" (positions 19-23, but end is exclusive so 24)
        assertEquals("Identifier value should be parsed correctly", "world", identifierNode.value);
    }


}