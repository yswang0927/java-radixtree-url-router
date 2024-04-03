package route;

import java.util.ArrayList;
import java.util.List;

public class MethodTrees {
    private List<MethodTree> trees;

    public MethodTrees() {
        trees = new ArrayList<>();
    }

    public void add(MethodTree tree) {
        if (tree != null) {
            trees.add(tree);
        }
    }

    public RouteNode get(String method) {
        for (MethodTree tree : trees) {
            if (tree.getMethod().equals(method)) {
                return tree.getRoot();
            }
        }
        return null;
    }
}
