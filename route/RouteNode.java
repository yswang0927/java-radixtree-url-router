package route;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 翻译自：https://github.com/gin-gonic/gin/blob/master/tree.go
 *
 * @author wangyongshan
 * @version 1.9.1
 */
public class RouteNode {
    String path = "";
    String indices = "";
    boolean wildChild = false;
    NodeType nType = NodeType.STATIC;
    int priority = 0;
    List<RouteNode> children = new ArrayList<>();
    HandlersChain handlers;
    String fullPath = "";

    RouteNode() {
    }

    RouteNode(String path, String indices, boolean wildChild, NodeType nType, int priority,
                     List<RouteNode> children, HandlersChain handlers, String fullPath) {
        this.path = path == null ? "" : path;
        this.indices = indices == null ? "" : indices;
        this.wildChild = wildChild;
        this.nType = nType == null ? NodeType.STATIC : nType;
        this.priority = priority;
        this.children = children;
        this.handlers = handlers;
        this.fullPath = fullPath == null ? "" : fullPath;
    }

    public void addChild(RouteNode child) {
        if (this.wildChild && this.children.size() > 0) {
            RouteNode wildcardChild = this.children.get(this.children.size() - 1);
            this.children.remove(children.size() - 1);
            this.children.add(child);
            this.children.add(wildcardChild);
        } else {
            this.children.add(child);
        }
    }

    public void addRoute(String path, HandlersChain handlers) {
        if (path == null) {
            return;
        }

        RouteNode n = this;
        String fullPath = path;
        n.priority++;

        // Empty tree
        if (n.path.isEmpty() && (n.children == null || n.children.isEmpty())) {
            n.insertChild(path, fullPath, handlers);
            n.nType = NodeType.ROOT;
            return;
        }

        int parentFullPathIndex = 0;
        walk:
        while (true) {
            // Find the longest common prefix.
            // This also implies that the common prefix contains no ':' or '*'
            // since the existing key can't contain those chars.
            int i = longestCommonPrefix(path, n.path);

            // Split edge
            if (i < n.path.length()) {
                RouteNode child = new RouteNode(
                    n.path.substring(i),
                    n.indices,
                    n.wildChild,
                    NodeType.STATIC,
                    n.priority - 1,
                    n.children,
                    n.handlers,
                    n.fullPath
                );

                n.children = new ArrayList<>();
                n.children.add(child);

                n.indices = String.valueOf(n.path.charAt(i));
                n.path = path.substring(0, i);
                n.handlers = null;
                n.wildChild = false;
                n.fullPath = fullPath.substring(0, parentFullPathIndex + i);
            }

            // Make new node a child of this node
            if (i < path.length()) {
                path = path.substring(i);
                char c = path.charAt(0);

                // '/' after param
                if (n.nType == NodeType.PARAM && c == '/' && n.children.size() == 1) {
                    parentFullPathIndex += n.path.length();
                    n = n.children.get(0);
                    n.priority++;
                    continue walk;
                }

                // Check if a child with the next path byte exists
                for (int j = 0, max = n.indices.length(); j < max; j++) {
                    if (c == n.indices.charAt(j)) {
                        parentFullPathIndex += n.path.length();
                        j = n.incrementChildPrio(j);
                        n = n.children.get(j);
                        continue walk;
                    }
                }

                // Otherwise insert it
                if (c != ':' && c != '*' && n.nType != NodeType.CATCH_ALL) {
                    n.indices += c;
                    RouteNode child = new RouteNode();
                    child.fullPath = fullPath;
                    n.addChild(child);
                    n.incrementChildPrio(n.indices.length() - 1);
                    n = child;
                } else if (n.wildChild) {
                    // inserting a wildcard node, need to check if it conflicts with the existing wildcard
                    n = n.children.get(n.children.size() - 1);
                    n.priority++;

                    // Check if the wildcard matches
                    int nPathLen = n.path.length();
                    if (path.length() >= nPathLen && n.path.equals(path.substring(0, nPathLen)) &&
                        // Adding a child to a catchAll is not possible
                        n.nType != NodeType.CATCH_ALL &&
                        // Check for longer wildcard, e.g. :name and :names
                        (nPathLen >= path.length() || path.charAt(nPathLen) == '/')) {
                        continue walk;
                    }

                    String pathSeg = path;
                    if (n.nType != NodeType.CATCH_ALL) {
                        pathSeg = path.split("/", 2)[0];
                    }

                    String prefix = fullPath.substring(0, fullPath.indexOf(pathSeg)) + n.path;
                    throw new RouteConflictException(String.format("'%s' in new path '%s' conflicts with existing wildcard '%s' in existing prefix '%s'", pathSeg, fullPath, n.path, prefix));
                }

                n.insertChild(path, fullPath, handlers);
                return;
            }

            // Otherwise add handle to current node
            if (n.handlers != null) {
                throw new RuntimeException("handlers are already registered for path '" + fullPath + "'");
            }
            n.handlers = handlers;
            n.fullPath = fullPath;
            return;
        }
    }

