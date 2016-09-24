package webGrude.http;

import org.apache.http.message.BasicNameValuePair;

public interface BrowserClient {
    public String get(final String get);
    public String post(final String post, BasicNameValuePair... params);
}