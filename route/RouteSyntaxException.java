package route;

/**
 * 路由语法异常
 *
 * @author wangyongshan
 */
public class RouteSyntaxException extends RuntimeException {
    public RouteSyntaxException() {
        super();
    }

    public RouteSyntaxException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable initCause(Throwable cause) {
        return null;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }

    @Override
    public void setStackTrace(StackTraceElement[] stackTrace) {
        // ignore
    }
}

