package route;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 测试用例。
 *
 * @author wangyongshan
 */
public class ExampleTest {

    public static void main(String[] args) {
        testTreeAddAndGet();
        testTreeWildcard();
        testUnescapeParameters();
        testTreeWildcardConflict();
        testTreeChildConflict();
        testTreeDuplicatePath();
        testEmptyWildcardName();
        testTreeCatchAllConflict();
        testTreeCatchAllConflictRoot();
        testTreeDoubleWildcard();
        testTreeTrailingSlashRedirect();
        testTreeRootTrailingSlashRedirect();
        testRedirectTrailingSlash();
        testTreeFindCaseInsensitivePath();
        testPerformance();
    }

    private static void testTreeAddAndGet() {
        String[] routes = {
            "/hi",
            "/contact",
            "/co",
            "/c",
            "/a",
            "/ab",
            "/doc/",
            "/doc/go_faq.html",
            "/doc/go1.html",
            "/α",
            "/β"
        };

        RouteNode tree = new RouteNode();
        for (String route : routes) {
            tree.addRoute(route, new HandlersChain() {});
        }

        Object[][] checks = {
            {"/a", false, "/a", "[]"},
            {"/", true, "", "[]"},
            {"/hi", false, "/hi", "[]"},
            {"/contact", false, "/contact", "[]"},
            {"/co", false, "/co", "[]"},
            {"/con", true, "", "[]"},  // key mismatch
            {"/cona", true, "", "[]"}, // key mismatch
            {"/no", true, "", "[]"},   // no matching child
            {"/ab", false, "/ab", "[]"},
            {"/α", false, "/α", "[]"},
            {"/β", false, "/β", null}
        };

        for (Object[] check : checks) {
            RouteInfo matched = tree.getValue((String)check[0], new Params(), new ArrayList<>(), false);
            if (!String.valueOf(check[2]).equals(matched.fullPath)) {
                throw new RuntimeException("测试失败："+ check[0]);
            }
        }
    }

