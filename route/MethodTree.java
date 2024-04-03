package route;

public class MethodTree {
    private String method;
    private RouteNode root;

    public MethodTree(String method, RouteNode root) {
        this.method = method;
        this.root = root;
    }

    public String getMethod() {
        return method;
    }

    public RouteNode getRoot() {
        return root;
    }

}