    private int incrementChildPrio(int pos) {
        List<RouteNode> cs = this.children;
        cs.get(pos).priority++;
        int prio = cs.get(pos).getPriority();

        // Adjust position (move to front)
        int newPos = pos;
        for (; newPos > 0 && cs.get(newPos - 1).getPriority() < prio; newPos--) {
            // Swap node positions
            RouteNode temp = cs.get(newPos - 1);
            cs.set(newPos - 1, cs.get(newPos));
            cs.set(newPos, temp);
        }

        // Build new index char string
        if (newPos != pos) {
            String prefix = this.indices.substring(0, newPos);
            String indexChar = this.indices.substring(pos, pos + 1);
            String rest = this.indices.substring(newPos, pos) + this.indices.substring(pos + 1);
            this.indices = prefix + indexChar + rest;
        }

        return newPos;
    }

    private void insertChild(String path, String fullPath, HandlersChain handlers) {
        RouteNode n = this;
        for (;;) {
            // Find prefix until first wildcard
            FoundWildcard foundWildcard = findWildcard(path);
            String wildcard = foundWildcard.wildcard;
            int i = foundWildcard.index;
            boolean valid = foundWildcard.valid;

            if (i < 0) { // No wildcard found
                break;
            }
            // The wildcard name must only contain one ':' or '*' character
            if (!valid) {
                throw new RuntimeException("only one wildcard per path segment is allowed, has: '" +
                    wildcard + "' in path '" + fullPath + "'");
            }

            // check if the wildcard has a name
            if (wildcard.length() < 2) {
                throw new RuntimeException("wildcards must be named with a non-empty name in path '" + fullPath + "'");
            }

            if (wildcard.charAt(0) == ':') { // param
                if (i > 0) {
                    // Insert prefix before the current wildcard
                    n.path = path.substring(0, i);
                    path = path.substring(i);
                }

                RouteNode child = new RouteNode();
                child.nType = NodeType.PARAM;
                child.path = wildcard;
                child.fullPath = fullPath;
                n.addChild(child);
                n.wildChild = true;
                n = child;
                n.priority++;

                // if the path doesn't end with the wildcard, then there
                // will be another subpath starting with '/'
                if (wildcard.length() < path.length()) {
                    path = path.substring(wildcard.length());

                    child = new RouteNode();
                    child.priority = 1;
                    child.fullPath = fullPath;
                    n.addChild(child);
                    n = child;
                    continue;
                }

                // Otherwise we're done. Insert the handle in the new leaf
                n.handlers = handlers;
                return;
            }

            // catchAll
            if (i + wildcard.length() != path.length()) {
                throw new RuntimeException("catch-all routes are only allowed at the end of the path in path '" + fullPath + "'");
            }

            if (!n.path.isEmpty() && n.path.charAt(n.path.length() - 1) == '/') {
                String pathSeg = "";
                if (!n.children.isEmpty()) {
                    pathSeg = n.children.get(0).path.split("/", 2)[0];
                }
                throw new RuntimeException("catch-all wildcard '" + path +
                    "' in new path '" + fullPath +
                    "' conflicts with existing path segment '" + pathSeg +
                    "' in existing prefix '" + n.path + pathSeg +
                    "'");
            }

            // currently fixed width 1 for '/'
            i--;
            if (path.charAt(i) != '/') {
                throw new RuntimeException("no / before catch-all in path '" + fullPath + "'");
            }

            n.path = path.substring(0, i);

            // First node: catchAll node with empty path
            RouteNode child = new RouteNode();
            child.wildChild = true;
            child.nType = NodeType.CATCH_ALL;
            child.fullPath = fullPath;

            n.addChild(child);
            n.indices = "/";
            n = child;
            n.priority++;

            // second node: node holding the variable
            child = new RouteNode();
            child.path = path.substring(i);
            child.nType = NodeType.CATCH_ALL;
            child.handlers = handlers;
            child.priority = 1;
            child.fullPath = fullPath;

            n.children = new ArrayList<>();
            n.children.add(child);
            return;
        }

        // If no wildcard was found, simply insert the path and handle
        n.path = path;
        n.handlers = handlers;
        n.fullPath = fullPath;
    }

