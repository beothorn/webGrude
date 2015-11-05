package webGrude.http;

import org.apache.http.message.BasicNameValuePair;

public interface BrowserClient {

    String get(final String get);

    String post(final String post, BasicNameValuePair... params);

}