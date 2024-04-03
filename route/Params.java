package route;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Params {
    List<Param> params = new ArrayList<>();

    public void addParam(String key, String value) {
        params.add(new Param(key, value));
    }

    public Optional<String> get(String name) {
        for (Param param : params) {
            if (param.getKey().equals(name)) {
                return Optional.of(param.getValue());
            }
        }
        return Optional.empty();
    }

    public String byName(String name) {
        return this.get(name).orElse("");
    }

}
