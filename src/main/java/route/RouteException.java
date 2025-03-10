package route;

/**
 * 路由异常
 *
 * @author wangyongshan
 */
public class RouteException extends RuntimeException {
    public RouteException() {
        super();
    }

    public RouteException(String message) {
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