    private static int longestCommonPrefix(String path, String nodePath) {
        int minLen = Math.min(path.length(), nodePath.length());
        int i = 0;
        for (; i < minLen && path.charAt(i) == nodePath.charAt(i); i++) ;
        return i;
    }

    private FoundWildcard findWildcard(String path) {
        // Find start
        for (int start = 0; start < path.length(); start++) {
            char c = path.charAt(start);
            // A wildcard starts with ':' (param) or '*' (catch-all)
            if (c != ':' && c != '*') {
                continue;
            }

            // Find end and check for invalid characters
            boolean valid = true;
            for (int end = start + 1; end < path.length(); end++) {
                char ch = path.charAt(end);
                if (ch == '/') {
                    return new FoundWildcard(path.substring(start, end), start, valid);
                } else if (ch == ':' || ch == '*') {
                    valid = false;
                }
            }
            return new FoundWildcard(path.substring(start), start, valid);
        }
        return new FoundWildcard("", -1, false);
    }

    public NodeValue getValue(String path, List<Param> params, List<SkippedNode> skippedNodes, boolean unescape) {
        int globalParamsCount = 0;
        NodeValue value = new NodeValue();

        RouteNode n = this;
        // Outer loop for walking the tree
        walk:
        while (true) {
            String prefix = n.path;
            int prefixLen = prefix.length();
            if (path.length() > prefixLen) {
                if (path.substring(0, prefixLen).startsWith(prefix)) {
                    path = path.substring(prefixLen);

                    // Try all the non-wildcard children first by matching the indices
                    char idxc = path.charAt(0);
                    for (int i = 0, iLen = n.indices.length(); i < iLen; i++) {
                        char c = n.indices.charAt(i);
                        if (c == idxc) {
                            if (n.wildChild) {
                                //int index = skippedNodes.size();
                                skippedNodes.add(new SkippedNode(
                                    prefix + path,
                                    new RouteNode(
                                        n.path,
                                        null,
                                        n.wildChild,
                                        n.nType,
                                        n.priority,
                                        n.children,
                                        n.handlers,
                                        n.fullPath
                                    ),
                                    globalParamsCount
                                ));
                            }
                            n = n.children.get(i);
                            continue walk;
                        }
                    }

                    if (!n.wildChild) {
                        // If the path at the end of the loop is not equal to '/' and the current node has no child nodes
                        // the current node needs to roll back to last valid skippedNode
                        if (!path.equals("/")) {
                            for (int length = skippedNodes.size(); length > 0; length--) {
                                SkippedNode skippedNode = skippedNodes.get(length - 1);
                                skippedNodes.remove(length - 1);
                                if (skippedNode.getPath().endsWith(path)) {
                                    path = skippedNode.getPath();
                                    n = skippedNode.getNode();
                                    if (value.params != null) {
                                        value.params = value.params.subList(0, skippedNode.getParamsCount());
                                    }
                                    globalParamsCount = skippedNode.getParamsCount();
                                    continue walk;
                                }
                            }
                        }

                        value.tsr = path.equals("/") && n.handlers != null;
                        return value;
                    }

                    // Handle wildcard child, which is always at the end of the array
                    n = n.children.get(n.children.size() - 1);
                    globalParamsCount++;

                    switch (n.nType) {
                        case PARAM:
                            int end = 0;
                            while (end < path.length() && path.charAt(end) != '/') {
                                end++;
                            }

                            if (params != null) {
                                if (value.params == null) {
                                    value.params = params;
                                }

                                //int i = value.params.size();
                                String val = path.substring(0, end);
                                if (unescape) {
                                    try {
                                        val = URLDecoder.decode(val, StandardCharsets.UTF_8);
                                    } catch (Exception e) {
                                        // ignore
                                    }
                                }
                                value.params.add(new Param(n.path.substring(1), val));
                            }

                            if (end < path.length()) {
                                if (!n.children.isEmpty()) {
                                    path = path.substring(end);
                                    n = n.children.get(0);
                                    continue walk;
                                }
                                value.tsr = path.length() == end + 1;
                                return value;
                            }

                            value.handlers = n.handlers;
                            if (value.handlers != null) {
                                value.fullPath = n.fullPath;
                                return value;
                            }
                            if (n.children.size() == 1) {
                                n = n.children.get(0);
                                value.tsr = (n.path.equals("/") && n.handlers != null) || (n.path.isEmpty() && n.indices.equals("/"));
                            }
                            return value;

                        case CATCH_ALL:
                            if (params != null) {
                                if (value.params == null) {
                                    value.params = params;
                                }
                                //int i = value.params.size();
                                String val = path;
                                if (unescape) {
                                    try {
                                        val = URLDecoder.decode(val, StandardCharsets.UTF_8);
                                    } catch (Exception e) {
                                        // ignore
                                    }
                                }
                                value.params.add(new Param(n.path.substring(2), val));
                            }
                            value.handlers = n.handlers;
                            value.fullPath = n.fullPath;
                            return value;

                        default:
                            throw new IllegalStateException("Invalid node type");
                    }
                }
            }

            if (path.equals(prefix)) {
                // If the current path does not equal '/' and the node does not have a registered handle and the most recently matched node has a child node
                // the current node needs to roll back to last valid skippedNode
                if (n.handlers == null && !path.equals("/")) {
                    for (int length = skippedNodes.size(); length > 0; length--) {
                        SkippedNode skippedNode = skippedNodes.get(length - 1);
                        skippedNodes.remove(length - 1);
                        if (skippedNode.path.endsWith(path)) {
                            path = skippedNode.path;
                            n = skippedNode.node;
                            if (value.params != null) {
                                value.params = value.params.subList(0, skippedNode.paramsCount);
                            }
                            globalParamsCount = skippedNode.paramsCount;
                            continue walk;
                        }
                    }
                }

                // We should have reached the node containing the handle.
                // Check if this node has a handle registered.
                value.handlers = n.handlers;
                if (value.handlers != null) {
                    value.fullPath = n.fullPath;
                    return value;
                }

                // If there is no handle for this route, but this route has a
                // wildcard child, there must be a handle for this path with an
                // additional trailing slash
                if (path.equals("/") && n.wildChild && n.nType != NodeType.ROOT) {
                    value.tsr = true;
                    return value;
                }

                if (path.equals("/") && n.nType == NodeType.STATIC) {
                    value.tsr = true;
                    return value;
                }

                for (int i = 0; i < n.indices.length(); i++) {
                    if (n.indices.charAt(i) == '/') {
                        n = n.children.get(i);
                        value.tsr = (n.path.length() == 1 && n.handlers != null) ||
                            (n.nType == NodeType.CATCH_ALL && n.children.get(0).handlers != null);
                        return value;
                    }
                }

                return value;
            }

            value.tsr = path.equals("/") ||
                (prefix.length() == path.length() + 1 && prefix.charAt(path.length()) == '/' &&
                    path.equals(prefix.substring(0, prefix.length() - 1)) && n.handlers != null);

            if (!value.tsr && !path.equals("/")) {
                for (int length = skippedNodes.size(); length > 0; length--) {
                    SkippedNode skippedNode = skippedNodes.get(length - 1);
                    skippedNodes.remove(length - 1);
                    if (skippedNode.path.endsWith(path)) {
                        path = skippedNode.path;
                        n = skippedNode.node;
                        if (value.params != null) {
                            value.params = value.params.subList(0, skippedNode.paramsCount);
                        }
                        globalParamsCount = skippedNode.paramsCount;
                        continue walk;
                    }
                }
            }

            return value;
        }
    }