    private static void testTreeWildcard() {
        String[] routes = {
            "/",
            "/cmd/:tool/",
            "/cmd/:tool/:sub",
            "/cmd/whoami",
            "/cmd/whoami/root",
            "/cmd/whoami/root/",
            "/src/*filepath",
            "/search/",
            "/search/:query",
            "/search/gin-gonic",
            "/search/google",
            "/user_:name",
            "/user_:name/about",
            "/files/:dir/*filepath",
            "/doc/",
            "/doc/go_faq.html",
            "/doc/go1.html",
            "/info/:user/public",
            "/info/:user/project/:project",
            "/info/:user/project/golang",
            "/aa/*xx",
            "/ab/*xx",
            "/:cc",
            "/c1/:dd/e",
            "/c1/:dd/e1",
            "/:cc/cc",
            "/:cc/:dd/ee",
            "/:cc/:dd/:ee/ff",
            "/:cc/:dd/:ee/:ff/gg",
            "/:cc/:dd/:ee/:ff/:gg/hh",
            "/get/test/abc/",
            "/get/:param/abc/",
            "/something/:paramname/thirdthing",
            "/something/secondthing/test",
            "/get/abc",
            "/get/:param",
            "/get/abc/123abc",
            "/get/abc/:param",
            "/get/abc/123abc/xxx8",
            "/get/abc/123abc/:param",
            "/get/abc/123abc/xxx8/1234",
            "/get/abc/123abc/xxx8/:param",
            "/get/abc/123abc/xxx8/1234/ffas",
            "/get/abc/123abc/xxx8/1234/:param",
            "/get/abc/123abc/xxx8/1234/kkdd/12c",
            "/get/abc/123abc/xxx8/1234/kkdd/:param",
            "/get/abc/:param/test",
            "/get/abc/123abd/:param",
            "/get/abc/123abddd/:param",
            "/get/abc/123/:param",
            "/get/abc/123abg/:param",
            "/get/abc/123abf/:param",
            "/get/abc/123abfff/:param"
        };

        RouteNode tree = new RouteNode();
        for (String route : routes) {
            tree.addRoute(route, new HandlersChain() {});
        }

        Object[][] checks = {
            {"/", false, "/", "[]"},
            {"/cmd/test", true, "/cmd/:tool/", "[Param{key='tool', value='test'}]"},
            {"/cmd/test/", false, "/cmd/:tool/", "[Param{key='tool', value='test'}]"},
            {"/cmd/test/3", false, "/cmd/:tool/:sub", "[Param{key='tool', value='test'}, Param{key='sub', value='3'}]"},
            {"/cmd/who", true, "/cmd/:tool/", "[Param{key='tool', value='who'}]"},
            {"/cmd/who/", false, "/cmd/:tool/", "[Param{key='tool', value='who'}]"},
            {"/cmd/whoami", false, "/cmd/whoami", "[]"},
            {"/cmd/whoami/", true, "/cmd/whoami", "[]"},
            {"/cmd/whoami/r", false, "/cmd/:tool/:sub", "[Param{key='tool', value='whoami'}, Param{key='sub', value='r'}]"},
            {"/cmd/whoami/r/", true, "/cmd/:tool/:sub", "[Param{key='tool', value='whoami'}, Param{key='sub', value='r'}]"},
            {"/cmd/whoami/root", false, "/cmd/whoami/root", "[]"},
            {"/cmd/whoami/root/", false, "/cmd/whoami/root/", "[]"},
            {"/src/", false, "/src/*filepath", "[Param{key='filepath', value='/'}]"},
            {"/src/some/file.png", false, "/src/*filepath", "[Param{key='filepath', value='/some/file.png'}]"},
            {"/search/", false, "/search/", "[]"},
            {"/search/someth!ng+in+ünìcodé", false, "/search/:query", "[Param{key='query', value='someth!ng+in+ünìcodé'}]"},
            {"/search/someth!ng+in+ünìcodé/", true, "", "[Param{key='query', value='someth!ng+in+ünìcodé'}]"},
            {"/search/gin", false, "/search/:query", "[Param{key='query', value='gin'}]"},
            {"/search/gin-gonic", false, "/search/gin-gonic", "[]"},
            {"/search/google", false, "/search/google", "[]"},
            {"/user_gopher", false, "/user_:name", "[Param{key='name', value='gopher'}]"},
            {"/user_gopher/about", false, "/user_:name/about", "[Param{key='name', value='gopher'}]"},
            {"/files/js/inc/framework.js", false, "/files/:dir/*filepath", "[Param{key='dir', value='js'}, Param{key='filepath', value='/inc/framework.js'}]"},
            {"/info/gordon/public", false, "/info/:user/public", "[Param{key='user', value='gordon'}]"},
            {"/info/gordon/project/go", false, "/info/:user/project/:project", "[Param{key='user', value='gordon'}, Param{key='project', value='go'}]"},
            {"/info/gordon/project/golang", false, "/info/:user/project/golang", "[Param{key='user', value='gordon'}]"},
            {"/aa/aa", false, "/aa/*xx", "[Param{key='xx', value='/aa'}]"},
            {"/ab/ab", false, "/ab/*xx", "[Param{key='xx', value='/ab'}]"},
            {"/a", false, "/:cc", "[Param{key='cc', value='a'}]"},
            // * Error with argument being intercepted
            // new PR handle (/all /all/cc /a/cc)
            // fix PR: https://github.com/gin-gonic/gin/pull/2796
            {"/all", false, "/:cc", "[Param{key='cc', value='all'}]"},
            {"/d", false, "/:cc", "[Param{key='cc', value='d'}]"},
            {"/ad", false, "/:cc", "[Param{key='cc', value='ad'}]"},
            {"/dd", false, "/:cc", "[Param{key='cc', value='dd'}]"},
            {"/dddaa", false, "/:cc", "[Param{key='cc', value='dddaa'}]"},
            {"/aa", false, "/:cc", "[Param{key='cc', value='aa'}]"},
            {"/aaa", false, "/:cc", "[Param{key='cc', value='aaa'}]"},
            {"/aaa/cc", false, "/:cc/cc", "[Param{key='cc', value='aaa'}]"},
            {"/ab", false, "/:cc", "[Param{key='cc', value='ab'}]"},
            {"/abb", false, "/:cc", "[Param{key='cc', value='abb'}]"},
            {"/abb/cc", false, "/:cc/cc", "[Param{key='cc', value='abb'}]"},
            {"/allxxxx", false, "/:cc", "[Param{key='cc', value='allxxxx'}]"},
            {"/alldd", false, "/:cc", "[Param{key='cc', value='alldd'}]"},
            {"/all/cc", false, "/:cc/cc", "[Param{key='cc', value='all'}]"},
            {"/a/cc", false, "/:cc/cc", "[Param{key='cc', value='a'}]"},
            {"/c1/d/e", false, "/c1/:dd/e", "[Param{key='dd', value='d'}]"},
            {"/c1/d/e1", false, "/c1/:dd/e1", "[Param{key='dd', value='d'}]"},
            {"/c1/d/ee", false, "/:cc/:dd/ee", "[Param{key='cc', value='c1'}, Param{key='dd', value='d'}]"},
            {"/cc/cc", false, "/:cc/cc", "[Param{key='cc', value='cc'}]"},
            {"/ccc/cc", false, "/:cc/cc", "[Param{key='cc', value='ccc'}]"},
            {"/deedwjfs/cc", false, "/:cc/cc", "[Param{key='cc', value='deedwjfs'}]"},
            {"/acllcc/cc", false, "/:cc/cc", "[Param{key='cc', value='acllcc'}]"},
            {"/get/test/abc/", false, "/get/test/abc/", "[]"},
            {"/get/te/abc/", false, "/get/:param/abc/", "[Param{key='param', value='te'}]"},
            {"/get/testaa/abc/", false, "/get/:param/abc/", "[Param{key='param', value='testaa'}]"},
            {"/get/xx/abc/", false, "/get/:param/abc/", "[Param{key='param', value='xx'}]"},
            {"/get/tt/abc/", false, "/get/:param/abc/", "[Param{key='param', value='tt'}]"},
            {"/get/a/abc/", false, "/get/:param/abc/", "[Param{key='param', value='a'}]"},
            {"/get/t/abc/", false, "/get/:param/abc/", "[Param{key='param', value='t'}]"},
            {"/get/aa/abc/", false, "/get/:param/abc/", "[Param{key='param', value='aa'}]"},
            {"/get/abas/abc/", false, "/get/:param/abc/", "[Param{key='param', value='abas'}]"},
            {"/something/secondthing/test", false, "/something/secondthing/test", "[]"},
            {"/something/abcdad/thirdthing", false, "/something/:paramname/thirdthing", "[Param{key='paramname', value='abcdad'}]"},
            {"/something/secondthingaaaa/thirdthing", false, "/something/:paramname/thirdthing", "[Param{key='paramname', value='secondthingaaaa'}]"},
            {"/something/se/thirdthing", false, "/something/:paramname/thirdthing", "[Param{key='paramname', value='se'}]"},
            {"/something/s/thirdthing", false, "/something/:paramname/thirdthing", "[Param{key='paramname', value='s'}]"},
            {"/c/d/ee", false, "/:cc/:dd/ee", "[Param{key='cc', value='c'}, Param{key='dd', value='d'}]"},
            {"/c/d/e/ff", false, "/:cc/:dd/:ee/ff", "[Param{key='cc', value='c'}, Param{key='dd', value='d'}, Param{key='ee', value='e'}]"},
            {"/c/d/e/f/gg", false, "/:cc/:dd/:ee/:ff/gg", "[Param{key='cc', value='c'}, Param{key='dd', value='d'}, Param{key='ee', value='e'}, Param{key='ff', value='f'}]"},
            {"/c/d/e/f/g/hh", false, "/:cc/:dd/:ee/:ff/:gg/hh", "[Param{key='cc', value='c'}, Param{key='dd', value='d'}, Param{key='ee', value='e'}, Param{key='ff', value='f'}, Param{key='gg', value='g'}]"},
            {"/cc/dd/ee/ff/gg/hh", false, "/:cc/:dd/:ee/:ff/:gg/hh", "[Param{key='cc', value='cc'}, Param{key='dd', value='dd'}, Param{key='ee', value='ee'}, Param{key='ff', value='ff'}, Param{key='gg', value='gg'}]"},
            {"/get/abc", false, "/get/abc", "[]"},
            {"/get/a", false, "/get/:param", "[Param{key='param', value='a'}]"},
            {"/get/abz", false, "/get/:param", "[Param{key='param', value='abz'}]"},
            {"/get/12a", false, "/get/:param", "[Param{key='param', value='12a'}]"},
            {"/get/abcd", false, "/get/:param", "[Param{key='param', value='abcd'}]"},
            {"/get/abc/123abc", false, "/get/abc/123abc", "[]"},
            {"/get/abc/12", false, "/get/abc/:param", "[Param{key='param', value='12'}]"},
            {"/get/abc/123ab", false, "/get/abc/:param", "[Param{key='param', value='123ab'}]"},
            {"/get/abc/xyz", false, "/get/abc/:param", "[Param{key='param', value='xyz'}]"},
            {"/get/abc/123abcddxx", false, "/get/abc/:param", "[Param{key='param', value='123abcddxx'}]"},
            {"/get/abc/123abc/xxx8", false, "/get/abc/123abc/xxx8", "[]"},
            {"/get/abc/123abc/x", false, "/get/abc/123abc/:param", "[Param{key='param', value='x'}]"},
            {"/get/abc/123abc/xxx", false, "/get/abc/123abc/:param", "[Param{key='param', value='xxx'}]"},
            {"/get/abc/123abc/abc", false, "/get/abc/123abc/:param", "[Param{key='param', value='abc'}]"},
            {"/get/abc/123abc/xxx8xxas", false, "/get/abc/123abc/:param", "[Param{key='param', value='xxx8xxas'}]"},
            {"/get/abc/123abc/xxx8/1234", false, "/get/abc/123abc/xxx8/1234", "[]"},
            {"/get/abc/123abc/xxx8/1", false, "/get/abc/123abc/xxx8/:param", "[Param{key='param', value='1'}]"},
            {"/get/abc/123abc/xxx8/123", false, "/get/abc/123abc/xxx8/:param", "[Param{key='param', value='123'}]"},
            {"/get/abc/123abc/xxx8/78k", false, "/get/abc/123abc/xxx8/:param", "[Param{key='param', value='78k'}]"},
            {"/get/abc/123abc/xxx8/1234xxxd", false, "/get/abc/123abc/xxx8/:param", "[Param{key='param', value='1234xxxd'}]"},
            {"/get/abc/123abc/xxx8/1234/ffas", false, "/get/abc/123abc/xxx8/1234/ffas", "[]"},
            {"/get/abc/123abc/xxx8/1234/f", false, "/get/abc/123abc/xxx8/1234/:param", "[Param{key='param', value='f'}]"},
            {"/get/abc/123abc/xxx8/1234/ffa", false, "/get/abc/123abc/xxx8/1234/:param", "[Param{key='param', value='ffa'}]"},
            {"/get/abc/123abc/xxx8/1234/kka", false, "/get/abc/123abc/xxx8/1234/:param", "[Param{key='param', value='kka'}]"},
            {"/get/abc/123abc/xxx8/1234/ffas321", false, "/get/abc/123abc/xxx8/1234/:param", "[Param{key='param', value='ffas321'}]"},
            {"/get/abc/123abc/xxx8/1234/kkdd/12c", false, "/get/abc/123abc/xxx8/1234/kkdd/12c", "[]"},
            {"/get/abc/123abc/xxx8/1234/kkdd/1", false, "/get/abc/123abc/xxx8/1234/kkdd/:param", "[Param{key='param', value='1'}]"},
            {"/get/abc/123abc/xxx8/1234/kkdd/12", false, "/get/abc/123abc/xxx8/1234/kkdd/:param", "[Param{key='param', value='12'}]"},
            {"/get/abc/123abc/xxx8/1234/kkdd/12b", false, "/get/abc/123abc/xxx8/1234/kkdd/:param", "[Param{key='param', value='12b'}]"},
            {"/get/abc/123abc/xxx8/1234/kkdd/34", false, "/get/abc/123abc/xxx8/1234/kkdd/:param", "[Param{key='param', value='34'}]"},
            {"/get/abc/123abc/xxx8/1234/kkdd/12c2e3", false, "/get/abc/123abc/xxx8/1234/kkdd/:param", "[Param{key='param', value='12c2e3'}]"},
            {"/get/abc/12/test", false, "/get/abc/:param/test", "[Param{key='param', value='12'}]"},
            {"/get/abc/123abdd/test", false, "/get/abc/:param/test", "[Param{key='param', value='123abdd'}]"},
            {"/get/abc/123abdddf/test", false, "/get/abc/:param/test", "[Param{key='param', value='123abdddf'}]"},
            {"/get/abc/123ab/test", false, "/get/abc/:param/test", "[Param{key='param', value='123ab'}]"},
            {"/get/abc/123abgg/test", false, "/get/abc/:param/test", "[Param{key='param', value='123abgg'}]"},
            {"/get/abc/123abff/test", false, "/get/abc/:param/test", "[Param{key='param', value='123abff'}]"},
            {"/get/abc/123abffff/test", false, "/get/abc/:param/test", "[Param{key='param', value='123abffff'}]"},
            {"/get/abc/123abd/test", false, "/get/abc/123abd/:param", "[Param{key='param', value='test'}]"},
            {"/get/abc/123abddd/test", false, "/get/abc/123abddd/:param", "[Param{key='param', value='test'}]"},
            {"/get/abc/123/test22", false, "/get/abc/123/:param", "[Param{key='param', value='test22'}]"},
            {"/get/abc/123abg/test", false, "/get/abc/123abg/:param", "[Param{key='param', value='test'}]"},
            {"/get/abc/123abf/testss", false, "/get/abc/123abf/:param", "[Param{key='param', value='testss'}]"},
            {"/get/abc/123abfff/te", false, "/get/abc/123abfff/:param", "[Param{key='param', value='te'}]"}
        };

        for (Object[] check : checks) {
            String path = (String) check[0];
            String route = (String) check[2];
            String params = (String) check[3];

            RouteInfo matched = tree.getValue(path, new Params(), new ArrayList<>(), false);
            if (!matched.tsr && !route.equals(matched.fullPath)) {
                throw new RuntimeException("测试失败："+ path);
            }

            if (!params.equals(matched.getParams().toString())) {
                throw new RuntimeException(String.format("测试失败< %s >，返回参数不对，预期：%s，实际：%s", path, params, matched.getParams().toString()));
            }
        }

    }

