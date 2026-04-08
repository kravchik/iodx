package yk.lang.iodx;

import yk.lang.iodx.utils.BadException;

public class UtilsForTests {
    public static String readResource(String path) {
        String content = TestIodxPrinterCases.resourceAsString(path);
        if (content == null) {
            throw BadException.die("File " + path + " not found");
        }
        return content;
    }
}
