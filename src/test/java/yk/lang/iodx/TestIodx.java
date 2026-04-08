package yk.lang.iodx;

import org.junit.Test;
import yk.ycollections.YList;
import yk.ycollections.YMap;

import static org.junit.Assert.assertEquals;
import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

public class TestIodx {

    public static class Point {
        public int x;
        public int y;

        public Point() {}

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Point)) return false;
            Point other = (Point) obj;
            return x == other.x && y == other.y;
        }
    }

    @Test
    public void testEnity() {
        Object entity = Iodx.readIodxEntity("hello(world)");
        assertEquals(new IodxEntity("hello", al("world")), entity);
        assertEquals("hello(world)", Iodx.printIodxEntity(entity));

        assertEquals(null, Iodx.readIodxEntity("null"));
        assertEquals("null", Iodx.printIodxEntity(null));
    }

    @Test
    public void testEnities() {
        IodxEntity ye = new IodxEntity("hello", al("world"));

        Object entity = Iodx.readIodxEntities("hello(world) hello(world)");
        assertEquals(al(ye, ye), entity);
        assertEquals("hello(world) hello(world)", Iodx.printIodxEntities(al(ye, ye)));

        assertEquals(al(null, null), Iodx.readIodxEntities("null null"));
        assertEquals("null null", Iodx.printIodxEntities(al(null, null)));
    }

    @Test
    public void testJava() {
        Point point = new Point(10, 20);
        String serialized = Iodx.printJava(point);
        assertEquals("Point(x = 10 y = 20)", serialized);
        assertEquals(point, Iodx.readJava(Point.class, serialized));

        serialized = Iodx.printJava(al(point));
        assertEquals("(Point(x = 10 y = 20))", serialized);
        assertEquals(al(point), Iodx.readJava(YList.class, serialized, Point.class));

        serialized = Iodx.printJava(hm("a", point));
        assertEquals("(a = Point(x = 10 y = 20))", serialized);
        assertEquals(hm("a", point), Iodx.readJava(YMap.class, serialized, Point.class));
    }

    @Test
    public void testJavaBody() {
        Point point = new Point(5, 15);

        String bodyText = Iodx.printJavaBody(point);
        assertEquals("x = 5 y = 15", bodyText);
        assertEquals(point, Iodx.readJavaBody(Point.class, bodyText));

        bodyText = Iodx.printJavaBody(al(point, point));
        assertEquals("ref(1 Point(x = 5 y = 15)) ref(1)", bodyText);
        assertEquals(al(point, point), Iodx.readJavaBody(YList.class, bodyText, Point.class));

        bodyText = Iodx.printJavaBody(hm("a", point));
        assertEquals("a = Point(x = 5 y = 15)", bodyText);
        assertEquals(hm("a", point), Iodx.readJavaBody(YMap.class, bodyText, Point.class));
    }
}