    private static void testUnescapeParameters() {
        String[] routes = {
            "/",
            "/cmd/:tool/:sub",
            "/cmd/:tool/",
            "/src/*filepath",
            "/search/:query",
            "/files/:dir/*filepath",
            "/info/:user/project/:project",
            "/info/:user"
        };

        RouteNode tree = new RouteNode();
        for (String route : routes) {
            tree.addRoute(route, new HandlersChain() {});
        }

        Object[][] checks = {
            {"/", false, "/", "[]"},
            {"/cmd/test/", false, "/cmd/:tool/", "[Param{key='tool', value='test'}]"},
            {"/cmd/test", true, "", "[Param{key='tool', value='test'}]"},
            {"/src/some/file.png", false, "/src/*filepath", "[Param{key='filepath', value='/some/file.png'}]"},
            {"/src/some/file+test.png", false, "/src/*filepath", "[Param{key='filepath', value='/some/file test.png'}]"},
            {"/src/some/file++++%%%%test.png", false, "/src/*filepath", "[Param{key='filepath', value='/some/file++++%%%%test.png'}]"},
            {"/src/some/file%2Ftest.png", false, "/src/*filepath", "[Param{key='filepath', value='/some/file/test.png'}]"},
            {"/search/someth!ng+in+ünìcodé", false, "/search/:query", "[Param{key='query', value='someth!ng in ünìcodé'}]"},
            {"/info/gordon/project/go", false, "/info/:user/project/:project", "[Param{key='user', value='gordon'}, Param{key='project', value='go'}]"},
            {"/info/slash%2Fgordon", false, "/info/:user", "[Param{key='user', value='slash/gordon'}]"},
            {"/info/slash%2Fgordon/project/Project%20%231", false, "/info/:user/project/:project", "[Param{key='user', value='slash/gordon'}, Param{key='project', value='Project #1'}]"},
            {"/info/slash%%%%", false, "/info/:user", "[Param{key='user', value='slash%%%%'}]"},
            {"/info/slash%%%%2Fgordon/project/Project%%%%20%231", false, "/info/:user/project/:project", "[Param{key='user', value='slash%%%%2Fgordon'}, Param{key='project', value='Project%%%%20%231'}]"}
        };

        boolean unescape = true;
        for (Object[] check : checks) {
            String path = (String) check[0];
            String route = (String) check[2];
            String params = (String) check[3];

            RouteInfo matched = tree.getValue(path, new Params(), new ArrayList<>(), unescape);
            if (!matched.tsr && !route.equals(matched.fullPath)) {
                throw new RuntimeException("测试失败："+ path);
            }

            if (!params.equals(matched.getParams().toString())) {
                throw new RuntimeException(String.format("测试失败< %s >，返回参数不对，预期：%s，实际：%s", path, params, matched.getParams().toString()));
            }
        }
    }

