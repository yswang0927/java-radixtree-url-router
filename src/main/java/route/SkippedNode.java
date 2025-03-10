package route;

/**
 * @author wangyongshan
 */
public class SkippedNode {
    String path;
    RouteNode node;
    int paramsCount;

    SkippedNode(String path, RouteNode node, int paramsCount) {
        this.path = path;
        this.node = node;
        this.paramsCount = paramsCount;
    }

    public String getPath() {
        return path;
    }

    public RouteNode getNode() {
        return node;
    }

    public int getParamsCount() {
        return paramsCount;
    }

    @Override
    public String toString() {
        return "SkippedNode{" +
            "path='" + path + '\'' +
            ", node=" + node +
            ", paramsCount=" + paramsCount +
            '}';
    }
}

