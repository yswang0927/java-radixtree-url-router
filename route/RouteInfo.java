package route;

/**
 * 匹配的路由信息。
 *
 * @author wangyongshan
 */
public class RouteInfo {
    String method = "";
    String fullPath = "";
    Params params = new Params();
    HandlersChain handlers;
    boolean tsr;

    RouteInfo() {
    }

    public String getPath() {
        return fullPath;
    }

    public Params getParams() {
        return params;
    }

    public HandlersChain getHandlers() {
        return handlers;
    }

    public boolean isTsr() {
        return tsr;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return "RouteInfo{" +
            "fullPath='" + fullPath + '\'' +
            ", params=" + params +
            ", tsr=" + tsr +
            ", handlers=" + handlers +
            '}';
    }

}
