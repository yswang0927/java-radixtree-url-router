package route;

/**
 * 路由节点类型。
 *
 * @author wangyongshan
 */
public enum NodeType {
    // 根路由
    ROOT,
    // 静态路由
    STATIC,
    // 含有命名参数路由(/users/:userId)
    PARAM,
    // 含有通配符*路由(/actions/*action)
    CATCH_ALL
}
