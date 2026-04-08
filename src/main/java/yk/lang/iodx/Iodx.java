package yk.lang.iodx;

import yk.lang.iodx.congocc.IodxCstParser;
import yk.ycollections.YList;

public class Iodx {

    public static Object readIodxEntity(String s) {
        return readIodxEntities(s).assertSize(1).first();
    }

    public static YList<Object> readIodxEntities(String s) {
        return IodxEntityFromCst.translate(IodxCstParser.parse(s).children);
    }

    public static String printIodxEntity(Object s) {
        return new IodxPrinter().print(s);
    }

    public static String printIodxEntities(YList<Object> entities) {
        return new IodxPrinter().printBody(entities);
    }

    public static Object readJava(String s, Class... cc) {
        return new IodxJavaFromEntity()
            .addImport(cc)
            .deserialize(readIodxEntity(s));
    }

    public static <T> T readJava(Class<T> clazz, String s, Class... cc) {
        return (T) new IodxJavaFromEntity()
            .addImport(clazz)
            .addImport(cc)
            .deserialize(readIodxEntity(s));
    }

    public static <T> T readJavaBody(Class<T> clazz, String text, Class... cc) {
        return (T) new IodxJavaFromEntity()
            .addImport(cc)
            .deserializeObject(null, clazz, readIodxEntities(text));
    }

    public static String printJava(Object o) {
        return printIodxEntity(new IodxJavaToEntity().serialize(o));
    }

    public static String printJavaBody(Object o) {
        return printIodxEntities(((IodxEntity) new IodxJavaToEntity().serialize(o)).children);
    }
}
