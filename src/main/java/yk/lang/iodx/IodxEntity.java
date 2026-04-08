package yk.lang.iodx;

import yk.lang.iodx.utils.Caret;
import yk.ycollections.Tuple;
import yk.ycollections.YList;

import java.util.Objects;

import static yk.ycollections.Tuple.tuple;

//TODO rename
//  Node
//  AstNode ?
//  Entity
//  Something, Smthng
//  Item
//  Thing, Thingy
//  Piece
public class IodxEntity {
    public String name;
    // Can contain primitives, strings, tuples, or other IodxEntity
    public YList children;
    public Caret caret;
    // Contains carets corresponding to children (because not all child classes can contain them)
    public YList<Caret> childrenCarets;

    public IodxEntity(String name, YList children) {
        this.name = name;
        this.children = children;
        this.caret = null;
        this.childrenCarets = null;
    }

    public IodxEntity(String name, YList children, Caret caret) {
        this.name = name;
        this.children = children;
        this.caret = caret;
        this.childrenCarets = null;
    }

    public IodxEntity(String name, YList children, Caret caret, YList<Caret> childrenCarets) {
        this.name = name;
        this.children = children;
        this.caret = caret;
        this.childrenCarets = childrenCarets;
    }

    public boolean containsKey(Object k) {
        return children.isAny(o -> o instanceof Tuple && k.equals(((Tuple<?, ?>) o).a));
    }

    public Object get(Object k) {
        Object result = children.find(o -> o instanceof Tuple && k.equals(((Tuple<?, ?>) o).a));
        return result == null ? null : ((Tuple)result).b;
    }

    public Object getOr(Object k, Object or) {
        Object result = children.find(o -> o instanceof Tuple && k.equals(((Tuple<?, ?>) o).a));
        return result == null ? or : ((Tuple)result).b;
    }

    public YList<Tuple> filterFields() {
        return children.filter(o -> o instanceof Tuple);
    }

    public IodxEntity withReplace(String key, Object value) {
        if (key == null) throw new RuntimeException("key is null");
        return new IodxEntity(name,
            children.map(c -> c instanceof Tuple && key.equals(((Tuple) c).a) ? tuple(key, value) : c),
            caret,
            childrenCarets);
    }

    public static class IodxComment {
        public boolean isOneLine;
        public String text;

        public IodxComment(boolean isOneLine, String text) {
            this.isOneLine = isOneLine;
            this.text = text;
        }

        @Override
        public String toString() {
            return "IodxComment{" +
                    "isOneLine=" + isOneLine +
                    ", text='" + text + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IodxComment that = (IodxComment) o;
            return isOneLine == that.isOneLine && Objects.equals(text, that.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(isOneLine, text);
        }
    }

    @Override
    public String toString() {
        return "IodxEntity{" + (name == null ? "" : "name='" + name + "', ") + "children=" + children + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IodxEntity that = (IodxEntity) o;
        return Objects.equals(name, that.name) && Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, children);
    }
}
