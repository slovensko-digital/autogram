package com.octosign.whitelabel.cli.command;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static com.octosign.whitelabel.ui.utils.Utils.isNullOrBlank;
import static java.util.stream.Collectors.toMap;

public class QueryParams {
    private final Map<String, String> params;

    public static QueryParams parseQueryString(final String query) {
        return new QueryParams(query);
    }

    private QueryParams(final String query) {
        if (isNullOrBlank(query)) {
            params = Collections.emptyMap();
        } else {
            params = URLEncodedUtils.parse(query, StandardCharsets.UTF_8).stream()
                                    .collect(toMap(NameValuePair::getName, NameValuePair::getValue));
        }
    }

    public String get(String key) {
        return params.get(key);
    }

    public Map<String, String> asMap() {
        return Collections.unmodifiableMap(params);
    }
}