    private static void testTreeWildcardConflict() {
        Object[][] routes = {
            {"/cmd/:tool/:sub", false},
            {"/cmd/vet", false},
            {"/foo/bar", false},
            {"/foo/:name", false},
            {"/foo/:names", true},
            {"/cmd/*path", true},
            {"/cmd/:badvar", true},
            {"/cmd/:tool/names", false},
            {"/cmd/:tool/:badsub/details", true},
            {"/src/*filepath", false},
            {"/src/:file", true},
            {"/src/static.json", true},
            {"/src/*filepathx", true},
            {"/src/", true},
            {"/src/foo/bar", true},
            {"/src1/", false},
            {"/src1/*filepath", true},
            {"/src2*filepath", true},
            {"/src2/*filepath", false},
            {"/search/:query", false},
            {"/search/valid", false},
            {"/user_:name", false},
            {"/user_x", false},
            {"/user_:name", false},
            {"/id:id", false},
            {"/id/:id", false},
            {"/static/*file", false},
            {"/static/", true}
        };

        RouteNode tree = new RouteNode();

        for (Object[] check : routes) {
            String path = (String) check[0];
            boolean conflict = (boolean) check[1];
            Exception exp = null;
            try {
                tree.addRoute(path, null);
            } catch (Exception e) {
                exp = e;
            }

            // 应该冲突的，但是没有异常出现，说明检测失败了
            if (conflict && exp == null) {
                throw new RuntimeException("应该出现冲突的，但却没有出现："+ path);
            }

            // 不应该出现冲突的，但是出现了异常，说明检测失败了
            if (!conflict && exp != null) {
                throw new RuntimeException("不应该出现冲突的，但却出现了："+ path, exp);
            }

        }

    }

