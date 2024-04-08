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
     * 注册路由。
     *
     * @param httpMethod HTTP method
     * @param routePath 注册的路由路径（支持变量</a/:var1>和通配符</a/*action>）
     * @param handlers 路由处理程序
     * @exception RouteException 如果出现路由语法错误、路由定义冲突、路由注册失败等会抛出此异常
     */
    public Router addRoute(String httpMethod, String routePath, HandlersChain handlers) throws RouteException {
        if (httpMethod == null || httpMethod.isEmpty()) {
            throw new RouteException("HTTP method can not be empty");
        }

        if (routePath == null || routePath.isEmpty()) {
            throw new RouteException("Route path can not be empty");
        }

        if (routePath.charAt(0) != '/') {
            throw new RouteException("Route path must begin with '/'");
        }

        RouteNode root = routesMap.get(httpMethod);
        if (root == null) {
            root = routesMap.computeIfAbsent(httpMethod, (String method) -> {
                RouteNode rootNode = new RouteNode();
                rootNode.nType = NodeType.ROOT;
                rootNode.fullPath = "/";
                return rootNode;
            });
        }

        root.addRoute(routePath, handlers);
        return this;
    }

    /**
     * 注册 GET 请求路由。
     *
     * @param routePath 注册的路由路径（支持变量</a/:var1>和通配符</a/*action>）
     * @param handlers 路由处理程序
     * @throws RouteException 如果出现路由语法错误、路由定义冲突、路由注册失败等会抛出此异常
     */
    public Router get(String routePath, HandlersChain handlers) throws RouteException {
        return this.addRoute(HTTP_METHOD_GET, routePath, handlers);
    }

    /**
     * 注册 POST 请求路由。
     *
     * @param routePath 注册的路由路径（支持变量</a/:var1>和通配符</a/*action>）
     * @param handlers 路由处理程序
     * @throws RouteException 如果出现路由语法错误、路由定义冲突、路由注册失败等会抛出此异常
     */
    public Router post(String routePath, HandlersChain handlers) throws RouteException {
        return this.addRoute(HTTP_METHOD_POST, routePath, handlers);
    }

    /**
     * 注册 PUT 请求路由。
     *
     * @param routePath 注册的路由路径（支持变量</a/:var1>和通配符</a/*action>）
     * @param handlers 路由处理程序
     * @throws RouteException 如果出现路由语法错误、路由定义冲突、路由注册失败等会抛出此异常
     */
    public Router put(String routePath, HandlersChain handlers) throws RouteException {
        return this.addRoute(HTTP_METHOD_PUT, routePath, handlers);
    }

    /**
     * 注册 DELETE 请求路由。
     *
     * @param routePath 注册的路由路径（支持变量</a/:var1>和通配符</a/*action>）
     * @param handlers 路由处理程序
     * @throws RouteException 如果出现路由语法错误、路由定义冲突、路由注册失败等会抛出此异常
     */
    public Router delete(String routePath, HandlersChain handlers) throws RouteException {
        return this.addRoute(HTTP_METHOD_DELETE, routePath, handlers);
    }

    /**
     * 注册 OPTIONS 请求路由。
     *
     * @param routePath 注册的路由路径（支持变量</a/:var1>和通配符</a/*action>）
     * @param handlers 路由处理程序
     * @throws RouteException 如果出现路由语法错误、路由定义冲突、路由注册失败等会抛出此异常
     */
    public Router options(String routePath, HandlersChain handlers) throws RouteException {
        return this.addRoute(HTTP_METHOD_OPTIONS, routePath, handlers);
    }

    /**
     * 注册 HEAD 请求路由。
     *
     * @param routePath 注册的路由路径（支持变量</a/:var1>和通配符</a/*action>）
     * @param handlers 路由处理程序
     * @throws RouteException 如果出现路由语法错误、路由定义冲突、路由注册失败等会抛出此异常
     */
    public Router head(String routePath, HandlersChain handlers) throws RouteException {
        return this.addRoute(HTTP_METHOD_HEAD, routePath, handlers);
    }

    /**
     * 注册 PATCH 请求路由。
     *
     * @param routePath 注册的路由路径（支持变量</a/:var1>和通配符</a/*action>）
     * @param handlers 路由处理程序
     * @throws RouteException 如果出现路由语法错误、路由定义冲突、路由注册失败等会抛出此异常
     */
    public Router patch(String routePath, HandlersChain handlers) throws RouteException {
        return this.addRoute(HTTP_METHOD_PATCH, routePath, handlers);
    }

    /**
     * 注册任意请求路由。
     *
     * @param routePath 注册的路由路径（支持变量</a/:var1>和通配符</a/*action>）
     * @param handlers 路由处理程序
     * @throws RouteException 如果出现路由语法错误、路由定义冲突、路由注册失败等会抛出此异常
     */
    public Router any(String routePath, HandlersChain handlers) throws RouteException {
        for (String method : ANY_HTTP_METHODS) {
            this.addRoute(method, routePath, handlers);
        }
        return this;
    }

    /**
     * 根据当前HTTP请求方法和请求路径获取与之匹配的注册路由。
     *
     * @param httpMethod HTTP请求方法
     * @param requestUrl 请求路径
     * @return 与之匹配的注册路由，如果未匹配到，则返回 null
     * @throws RouteException 如果匹配失败则抛出此异常
     */
    public RouteInfo match(String httpMethod, String requestUrl) throws RouteException {
        if (httpMethod == null || httpMethod.isEmpty()) {
            throw new RouteException("HTTP method can not be empty");
        }

        if (requestUrl == null || requestUrl.isEmpty()) {
            throw new RouteException("Request url can not be empty");
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
