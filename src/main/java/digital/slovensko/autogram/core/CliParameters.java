package digital.slovensko.autogram.core;

import java.util.HashMap;
import java.util.Map;

public class CliParameters {
    private Map<String, String> namedParams;

    public CliParameters() {
        this.namedParams = new HashMap<String, String>();
    }

    public void put(String key, String value) {
        this.namedParams.put(key, value);
    }

    public String get(String key) {
        return this.namedParams.get(key);
    }
}
