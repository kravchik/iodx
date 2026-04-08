package yk.lang.iodx;

import org.junit.Test;

public class TestJavaSerializationCases {

    @Test
    public void testAllCases() {
    //    boolean errors = false;
    //    YList<String> result = al();
    //
    //    YList<Object> oo = Iodx.readIodxEntities(readResource("serialization.cases.smoke.iodx"));
    //    for (Object o : oo) {
    //        if (o instanceof IodxEntity) {
    //
    //            IodxEntity ye = (IodxEntity) o;
    //            IodxEntity currentEntity = new IodxEntity(ye.name, al());
    //
    //            String java = (String) ye.get("java");
    //            if (java == null) {
    //                errors = true;
    //                currentEntity.children.add(new IodxEntity.IodxComment(true, "!!! absent 'java' field"));
    //            }
    //
    //            String expected = (String) ye.get("expected");
    //            if (expected == null) {
    //                errors = true;
    //                currentEntity.children.add(new IodxEntity.IodxComment(true, "!!! absent 'expected' field"));
    //            }
    //
    //            //TODO default imports in test case and globally
    //            Object data = IodxJava.deserialize(expected);
    //
    //            for (Object child : ye.children) {
    //                System.out.println(child);
    //                if (child instanceof Tuple) {
    //                    Tuple t = (Tuple) child;
    //                    if (expected != null && "java".equals(t.a)) {
    //                        if (!java.equals(data.toString())) {
    //                            errors = true;
    //                            currentEntity.children.add(new Tuple<>("java", data.toString()));
    //                        } else {
    //                            currentEntity.children.add(child);
    //                        }
    //                    } else if ("expected".equals(t.a)) {
    //                        currentEntity.children.add(child);
    //                    } else if (expected != null && "alternative".equals(t.a)) {
    //                        Object altActual = IodxJava.deserialize((String) t.b);
    //                        if (!data.equals(altActual)) {
    //                            errors = true;
    //                            currentEntity.children.add(new Tuple<>("alternative", "NOT EQUALS"));
    //                        } else {
    //                            currentEntity.children.add(new Tuple<>("alternative", t.b));
    //                        }
    //                    } else {
    //                        currentEntity.children.add(t);
    //                    }
    //                } else {
    //                    currentEntity.children.add(child);
    //                }
    //            }
    //            System.out.println(currentEntity);
    //            result.add(Iodx.printIodxEntity(currentEntity));
    //        } else {
    //            result.add(Iodx.printIodxEntities(al(o)));
    //        }
    //    }
    //    //errors = true;
    //    if (errors) assertEquals(readResource("serialization.cases.smoke.iodx"), result.toString("\n\n"));
    }

}