    public String findCaseInsensitivePath(String path, boolean fixTrailingSlash) {
        return findCaseInsensitivePathRec(path, "", new byte[4], fixTrailingSlash);
    }

    // Recursive case-insensitive lookup function used by n.findCaseInsensitivePath
    private String findCaseInsensitivePathRec(String path, String ciPath, byte[] rb, boolean fixTrailingSlash) {
        RouteNode n = this;
        int npLen = n.path.length();

        // Outer loop for walking the tree
        walk:
        while (path.length() >= npLen
            && (npLen == 0 || path.substring(1, npLen).equalsIgnoreCase(n.path.substring(1)))) {
            // Add common prefix to result
            String oldPath = path;
            path = path.substring(npLen);
            ciPath = ciPath + n.path;

            if (path.length() == 0) {
                // We should have reached the node containing the handle.
                // Check if this node has a handle registered.
                if (n.handlers != null) {
                    return ciPath;
                }

                // No handle found.
                // Try to fix the path by adding a trailing slash
                if (fixTrailingSlash) {
                    for (int i = 0, len = n.indices.length(); i < len; i++) {
                        if (n.indices.charAt(i) == '/') {
                            n = n.children.get(i);
                            if ((n.path.length() == 1 && n.handlers != null) ||
                                (n.nType == NodeType.CATCH_ALL && n.children.get(0).handlers != null)) {
                                return ciPath + '/';
                            }
                            return null;
                        }
                    }
                }
                return null;
            }

            // If this node does not have a wildcard (param or catchAll) child,
            // we can just look up the next child node and continue to walk down
            // the tree
            if (!n.wildChild) {
                rb = shiftNRuneBytes(rb, npLen);

                if (rb[0] != 0) {
                    // Old rune not finished
                    char idxc = (char) rb[0];
                    for (int i = 0; i < n.indices.length(); i++) {
                        if (idxc == n.indices.charAt(i)) {
                            // continue with child node
                            n = n.children.get(i);
                            npLen = n.path.length();
                            continue walk;
                        }
                    }
                } else {
                    // Process a new rune
                    char rv = '\u0000';

                    // Find rune start.
                    // Runes are up to 4 byte long,
                    // -4 would definitely be another rune.
                    int off = 0;
                    for (int max = Math.min(npLen, 3); off < max; off++) {
                        int i = npLen - off;
                        if (isRuneStart(firstByteValue(oldPath.charAt(i)))) {
                            // read rune from cached path
                            rv = oldPath.charAt(i);
                            break;
                        }
                    }

                    // Calculate lowercase bytes of current rune
                    char lo = Character.toLowerCase(rv);
                    Utils.encodeRune(rb, lo);

                    rb = shiftNRuneBytes(rb, off);

                    char idxc = (char) rb[0];
                    for (int i = 0; i < n.indices.length(); i++) {
                        // Lowercase matches
                        if (n.indices.charAt(i) == idxc) {
                            // must use a recursive approach since both the
                            // uppercase byte and the lowercase byte might exist
                            // as an index
                            String out = n.children.get(i).findCaseInsensitivePathRec(path, ciPath, rb, fixTrailingSlash);
                            if (out != null) {
                                return out;
                            }
                            break;
                        }
                    }

                    // If we found no match, the same for the uppercase rune,
                    // if it differs
                    char up = Character.toUpperCase(rv);
                    if (up != lo) {
                        Utils.encodeRune(rb, up);
                        rb = shiftNRuneBytes(rb, off);

                        idxc = (char) rb[0];
                        for (int i = 0; i < n.indices.length(); i++) {
                            // Uppercase matches
                            if (n.indices.charAt(i) == idxc) {
                                // Continue with child node
                                n = n.children.get(i);
                                npLen = n.path.length();
                                continue walk;
                            }
                        }
                    }
                }

                // Nothing found. We can recommend to redirect to the same URL
                // without a trailing slash if a leaf exists for that path
                if (fixTrailingSlash && path.equals("/") && n.handlers != null) {
                    return ciPath;
                }
                return null;
            }

            n = n.children.get(0);
            switch (n.nType) {
                case PARAM:
                    // Find param end (either '/' or path end)
                    int end = 0;
                    while (end < path.length() && path.charAt(end) != '/') {
                        end++;
                    }

                    // Add param value to case insensitive path
                    ciPath = ciPath.concat(path.substring(0, end));

                    // We need to go deeper!
                    if (end < path.length()) {
                        if (n.children.size() > 0) {
                            // Continue with child node
                            n = n.children.get(0);
                            npLen = n.path.length();
                            path = path.substring(end);
                            continue;
                        }

                        // ... but we can't
                        if (fixTrailingSlash && path.length() == end + 1) {
                            return ciPath;
                        }
                        return null;
                    }

                    if (n.handlers != null) {
                        return ciPath;
                    }

                    if (fixTrailingSlash && n.children.size() == 1) {
                        // No handle found. Check if a handle for this path + a
                        // trailing slash exists
                        n = n.children.get(0);
                        if (n.path.equals("/") && n.handlers != null) {
                            return ciPath + '/';
                        }
                    }
                    return null;
                case CATCH_ALL:
                    return ciPath.concat(path);
                default:
                    throw new RouteSyntaxException("Invalid node type");
            }
        }

        // Nothing found.
        // Try to fix the path by adding / removing a trailing slash
        if (fixTrailingSlash) {
            if ("/".equals(path)) {
                return ciPath;
            }

            int pLen = path.length();
            if (pLen + 1 == npLen && n.path.charAt(pLen) == '/' &&
                path.substring(1).equalsIgnoreCase(n.path.substring(1, pLen)) && n.handlers != null) {
                return ciPath.concat(path);
            }
        }
        return null;
    }

