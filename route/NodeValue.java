package route;

import java.util.ArrayList;
import java.util.List;

public class NodeValue {
    String fullPath = "";
    List<Param> params = new ArrayList<>();
    HandlersChain handlers;
    boolean tsr;

    NodeValue() {
    }

    // Getters and setters
    public HandlersChain getHandlers() {
        return handlers;
    }

    public List<Param> getParams() {
        return params;
    }

    public boolean isTsr() {
        return tsr;
    }

    public String getFullPath() {
        return fullPath;
    }

    @Override
    public String toString() {
        return "NodeValue{" +
            "fullPath='" + fullPath + '\'' +
            ", params=" + params +
            ", tsr=" + tsr +
            ", handlers=" + handlers +
            '}';
    }

}
