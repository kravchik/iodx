package yk.lang.iodx.utils;

/**
 * Created with IntelliJ IDEA.
 * User: yuri
 * Date: 05/02/15
 * Time: 20:39
 * <p>
 * COPY OF one from yincubator
 * TODO YadsException
 */
public class BadException extends RuntimeException {
    public BadException() {
    }

    public BadException(String message) {
        super(message);
    }

    public BadException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadException(Throwable cause) {
        super(cause);
    }

    public static BadException die(String message) {
        throw new BadException(message);
    }

    public static BadException die(String message, Object... args) {
        throw new BadException(String.format(message, args));
    }

    public static BadException die(Throwable cause, String message) {
        throw new BadException(message, cause);
    }

    public static BadException die(Throwable cause, String message, Object... args) {
        throw new BadException(String.format(message, args), cause);
    }

    public static BadException die(Throwable cause) {
        throw new BadException(cause);
    }

    public static BadException notImplemented() {
        throw die("not implemented");
    }

    public static BadException notImplemented(String cause) {
        throw die("not implemented: " + cause);
    }

    public static BadException shouldNeverReachHere() {
        return shouldNeverReachHere("");
    }

    public static BadException shouldNeverReachHere(String msg) {
        throw die("should never reach here " + msg);
    }

}