    private static void testTreeChildConflict() {
        Object[][] routes = {
            {"/cmd/vet", false},
            {"/cmd/:tool", false},
            {"/cmd/:tool/:sub", false},
            {"/cmd/:tool/misc", false},
            {"/cmd/:tool/:othersub", true},
            {"/src/AUTHORS", false},
            {"/src/*filepath", true},
            {"/user_x", false},
            {"/user_:name", false},
            {"/id/:id", false},
            {"/id:id", false},
            {"/:id", false},
            {"/*filepath", true}
        };

        RouteNode tree = new RouteNode();

        for (Object[] check : routes) {
            String path = (String) check[0];
            boolean conflict = (boolean) check[1];

            Exception exp = null;
            try {
                tree.addRoute(path, null);
            } catch (Exception e) {
                exp = e;
            }

            // 应该冲突的，但是没有异常出现，说明检测失败了
            if (conflict && exp == null) {
                throw new RuntimeException("应该出现冲突的，但却没有出现："+ path);
            }

            // 不应该出现冲突的，但是出现了异常，说明检测失败了
            if (!conflict && exp != null) {
                throw new RuntimeException("不应该出现冲突的，但却出现了："+ path, exp);
            }

        }
    }

    private static void testTreeDuplicatePath() {
        String[] routes = {
            "/",
            "/doc/",
            "/src/*filepath",
            "/search/:query",
            "/user_:name",
        };

        RouteNode tree = new RouteNode();
        for (String route : routes) {
            Exception exp = null;
            try {
                tree.addRoute(route, new HandlersChain() {});
            } catch (Exception e) {
                exp = e;
            }

            if (exp != null) {
                throw new RuntimeException("首次注册不应该出现异常的，却出现了："+ route, exp);
            }

            // 重复插入
            exp = null;
            try {
                tree.addRoute(route, null);
            } catch (Exception e) {
                exp = e;
            }

            if (exp == null) {
                throw new RuntimeException("重复注册应该出现异常的，却没有出现："+ route, exp);
            }
        }

        Object[][] checks = {
            {"/", false, "/", "[]"},
            {"/doc/", false, "/doc/", "[]"},
            {"/src/some/file.png", false, "/src/*filepath", "[Param{key='filepath', value='/some/file.png'}]"},
            {"/search/someth!ng+in+ünìcodé", false, "/search/:query", "[Param{key='query', value='someth!ng+in+ünìcodé'}]"},
            {"/user_gopher", false, "/user_:name", "[Param{key='name', value='gopher'}]"}
        };

        for (Object[] check : checks) {
            String path = (String) check[0];
            String route = (String) check[2];
            String params = (String) check[3];

            RouteInfo matched = tree.getValue(path, new Params(), new ArrayList<>(), false);
            if (!matched.tsr && !route.equals(matched.fullPath)) {
                throw new RuntimeException("测试失败："+ path);
            }

            if (!params.equals(matched.getParams().toString())) {
                throw new RuntimeException(String.format("测试失败< %s >，返回参数不对，预期：%s，实际：%s", path, params, matched.getParams().toString()));
            }
        }
    }

    private static void testEmptyWildcardName() {
        String[] routes = {
            "/user:",
            "/user:/",
            "/cmd/:/",
            "/src/*"
        };

        RouteNode tree = new RouteNode();
        for (String route : routes) {
            Exception exp = null;
            try {
                tree.addRoute(route, null);
            } catch (Exception e) {
                exp = e;
            }
            if (exp == null) {
                throw new RuntimeException("路由缺少 wildcward name 却没有报错");
            }
        }

    }

