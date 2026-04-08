package yk.lang.iodx.utils;

public class Caret {

    public int beginLine;
    public int beginColumn;
    public int endLine;
    public int endColumn;
    public int beginOffset;
    public int endOffset;

    public Caret(int beginLine, int beginColumn, int endLine, int endColumn, int beginOffset, int endOffset) {
        this.beginLine = beginLine == 0 ? 1 : beginLine;
        this.beginColumn = beginColumn == 0 ? 1 : beginColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
        this.beginOffset = beginOffset;
        this.endOffset = endOffset;
    }

    public static Caret startEnd(Caret startCaret, Caret endCaret) {
        return new Caret(
            startCaret.beginLine, startCaret.beginColumn, 
            endCaret.endLine, endCaret.endColumn,
            startCaret.beginOffset, endCaret.endOffset
        );
    }

    public String toStringBegin() {
        return String.format("%s:%s", beginLine, beginColumn);
    }

    @Override
    public String toString() {
        return String.format("%s:%s .. %s:%s [%s-%s]", beginLine, beginColumn, endLine, endColumn, beginOffset, endOffset);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Caret)) return false;
        Caret caret = (Caret) o;
        return beginLine == caret.beginLine && beginColumn == caret.beginColumn && endLine == caret.endLine && endColumn == caret.endColumn && beginOffset == caret.beginOffset && endOffset == caret.endOffset;
    }

    @Override
    public int hashCode() {
        int result = beginLine;
        result = 31 * result + beginColumn;
        result = 31 * result + endLine;
        result = 31 * result + endColumn;
        result = 31 * result + beginOffset;
        result = 31 * result + endOffset;
        return result;
    }
}
