package yk.lang.iodx;

import yk.lang.iodx.congocc.IodxCstLexer;
import yk.lang.iodx.congocc.IodxCstParser;
import yk.lang.iodx.utils.BadException;
import yk.lang.iodx.utils.IodxEscapeUtils;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import java.util.List;
import java.util.Map;

import static yk.ycollections.Tuple.tuple;
import static yk.ycollections.YArrayList.al;

public class IodxPrinter {
    public int maxWidth = 100;
    public int maxLocalWidth = Integer.MAX_VALUE;
    public String tab = "  ";
    public int compactFromLevel = 0;

    public String valueToString(Object valObj) {
        String value;
        if (valObj == null) {
            value = "null";
        } else if (valObj instanceof String || valObj instanceof Character) {
            String value1;
            value1 = valObj.toString();
            boolean woQuotes = withoutQuotes(value1);
            if (!woQuotes) {
                if (value1.contains("'")) value1 = "\"" + IodxEscapeUtils.escapeDoubleQuotes(value1) + "\"";
                else value1 = "'" + IodxEscapeUtils.escapeSingleQuotes(value1) + "'";
            }
            value = value1;
        } else if (valObj instanceof Number) {
            Number valObj1 = (Number) valObj;
            String value1;
            if (valObj1 instanceof Byte) {
                int i = valObj1.intValue();
                if (i < 0) i = i + 256;
                valObj1 = i;
            }
            if (valObj1 instanceof Float) {
                String s = valObj1.toString();
                if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
                value1 = String.format("%sf", s);
            } else if (valObj1 instanceof Double) {
                String s = valObj1.toString();
                if (s.endsWith(".0")) s = s.substring(0, s.length() - 2);
                value1 = String.format("%sd", s);
            } else if (valObj1 instanceof Long) {
                value1 = valObj1 + "l";
            } else if (valObj1 instanceof Integer) {
                value1 = "" + valObj1;
            } else if (valObj1 instanceof Short) {
                value1 = "" + valObj1;
            } else {
                throw new RuntimeException("Should never reach here");
            }
            value = value1;
        } else if (valObj instanceof Boolean) {
            value = valObj.toString();
        } else {
            throw new RuntimeException(String.format("Unsupported const type. Class: %s, Value: %s",
                    valObj.getClass(), valObj));
        }
        return value;
    }

    public boolean withoutQuotes(String value) {
        try {
            Object would = new IodxCstParser(new IodxCstLexer(value)).parseElement().value;
            if (value.equals(would)) return true;
        } catch (Exception | Error ignore) {}
        return false;
    }

    public String print(IodxCst cst) {
        return printCst(0, cst).toString("\n");
    }

    public String print(Object obj) {
        return printObject(0, obj).toString("\n");
    }

    public String printBody(YList elements) {
        return printObjectList(elements, 0, null, null, false).toString("\n");
    }

    private YList<String> printCst(int startAt, IodxCst cst) {
        if (cst == null) return al("null");
        
        switch (cst.type) {
            case "LIST_BODY":
                return printObjectList(cst.children, startAt, null, null, false);
            
            case "NAMED_CLASS":
                String className = getTokenText(cst.childByField.get("name"));
                IodxCst namedBody = cst.childByField.get("body");
                return printObjectList(namedBody.children, startAt, className + "(", ")", true);
            
            case "UNNAMED_CLASS":
                IodxCst unnamedBody = cst.childByField.get("body");
                return printObjectList(unnamedBody.children, startAt, "(", ")", true);
            
            case "COMMENT_SINGLE_LINE":
                return al("//" + getTokenText(cst));
            
            case "COMMENT_MULTI_LINE":
                return al("/*" + getTokenText(cst) + "*/");
            
            case "INTEGER_LITERAL":
            case "FLOATING_POINT_LITERAL":
            case "ANY_LITERAL":
            case "ANY_OPERATOR":
            case "STRING_LITERAL_DQ":
            case "STRING_LITERAL_SQ":
                return al(getTokenText(cst));
            
            case "LEFT_PAREN":
                return al("(");
            
            case "RIGHT_PAREN":
                return al(")");
            
            case "WHITE_SPACE":
                return al(" ");
            
            default:
                return al(cst.type + ":" + getTokenText(cst));
        }
    }

    private YList<String> printObject(int startAt, Object obj) {
        if (obj instanceof IodxCst) {
            return printCst(startAt, (IodxCst) obj);
        } else if (obj instanceof List) {
            return printObjectList((YList) obj, startAt, "(", ")", true);
        } else if (obj instanceof Map) {
            // Map -> unnamed class with key = value pairs
            Map<?, ?> map = (Map<?, ?>) obj;
            if (map.isEmpty()) {
                // Empty map special case -> (=)
                return al("(=)");
            }
            YList<Object> objects = al();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                objects.add(tuple(entry.getKey(), entry.getValue()));
            }
            return printObjectList(objects, startAt, "(", ")", true);
        } else if (obj instanceof Tuple) {
            // Tuple -> key = value (using IodxEntityOutput logic)
            Tuple<?, ?> tuple = (Tuple<?, ?>) obj;
            YList<String> result = printObject(startAt, tuple.a);
            if (result.isEmpty()) BadException.shouldNeverReachHere();
            YList<String> valueResult = printObject(startAt + tab.length(), tuple.b);
            if (valueResult.isEmpty()) BadException.shouldNeverReachHere();
            
            // Append " = value" to the last line of the key
            result.set(result.size() - 1, result.last() + " = " + valueResult.first());
            // Add remaining lines from value if it's multi-line
            for (int i = 1; i < valueResult.size(); i++) {
                result.add(valueResult.get(i));
            }
            return result;
        } else if (obj instanceof IodxEntity.IodxComment) {
            // IodxComment -> comment output
            IodxEntity.IodxComment comment = (IodxEntity.IodxComment) obj;
            if (comment.isOneLine) {
                return al("//" + comment.text);
            } else {
                return al("/*" + comment.text + "*/");
            }
        } else if (obj instanceof IodxEntity) {
            // IodxEntity -> named or unnamed class
            IodxEntity entity = (IodxEntity) obj;
            YList<Object> children = entity.children;
            if (entity.name == null) {
                // Unnamed class
                return printObjectList(children, startAt, "(", ")", true);
            } else {
                // Named class
                return printObjectList(children, startAt, entity.name + "(", ")", true);
            }
        }
        
