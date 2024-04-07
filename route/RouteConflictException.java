package route;

/**
 * 路由冲突异常
 *
 * @author wangyongshan
 */
public class RouteConflictException extends RouteSyntaxException {
    public RouteConflictException() {
        super();
    }

    public RouteConflictException(String message) {
        super(message);
    }
}
