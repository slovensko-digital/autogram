package com.octosign.whitelabel.cli.command;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.octosign.whitelabel.ui.Utils.isNullOrBlank;

public class QueryParams {
    private final Map<String, String> params;

    public static QueryParams parseQueryString(final String query) {
        return new QueryParams(query);
    }

    private QueryParams(final String query) {
        if (isNullOrBlank(query)) {
            params = Collections.emptyMap();
        } else {
            params = Stream.of(query.split("&"))
                           .filter(entry -> !entry.isBlank())
                           .map(entry -> entry.split("="))
                           .collect(Collectors.toMap(pair -> pair[0], pair -> (pair.length > 1) ? pair[1] : null));
        }
    }

    public String get(String key) {
        return params.get(key);
    }

    public <T> T get(String key, Class<T> klass) {
        return klass.cast(key);
    }
}