        // Handle primitives using the same logic as IodxEntityOutput
        return al(valueToString(obj));
    }
    
    private String getTokenText(IodxCst cst) {
        // Handle null value for ANY_LITERAL (null literal)
        if (cst.value == null && "ANY_LITERAL".equals(cst.type)) {
            return "null";
        }
        
        // Use value field for literals that have it
        if (cst.value != null) {
            switch (cst.type) {
                case "INTEGER_LITERAL":
                    return cst.value.toString();
                    
                case "FLOATING_POINT_LITERAL":
                    return formatFloatingPoint(cst.value);
                    
                case "STRING_LITERAL_DQ":
                    return "\"" + IodxEscapeUtils.escapeDoubleQuotes(cst.value.toString()) + "\"";
                    
                case "STRING_LITERAL_SQ":
                    return "'" + IodxEscapeUtils.escapeSingleQuotes(cst.value.toString()) + "'";
                    
                case "ANY_LITERAL":
                case "ANY_OPERATOR":
                    return cst.value.toString();
                    
                default:
                    return cst.value.toString();
            }
        }
        
        // Fallback for nodes without value
        throw new RuntimeException("Can't get token text from node type: " + cst.type);
    }
    
    private String formatFloatingPoint(Object value) {
        if (value instanceof Float) {
            // Float is the default type, no suffix needed
            float f = (Float) value;
            String str = String.format("%.6g", f);
            // Remove trailing zeros and unnecessary decimal point
            if (str.contains(".")) {
                str = str.replaceAll("0+$", "").replaceAll("\\.$", "");
            }
            return str;
        } else if (value instanceof Double) {
            // Double needs 'd' suffix to distinguish from Float
            double d = (Double) value;
            String str = String.format("%.15g", d);
            // Remove trailing zeros and unnecessary decimal point
            if (str.contains(".")) {
                str = str.replaceAll("0+$", "").replaceAll("\\.$", "");
            }
            return str + "d";
        }
        return value.toString();
    }
    

    private int level = 0;

    private YList<String> printObjectList(YList objects, int startAt, String l1, String ln, boolean addTabs) {
        level++;
        YList<String> res = printObjectListImp(objects, startAt, l1, ln, addTabs);
        level--;
        return res;
    }

    private YList<String> printObjectListImp(YList<Object> objects, int startAt, String l1, String ln, boolean addTabs) {
        if ((l1 == null) != (ln == null)) BadException.die("Both prefix and suffix should either null, or not");
        boolean tryCompact = true;
        YList<String> cc = al();
        int commonLength = 0;
        
        for (Object obj : objects) {
            // Special handling for single-line comments to prevent compact mode
            if (obj instanceof IodxEntity.IodxComment) {
                IodxEntity.IodxComment comment = (IodxEntity.IodxComment) obj;
                if (comment.isOneLine) {
                    tryCompact = false;
                }
            }
            
            YList<String> childStrings = printObject(startAt + tab.length(), obj);
            cc.addAll(childStrings);
            if (childStrings.isEmpty()) BadException.shouldNeverReachHere();
            if (childStrings.size() > 1) tryCompact = false;
            else commonLength += childStrings.first().length();
        }
        
        if (tryCompact && level >= compactFromLevel) {
            int estimatedLen = commonLength + Math.max(0, (cc.size() - 1));
            if (l1 != null) estimatedLen += l1.length() + ln.length();
            if (estimatedLen + startAt <= maxWidth && estimatedLen <= maxLocalWidth) {
                String result = l1 == null ? cc.toString(" ") : (l1 + cc.toString(" ") + ln);
                if (result.length() != estimatedLen) {
                    throw new RuntimeException(String.format(
                            "Expected same length, but estimatedLen is %s, actual: %s, in text '%s'", 
                            estimatedLen, result.length(), result));
                }
                return al(result);
            }
        }
        
        if (l1 != null) {
            if (addTabs) return al(l1).withAll(cc.map(c -> tab + c)).with(ln);
            return al(l1).withAll(cc).with(ln);
        } else {
            if (addTabs) return cc.map(c -> tab + c);
            return cc;
        }
    }

    public IodxPrinter setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
        return this;
    }

    public IodxPrinter setMaxLocalWidth(int maxLocalWidth) {
        this.maxLocalWidth = maxLocalWidth;
        return this;
    }

    public IodxPrinter setCompactFromLevel(int compactFromLevel) {
        this.compactFromLevel = compactFromLevel;
        return this;
    }

    public IodxPrinter setTab(String tab) {
        this.tab = tab;
        return this;
    }
}