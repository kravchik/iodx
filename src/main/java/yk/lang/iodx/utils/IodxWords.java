package yk.lang.iodx.utils;

import yk.ycollections.YList;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created with IntelliJ IDEA.
 * User: yuri
 * Date: 08/01/15
 * Time: 12:56
 */
public class IodxWords {
    public static final String VALUE = "VALUE";
    public static final String CONST = "CONST";
    public static final String IMPORT = "IMPORT";                               //IMPORT

    public static final String TYPE = "TYPE";
    public static final String ARGS = "ARGS";
    public static final String NAMED_ARGS = "NAMED_ARGS";
    public static final String LEFT = "LEFT";
    public static final String REF = "REF";
    public static final String DOT = "DOT";                                     //LEFT NAME TYPE
    public static final String NODE_TYPE = "NODE_TYPE";
    public static final String NAME = "NAME";
    public static final String CARET = "CARET";

    public static final String IODX_NAMED = "IODX_NAMED";
    public static final String IODX_UNNAMED = "IODX_UNNAMED";
    public static final String IODX_ARRAY = "IODX_ARRAY";
    public static final String IODX_MAP = "IODX_MAP";
    public static final String COMMENT_SINGLE_LINE = "COMMENT_SINGLE_LINE";
    public static final String COMMENT_MULTI_LINE = "COMMENT_MULTI_LINE";
    public static final String IODX_RAW_CLASS = "IODX_RAW_CLASS";//class with a name, but ":" isn't addressed

    public static void main(String[] args) {
        checkFields();
    }

    private static void checkFields() {
        YList<Field> fields = Reflector.getAllFieldsInHierarchy(IodxWords.class);
        for (Field field : fields) {
            if (field.getType() == String.class && Modifier.isStatic(field.getModifiers())) {
                String value = Reflector.get(null, field);
                if (!value.toLowerCase().equals(field.getName().toLowerCase())) System.out.println("WARNING: " + field.getName() + " != " + value);
            }
        }
    }
}
