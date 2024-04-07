package route;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * URL路由器，用于注册URL路由和匹配URL路由。
 */
public class Router {

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

    public NodeValue matchRoute(String httpMethod, String requestUrl) {
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

        return root.getValue(requestUrl, new ArrayList<>(), new ArrayList<>(), false);
    }


}
