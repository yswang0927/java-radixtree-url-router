# java-radixtree-url-router

高性能Web-URL路由定义注册和匹配算法，从 `https://github.com/gin-gonic/gin/blob/master/tree.go` 翻译过来。

支持静态路由和动态路由：
- 静态路由：`/api/users`
- 命名参数动态路由：`/api/users/:userId`
- 通配符参数动态路由：`/api/actions/*actionName`

用法参见：`ExampleTest.java`