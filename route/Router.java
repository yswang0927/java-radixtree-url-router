package route;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * URL路由器，用于注册URL路由和匹配URL路由。
 *
 * @author wangyongshan
 */
public class Router {
    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";
    public static final String HTTP_METHOD_DELETE = "DELETE";
    public static final String HTTP_METHOD_HEAD = "HEAD";
    public static final String HTTP_METHOD_OPTIONS = "OPTIONS";
    public static final String HTTP_METHOD_PATCH = "PATCH";
    public static final String HTTP_METHOD_TRACE = "TRACE";
    public static final String HTTP_METHOD_CONNECT = "CONNECT";

    private static final String[] ANY_HTTP_METHODS = {
        HTTP_METHOD_GET, HTTP_METHOD_POST,
        HTTP_METHOD_PUT, HTTP_METHOD_DELETE,
        HTTP_METHOD_HEAD, HTTP_METHOD_OPTIONS,
        HTTP_METHOD_PATCH, HTTP_METHOD_TRACE,
        HTTP_METHOD_CONNECT
    };

    private ConcurrentMap<String, RouteNode> routesMap = new ConcurrentHashMap<>();

    /**
     * 注册路由
     * @param httpMethod HTTP method
     * @param routePath 注册的路由路径（支持变量</a/:var1>和通配符</a/*action>）
     * @param handlers 路由处理程序
     */
    public Router addRoute(String httpMethod, String routePath, HandlersChain handlers) {
        if (httpMethod == null || httpMethod.isEmpty()) {
            throw new RouteSyntaxException("HTTP method can not be empty");
        }

        if (routePath == null || routePath.isEmpty()) {
            throw new RouteSyntaxException("Route path can not be empty");
        }

        if (routePath.charAt(0) != '/') {
            throw new RouteSyntaxException("Route path must begin with '/'");
        }

        RouteNode root = routesMap.get(httpMethod);
        if (root == null) {
            root = routesMap.computeIfAbsent(httpMethod, (String method) -> {
                RouteNode rootNode = new RouteNode();
                rootNode.setFullPath("/");
                return rootNode;
            });
        }

        root.addRoute(routePath, handlers);

        return this;
    }

    public Router get(String routePath, HandlersChain handlers) {
        return this.addRoute(HTTP_METHOD_GET, routePath, handlers);
    }

    public Router post(String routePath, HandlersChain handlers) {
        return this.addRoute(HTTP_METHOD_POST, routePath, handlers);
    }

    public Router put(String routePath, HandlersChain handlers) {
        return this.addRoute(HTTP_METHOD_PUT, routePath, handlers);
    }

    public Router delete(String routePath, HandlersChain handlers) {
        return this.addRoute(HTTP_METHOD_DELETE, routePath, handlers);
    }

    public Router options(String routePath, HandlersChain handlers) {
        return this.addRoute(HTTP_METHOD_OPTIONS, routePath, handlers);
    }

    public Router head(String routePath, HandlersChain handlers) {
        return this.addRoute(HTTP_METHOD_HEAD, routePath, handlers);
    }

    public Router patch(String routePath, HandlersChain handlers) {
        return this.addRoute(HTTP_METHOD_PATCH, routePath, handlers);
    }

    public Router any(String routePath, HandlersChain handlers) {
        for (String method : ANY_HTTP_METHODS) {
            this.addRoute(method, routePath, handlers);
        }
        return this;
    }

    public RouteInfo match(String httpMethod, String requestUrl) {
        if (httpMethod == null || httpMethod.isEmpty()) {
            throw new RouteSyntaxException("HTTP method can not be empty");
        }

        if (requestUrl == null || requestUrl.isEmpty()) {
            throw new RouteSyntaxException("Request url can not be empty");
        }

        RouteNode root = routesMap.get(httpMethod);
        if (root == null) {
            return null;
        }

        RouteInfo routeInfo = root.getValue(requestUrl, new Params(), new ArrayList<>(), false);
        if (routeInfo.isTsr() || routeInfo.handlers == null) {
            return null;
        }

        routeInfo.method = httpMethod;
        return routeInfo;
    }

}
