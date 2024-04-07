package route;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author wangyongshan
 */
public class Params {
    List<Param> paramsList = new ArrayList<>();

    void addParam(String key, String value) {
        this.paramsList.add(new Param(key, value));
    }

    public Optional<String> get(String name) {
        for (Param param : this.paramsList) {
            if (param.getKey().equals(name)) {
                return Optional.of(param.getValue());
            }
        }
        return Optional.empty();
    }

    public String byName(String name) {
        return this.get(name).orElse("");
    }

    public int paramsCount() {
        return this.paramsList.size();
    }

    Params subList(int start, int end) {
        Params params = new Params();
        List<Param> subList = new ArrayList<>();
        for (int i = start; i < end; ++i) {
            subList.add(this.paramsList.get(i));
        }
        params.paramsList = subList;
        return params;
    }

    @Override
    public String toString() {
        return this.paramsList.toString();
    }
}
