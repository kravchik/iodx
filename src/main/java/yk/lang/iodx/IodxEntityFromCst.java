package yk.lang.iodx;

import yk.lang.iodx.utils.BadException;
import yk.lang.iodx.utils.Caret;
import yk.ycollections.YList;

import static yk.lang.iodx.utils.BadException.die;
import static yk.ycollections.Tuple.tuple;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

/**
 * Deserializer that converts IodxCst back to IodxEntity, String, Number, Boolean
 */
public class IodxEntityFromCst {
    public static final String DELIMITER = "=";

    /**
     * Main entry point: converts a IodxCst node to appropriate data object
     */
    public static Object resolve(IodxCst node) {
        if (node == null) return null;
        
        switch (node.type) {
            case "LIST_BODY":
                return translate(node.children);
                
            case "NAMED_CLASS":
                return resolveKeyValuesWithCaretsFromNodes(
                    node.childByField.get("body").children,
                    node.childByField.get("name").value.toString(),
                    node.caret);
                
            case "UNNAMED_CLASS":
                YList<IodxCst> bodyChildren = node.childByField.get("body").children;
                // Special case: (=) means empty map - return YHashMap directly
                if (bodyChildren.size() == 1 && isDelimiter(bodyChildren.get(0))) {
                    return hm(); // Return empty YHashMap directly
                }
                return resolveKeyValuesWithCaretsFromNodes(
                    bodyChildren,
                    null,
                    node.caret);
                
            case "COMMENT_SINGLE_LINE":
                // Convert to IodxComment - remove "//" prefix
                return new IodxEntity.IodxComment(true, node.value.toString().substring(2));
                
            case "COMMENT_MULTI_LINE":
                // Convert to IodxComment - remove "/*" and "*/" 
                String multiLineText = node.value.toString();
                return new IodxEntity.IodxComment(false, multiLineText.substring(2, multiLineText.length() - 2));
                
            case "INTEGER_LITERAL":
            case "FLOATING_POINT_LITERAL":
            case "STRING_LITERAL_DQ":
            case "STRING_LITERAL_SQ":
            case "ANY_LITERAL":
            case "ANY_OPERATOR":
            case "ANY_SEPARATOR":
                // Return the parsed value directly
                return node.value;
                
            default:
                throw new BadException("Unknown IodxCst node type: " + node.type);
        }
    }

    /**
     * Converts a list of IodxCst nodes, handling the special case of 'a = b' -> Tuple conversion.
     */
    public static YList<Object> translate(YList<IodxCst> nodes) {
        return resolveKeyValuesWithCaretsFromNodes(nodes, null, null).children;
    }

    /**
     * Converts a IodxCst LIST_BODY node to a IodxEntity with position information.
     * Extracts child nodes and their carets, then processes them with key-value logic.
     */
    public static IodxEntity resolveKeyValuesWithCarets(IodxCst listBodyNode) {
        if (listBodyNode == null || !"LIST_BODY".equals(listBodyNode.type)) {
            throw new IllegalArgumentException("Expected LIST_BODY node, got: " + 
                (listBodyNode == null ? "null" : listBodyNode.type));
        }
        return resolveKeyValuesWithCaretsFromNodes(listBodyNode.children, null, null);
    }

    /**
     * Converts a list of IodxCst nodes, collecting both resolved objects and their carets.
     * This is the primary implementation that handles the full 'a = b' -> Tuple conversion logic
     * while also collecting caret positions for each resolved element.
     * Returns a IodxEntity with the specified name, caret, and children with their carets.
     */
    private static IodxEntity resolveKeyValuesWithCaretsFromNodes(YList<IodxCst> nodes, String name, Caret caret) {
        if (nodes == null) return new IodxEntity(name, al(), caret, al());
        
        IodxCst leftNode = null;
        YList<Object> result = al();
        YList<Caret> carets = al();
        
        for (int i = 0; i < nodes.size(); i++) {
            IodxCst node = nodes.get(i);
            
            if (isDelimiter(node)) {
                // Found '=' delimiter - convert previous element and next element to Tuple
                if (leftNode == null) die("Expected key before '=' at " + node.caret.toStringBegin());
                if (isComment(leftNode)) die("Comment instead of key at " + leftNode.caret.toStringBegin());

                i++; // Move to next element (the value)
                if (i >= nodes.size()) die("Expected value after '=' at " + node.caret.toStringBegin());
                
                IodxCst rightNode = nodes.get(i);
                if (isComment(rightNode)) die("Comment instead of value at " + rightNode.caret.toStringBegin());
                if (isDelimiter(rightNode)) die("Expected value at " + rightNode.caret.toStringBegin());
                
                // Replace left element from result (it was the key)
                result.set(result.size() - 1, tuple(result.last(), resolve(rightNode)));
                // For tuples, use the caret that spans from key to value
                Caret tupleCaret = leftNode.caret != null && rightNode.caret != null 
                    ? Caret.startEnd(leftNode.caret, rightNode.caret) 
                    : leftNode.caret; // fallback to key caret
                carets.set(carets.size() - 1, tupleCaret);
                
                leftNode = null;
            } else {
                // Regular element - resolve and add to result
                leftNode = node;
                result.add(resolve(node));
                carets.add(node.caret);
            }
        }
        
        return new IodxEntity(name, result, caret, carets);
    }

    private static boolean isDelimiter(IodxCst node) {
        return "ANY_OPERATOR".equals(node.type) && DELIMITER.equals(node.value);
    }

    private static boolean isComment(IodxCst node) {
        return "COMMENT_SINGLE_LINE".equals(node.type) || "COMMENT_MULTI_LINE".equals(node.type);
    }
}