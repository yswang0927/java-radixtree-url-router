# java-radixtree-url-router

> 2025/3/10 基于tree.go最新版更新代码
> 

高性能Web-URL路由定义注册和匹配算法，从 `https://github.com/gin-gonic/gin/blob/master/tree.go` 翻译过来。

支持静态路由和动态路由：
- 静态路由：`/api/users`
- 命名参数动态路由：`/api/users/:userId`
- 通配符参数动态路由：`/api/actions/*actionName`

用法：
```
Router router = new Router();
// 注册URL路由
router.get("/api/users", HandlersChain);
router.get("/api/users/:userId", HandlersChain);
router.post("/api/users", HandlersChain);
router.put("/api/users/:userId", HandlersChain);

// 测试路由匹配
RouteInfo routeInfo = router.match("GET", "/api/users");
System.out.println(routeInfo);

routeInfo = router.match("GET", "/api/users/10010");
System.out.println(routeInfo);

routeInfo = router.match("POST", "/api/users");
System.out.println(routeInfo);

routeInfo = router.match("PUT", "/api/users/10010");
System.out.println(routeInfo);

routeInfo = router.match("DELETE", "/api/users/10010");
System.out.println(routeInfo);

```

测试用例参见：`ExampleTest.java`