    private static void testTreeCatchAllConflict() {
        Object[][] routes = {
            // [route, conflict]
            {"/src/*filepath/x", true},
            {"/src2/", false},
            {"/src2/*filepath/x", true},
            {"/src3/*filepath", false},
            {"/src3/*filepath/x", true},
        };

        RouteNode tree = new RouteNode();

        for (Object[] route : routes) {
            String path = (String) route[0];
            boolean conflict = (boolean) route[1];

            Exception exp = null;
            try {
                tree.addRoute(path, null);
            } catch (Exception e) {
                exp = e;
            }

            if (conflict) {
                if (exp == null) {
                    throw new RuntimeException(String.format("路由<%s>应该无效且抛出异常的，但是没有抛出异常", path));
                }
            } else if (exp != null) {
                throw new RuntimeException(String.format("路由<%s>应该正确的，但是却抛出了异常", path));
            }
        }

    }

    private static void testTreeCatchAllConflictRoot() {
        Object[][] checks = {
            {"/", false},
            {"/*filepath", true}
        };

        RouteNode tree = new RouteNode();

        for (Object[] check : checks) {
            String path = (String) check[0];
            boolean conflict = (boolean) check[1];

            Exception exp = null;
            try {
                tree.addRoute(path, null);
            } catch (Exception e) {
                exp = e;
            }

            // 应该冲突的，但是没有异常出现，说明检测失败了
            if (conflict && exp == null) {
                throw new RuntimeException("应该出现冲突的，但却没有出现："+ path);
            }

            // 不应该出现冲突的，但是出现了异常，说明检测失败了
            if (!conflict && exp != null) {
                throw new RuntimeException("不应该出现冲突的，但却出现了："+ path, exp);
            }

        }

    }

    private static void testTreeDoubleWildcard() {
        String[] routes = {
            "/:foo:bar",
            "/:foo:bar/",
            "/:foo*bar"
        };

        RouteNode tree = new RouteNode();
        for (String route : routes) {
            Exception exp = null;
            try {
                tree.addRoute(route, null);
            } catch (Exception e) {
                exp = e;
            }

            if (exp == null) {
                throw new RuntimeException("应该抛出异常(only one wildcard per path segment is allowed)，却没有抛出。");
            }
        }

    }

    private static void testTreeTrailingSlashRedirect() {
        String[] routes = {
            "/hi",
            "/b/",
            "/search/:query",
            "/cmd/:tool/",
            "/src/*filepath",
            "/x",
            "/x/y",
            "/y/",
            "/y/z",
            "/0/:id",
            "/0/:id/1",
            "/1/:id/",
            "/1/:id/2",
            "/aa",
            "/a/",
            "/admin",
            "/admin/:category",
            "/admin/:category/:page",
            "/doc",
            "/doc/go_faq.html",
            "/doc/go1.html",
            "/no/a",
            "/no/b",
            "/api/:page/:name",
            "/api/hello/:name/bar/",
            "/api/bar/:name",
            "/api/baz/foo",
            "/api/baz/foo/bar",
            "/blog/:p",
            "/posts/:b/:c",
            "/posts/b/:c/d/",
            "/vendor/:x/*y"
        };

        RouteNode tree = new RouteNode();
        for (String route : routes) {
            Exception exp = null;
            try {
                tree.addRoute(route, new HandlersChain() {});
            } catch (Exception e) {
                exp = e;
            }

            if (exp != null) {
                throw new RuntimeException("路由<"+ route +">第一次注册，不应该抛出异常的，却抛出了异常");
            }
        }

        String[] tsrRoutes = {
            "/hi/",
            "/b",
            "/search/gopher/",
            "/cmd/vet",
            "/src",
            "/x/",
            "/y",
            "/0/go/",
            "/1/go",
            "/a",
            "/admin/",
            "/admin/config/",
            "/admin/config/permissions/",
            "/doc/",
            "/admin/static/",
            "/admin/cfg/",
            "/admin/cfg/users/",
            "/api/hello/x/bar",
            "/api/baz/foo/",
            "/api/baz/bax/",
            "/api/bar/huh/",
            "/api/baz/foo/bar/",
            "/api/world/abc/",
            "/blog/pp/",
            "/posts/b/c/d",
            "/vendor/x"
        };

        for (String path : tsrRoutes) {
            RouteInfo value = tree.getValue(path, null, new ArrayList<>(), false);
            if (value.handlers != null) {
                throw new RuntimeException("Not-null handler for TSR route: "+ path);
            } else if (!value.tsr) {
                throw new RuntimeException("Expected TSR recommendation for route: "+ path);
            }
        }

        String[] noTsrRoutes = {
            "/",
            "/no",
            "/no/",
            "/_",
            "/_/",
            "/api",
            "/api/",
            "/api/hello/x/foo",
            "/api/baz/foo/bad",
            "/foo/p/p"
        };

        for (String path : noTsrRoutes) {
            RouteInfo value = tree.getValue(path, null, new ArrayList<>(), false);
            if (value.handlers != null) {
                throw new RuntimeException("Not-null handler for TSR route: "+ path);
            } else if (value.tsr) {
                throw new RuntimeException("expected no TSR recommendation for route: "+ path);
            }
        }

    }

