package route;

/**
 * 路由中参数名称和参数值。
 *
 * @author wangyongshan
 */
public class Param {
    private String key;
    private String value;

    Param(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Param{" +
            "key='" + key + '\'' +
            ", value='" + value + '\'' +
            '}';
    }
}
