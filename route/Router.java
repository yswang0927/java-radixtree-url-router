package route;

/**
 * URL路由器，用于注册URL路由和匹配URL路由。
 */
public class Router {

    private MethodTrees trees = new MethodTrees();

    public void addRoute(String method, String path, HandlersChain handlers) {
        RouteNode root = trees.get(method);
        if (root == null) {
            root = new RouteNode();
            root.setFullPath("/");
            trees.add(new MethodTree(method, root));
        }

        root.addRoute(path, handlers);
    }


}