    private static void testTreeRootTrailingSlashRedirect() {
        RouteNode tree = new RouteNode();
        Exception exp = null;
        try {
            tree.addRoute("/:test", new HandlersChain() {});
        } catch (Exception e) {
            exp = e;
        }

        if (exp != null) {
            throw new RuntimeException("不应该抛出异常的：/:test");
        }

        RouteInfo value = tree.getValue("/", null, new ArrayList<>(), false);
        if (value.handlers != null) {
            throw new RuntimeException("Not-null handler");
        }
        else if (value.tsr) {
            throw new RuntimeException("Expected no TSR recommendation");
        }

    }

    private static void testRedirectTrailingSlash() {
        String[] routes = {
            "/hello/:name",
            "/hello/:name/123",
            "/hello/:name/234"
        };

        RouteNode tree = new RouteNode();
        for (String route : routes) {
            tree.addRoute(route, new HandlersChain() {});
        }

        RouteInfo value = tree.getValue("/hello/abx/", new Params(), new ArrayList<>(), false);
        if (value.tsr != true) {
            throw new RuntimeException("want true, is false");
        }
    }

    private static void testTreeFindCaseInsensitivePath() {
        RouteNode tree = new RouteNode();

        String longPath = "/l" + strRepeat("o", 128) + "ng";
        String lOngPath = "/l" + strRepeat("O", 128) + "ng/";

        String[] routes = {
            "/hi",
            "/b/",
            "/ABC/",
            "/search/:query",
            "/cmd/:tool/",
            "/src/*filepath",
            "/x",
            "/x/y",
            "/y/",
            "/y/z",
            "/0/:id",
            "/0/:id/1",
            "/1/:id/",
            "/1/:id/2",
            "/aa",
            "/a/",
            "/doc",
            "/doc/go_faq.html",
            "/doc/go1.html",
            "/doc/go/away",
            "/no/a",
            "/no/b",
            "/Π",
            "/u/apfêl/",
            "/u/äpfêl/",
            "/u/öpfêl",
            "/v/Äpfêl/",
            "/v/Öpfêl",
            "/w/♬",  // 3 byte
            "/w/♭/", // 3 byte, last byte differs
            "/w/𠜎",  // 4 byte
            "/w/𠜏/", // 4 byte
            "/lang/简体中文",
            longPath
        };

        for (String route : routes) {
            Exception exp = null;
            try {
                tree.addRoute(route, new HandlersChain() {});
            } catch (Exception e) {
                exp = e;
            }

            if (exp != null) {
                throw new RuntimeException("不应该抛出异常的："+ route);
            }
        }

        // Check out == in for all registered routes
        // With fixTrailingSlash = true
        for (String path : routes) {
            if (path.startsWith("/search/")) {
                System.out.println("");
            }
            String foundPath = tree.findCaseInsensitivePath(path, true);
            if (foundPath == null) {
                throw new RuntimeException("Route <"+ path +"> not found!");
            }
            if (!path.equals(foundPath)) {
                throw new RuntimeException(String.format("Wrong result for route '%s': %s", path, foundPath));
            }
        }

        // With fixTrailingSlash = false
        for (String path : routes) {
            String foundPath = tree.findCaseInsensitivePath(path, false);
            if (foundPath == null) {
                throw new RuntimeException("Route <"+ path +"> not found!");
            }
            if (!path.equals(foundPath)) {
                throw new RuntimeException(String.format("Wrong result for route '%s': %s", path, foundPath));
            }
        }

        Object[][] tests = {
            // [in, out, found, slash]
            {"/HI", "/hi", true, false},
            {"/HI/", "/hi", true, true},
            {"/B", "/b/", true, true},
            {"/B/", "/b/", true, false},
            {"/abc", "/ABC/", true, true},
            {"/abc/", "/ABC/", true, false},
            {"/aBc", "/ABC/", true, true},
            {"/aBc/", "/ABC/", true, false},
            {"/abC", "/ABC/", true, true},
            {"/abC/", "/ABC/", true, false},
            {"/SEARCH/QUERY", "/search/QUERY", true, false},
            {"/SEARCH/QUERY/", "/search/QUERY", true, true},
            {"/CMD/TOOL/", "/cmd/TOOL/", true, false},
            {"/CMD/TOOL", "/cmd/TOOL/", true, true},
            {"/SRC/FILE/PATH", "/src/FILE/PATH", true, false},
            {"/x/Y", "/x/y", true, false},
            {"/x/Y/", "/x/y", true, true},
            {"/X/y", "/x/y", true, false},
            {"/X/y/", "/x/y", true, true},
            {"/X/Y", "/x/y", true, false},
            {"/X/Y/", "/x/y", true, true},
            {"/Y/", "/y/", true, false},
            {"/Y", "/y/", true, true},
            {"/Y/z", "/y/z", true, false},
            {"/Y/z/", "/y/z", true, true},
            {"/Y/Z", "/y/z", true, false},
            {"/Y/Z/", "/y/z", true, true},
            {"/y/Z", "/y/z", true, false},
            {"/y/Z/", "/y/z", true, true},
            {"/Aa", "/aa", true, false},
            {"/Aa/", "/aa", true, true},
            {"/AA", "/aa", true, false},
            {"/AA/", "/aa", true, true},
            {"/aA", "/aa", true, false},
            {"/aA/", "/aa", true, true},
            {"/A/", "/a/", true, false},
            {"/A", "/a/", true, true},
            {"/DOC", "/doc", true, false},
            {"/DOC/", "/doc", true, true},
            {"/NO", "", false, true},
            {"/DOC/GO", "", false, true},
            {"/π", "/Π", true, false},
            {"/π/", "/Π", true, true},
            {"/u/ÄPFÊL/", "/u/äpfêl/", true, false},
            {"/u/ÄPFÊL", "/u/äpfêl/", true, true},
            {"/u/ÖPFÊL/", "/u/öpfêl", true, true},
            {"/u/ÖPFÊL", "/u/öpfêl", true, false},
            {"/v/äpfêL/", "/v/Äpfêl/", true, false},
            {"/v/äpfêL", "/v/Äpfêl/", true, true},
            {"/v/öpfêL/", "/v/Öpfêl", true, true},
            {"/v/öpfêL", "/v/Öpfêl", true, false},
            {"/w/♬/", "/w/♬", true, true},
            {"/w/♭", "/w/♭/", true, true},
            {"/w/𠜎/", "/w/𠜎", true, true},
            {"/w/𠜏", "/w/𠜏/", true, true},
            {"/lang/简体中文", "/lang/简体中文", true, false},
            {"/lang/简体中文/", "/lang/简体中文", true, true},
            {lOngPath, longPath, true, true}
        };

        // With fixTrailingSlash = true
        for (Object[] test : tests) {
            String testIn = (String)test[0];
            String testOut = (String)test[1];
            boolean testFound = (boolean)test[2];

            String foundPath = tree.findCaseInsensitivePath(testIn, true);
            boolean found = foundPath != null;
            if (found != testFound || (found && !foundPath.equals(testOut))) {
                throw new RuntimeException(String.format("Wrong result for '%s': got %s, %b; want %s, %b", testIn, foundPath, found, testOut, testFound));
            }
        }

        // With fixTrailingSlash = false
        for (Object[] test : tests) {
            String testIn = (String)test[0];
            String testOut = (String)test[1];
            boolean testFound = (boolean)test[2];
            boolean testSlash = (boolean)test[3];

            String foundPath = tree.findCaseInsensitivePath(testIn, false);
            boolean found = foundPath != null;
            if (testSlash) {
                if (found) { // test needs a trailingSlash fix. It must not be found!
                    throw new RuntimeException(String.format("Found without fixTrailingSlash: %s; got %s", testIn, foundPath));
                }
            }
            else {
                if (found != testFound || (found && !foundPath.equals(testOut))) {
                    throw new RuntimeException(String.format("Wrong result for '%s': got %s, %b; want %s, %b", testIn, foundPath, found, testOut, testFound));
                }
            }

        }
    }


