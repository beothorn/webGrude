package webGrude.http;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class SimpleHttpClientImpl implements BrowserClient {

    private final CloseableHttpClient httpclient;
    private String userAgent = "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13";

    public SimpleHttpClientImpl() {
        httpclient = HttpClients.createDefault();
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String get(final String get) {
        final URL url;
        try {
            url = new URL(get);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        if (url.getProtocol().equalsIgnoreCase("file")) {
            try {
                return Files.toString(new File(url.getPath()), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            return internalGet(url);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String internalGet(final URL get) throws IOException, URISyntaxException {
        final HttpUriRequest request = RequestBuilder.get()
                .setUri(get.toURI())
                .setHeader("User-Agent", userAgent)
                .build();
        return executeRequest(request);
    }

    private String executeRequest(final HttpUriRequest request) throws IOException {
        final CloseableHttpResponse response = httpclient.execute(request);
        try {
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } finally {
            Closeables.close(response, true);
        }
    }

    public String post(String post, BasicNameValuePair... params) {
        try {
            return internalPost(post, params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String internalPost(String post, BasicNameValuePair... params) throws IOException {
        final HttpUriRequest request = RequestBuilder.post()
                .addParameters(params)
                .setUri(post)
                .setHeader("User-Agent", userAgent)
                .build();
        return executeRequest(request);
    }

}