    private static byte[] shiftNRuneBytes(byte[] rb, int n) {
        byte[] result = new byte[4];
        switch (n) {
            case 0:
                result = rb;
                break;
            case 1:
                result[0] = rb[1];
                result[1] = rb[2];
                result[2] = rb[3];
                result[3] = 0;
                break;
            case 2:
                result[0] = rb[2];
                result[1] = rb[3];
                result[2] = 0;
                result[3] = 0;
                break;
            case 3:
                result[0] = rb[3];
                result[1] = 0;
                result[2] = 0;
                result[3] = 0;
                break;
            default:
                break;
        }
        return result;
    }

    private static boolean isRuneStart(int b) {
        // 判断字节的最高位是否为0，如果为0，则为ASCII字符，也是UTF-8编码的Unicode字符的起始字节
        return (b & 0x80) == 0x00 || (b & 0xC0) == 0xC0;
    }

    private static int firstByteValue(char c) {
        byte[] utf8Bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
        // 取出第一个字节的值
        return utf8Bytes[0] & 0xFF;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getIndices() {
        return indices;
    }

    public void setIndices(String indices) {
        this.indices = indices;
    }

    public boolean isWildChild() {
        return wildChild;
    }

    public void setWildChild(boolean wildChild) {
        this.wildChild = wildChild;
    }

    public NodeType getnType() {
        return nType;
    }

    public void setnType(NodeType nType) {
        this.nType = nType;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public List<RouteNode> getChildren() {
        return children;
    }

    public void setChildren(List<RouteNode> children) {
        this.children = children;
    }

    public HandlersChain getHandlers() {
        return handlers;
    }

    public void setHandlers(HandlersChain handlers) {
        this.handlers = handlers;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    static class FoundWildcard {
        private String wildcard;
        private int index = -1;
        private boolean valid;

        FoundWildcard(String wildcard, int index, boolean valid) {
            this.wildcard = wildcard;
            this.index = index;
            this.valid = valid;
        }
    }

}
