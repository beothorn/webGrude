package webGrude.http;

import java.util.Map;

public interface BrowserClient {
    String get(final String get);
    String post(final String post, Map<String, String>... params);
}