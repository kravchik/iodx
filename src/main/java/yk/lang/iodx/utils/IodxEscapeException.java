package yk.lang.iodx.utils;

public class IodxEscapeException extends IllegalArgumentException {
    public final int offset;
    public final int length;

    public IodxEscapeException(String message, int offset, int length) {
        super(message);
        this.offset = offset;
        this.length = Math.max(1, length);
    }
}
