package route;

/**
 * 路由冲突异常
 */
public class RouteConflictException extends RuntimeException {
    public RouteConflictException() {
        super();
    }

    public RouteConflictException(String message) {
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