    private static void testPerformance() {
        RouteNode root = new RouteNode();

        int numberOfURLs = 100000; // 要生成的随机 URL 地址数量
        Random random = new Random();
        for (int i = 0; i < numberOfURLs; i++) {
            int numberOfSegments = random.nextInt(4) + 2; // 生成 2 到 5 之间的随机数作为分段数量
            StringBuilder urlBuilder = new StringBuilder();

            // 生成每个分段
            for (int j = 0; j < numberOfSegments; j++) {
                String segment = generateRandomWord(random.nextInt(6) + 3); // 生成长度为 3 到 8 之间的随机单词
                urlBuilder.append("/").append(segment);
            }

            // 输出生成的随机 URL 地址
            root.addRoute(urlBuilder.toString(), new HandlersChain() {});

            if (i % 100 == 0) {
                root.addRoute("/books_"+ i, new HandlersChain() {});
                root.addRoute("/books_"+ i + "/:bookId", new HandlersChain() {});
                root.addRoute(urlBuilder.append("/:param_").append(i).toString(), new HandlersChain() {});
            }
        }

        root.addRoute("/users", new HandlersChain() {});
        root.addRoute("/users/:userId", new HandlersChain() {});

        List<SkippedNode> skippedNodes = new ArrayList<>();
        long s = System.currentTimeMillis();
        RouteInfo value = root.getValue("/users", new Params(), skippedNodes, false);
        System.out.println(value);
        value = root.getValue("/users/9988", new Params(), skippedNodes, false);
        System.out.println(value);
        value = root.getValue("/books_"+ 2, new Params(), skippedNodes, false);
        System.out.println(value);
        value = root.getValue("/books_"+ 100, new Params(), skippedNodes, false);
        System.out.println(value);
        value = root.getValue("/books_"+ 100 + "/1020", new Params(), skippedNodes, false);
        System.out.println(value);
        value = root.getValue("/books_"+ 500, new Params(), skippedNodes, false);
        System.out.println(value);
        System.out.println(String.format("随机注册了 %d 个路由，执行路由匹配耗时：%d ms", numberOfURLs, System.currentTimeMillis() - s));
    }

    // 生成随机单词
    private static String generateRandomWord(int length) {
        Random random = new Random();
        StringBuilder wordBuilder = new StringBuilder();
        // 字母表
        String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        for (int i = 0; i < length; i++) {
            // 生成随机索引，范围是 0 到 字母表长度 - 1
            int index = random.nextInt(alphabet.length());
            // 从字母表中取出对应索引的字母，并添加到 StringBuilder 中
            wordBuilder.append(alphabet.charAt(index));
        }
        // 返回随机单词
        return wordBuilder.toString();
    }

    private static String strRepeat(String str, int repeats) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < repeats; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
