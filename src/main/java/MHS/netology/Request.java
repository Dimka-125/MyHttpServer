package MHS.netology;

import org.apache.hc.core5.net.URLEncodedUtils;
import org.apache.hc.core5.http.NameValuePair;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class Request {
    private final String method;
    private final String path;
    private final Map<String, String> queryParams = new HashMap<>();

    public Request(String requestLine) {
        String[] parts = requestLine.split(" ");
        if (parts.length != 3) {
            this.method = null;
            this.path = null;
            return;
        }

        this.method = parts[0];

        String uri = parts[1];
        int queryStart = uri.indexOf('?');
        if (queryStart == -1) {
            this.path = uri;
        } else {
            this.path = uri.substring(0, queryStart);
            String queryString = uri.substring(queryStart + 1);
            List<NameValuePair> pairs = URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
            for (NameValuePair pair : pairs) {
                queryParams.put(pair.getName(), pair.getValue());
            }
        }
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public Optional<String> getQueryParam(String name) {
        return Optional.ofNullable(queryParams.get(name));
    }

    public Map<String, String> getQueryParams() {
        return Collections.unmodifiableMap(queryParams);
    }
}