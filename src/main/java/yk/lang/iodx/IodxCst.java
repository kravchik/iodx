package yk.lang.iodx;

import yk.lang.iodx.utils.Caret;
import yk.ycollections.YList;
import yk.ycollections.YMap;

import static yk.ycollections.YArrayList.al;
import static yk.ycollections.YHashMap.hm;

public class IodxCst {
    public final String type;
    public final Caret caret;
    public final YList<IodxCst> children;
    public final YMap<String, IodxCst> childByField;

    public Object value;

    public IodxCst() {
        this(null, null, null, null, null);
    }

    public IodxCst(String type, Caret caret, Object value, YList<IodxCst> children, YMap<String, IodxCst> childByField) {
        this.type = type;
        this.caret = caret;
        this.children = children != null ? children : al();
        this.childByField = childByField == null ? hm() : childByField;
        this.value = value;
    }

    public IodxCst(String type, Caret caret, YList<IodxCst> children) {
        this(type, caret, null, children, null);
    }

    public IodxCst(String type, Caret caret, Object value) {
        this(type, caret, value, null, null);
    }


    public IodxCst(String type, Caret caret) {
        this(type, caret, null, null, null);
    }

    @Override
    public String toString() {
        if (children.isEmpty()) {
            return type;
        }
        return type + "(" + children + ")";
    }
}