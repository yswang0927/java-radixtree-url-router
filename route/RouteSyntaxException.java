package route;

/**
 * 路由语法异常
 */
public class RouteSyntaxException extends RuntimeException {
    public RouteSyntaxException() {
        super();
    }

    public RouteSyntaxException(String message) {
        super(message);
    }

}
