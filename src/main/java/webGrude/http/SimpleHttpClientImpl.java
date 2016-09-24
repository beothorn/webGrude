package webGrude.http;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.common.io.Closeables;
import com.google.common.io.Files;

public class SimpleHttpClientImpl implements BrowserClient {

    private final CloseableHttpClient httpclient;
    private String userAgent = "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13";

    public SimpleHttpClientImpl() {
        this.httpclient = HttpClients.createDefault();
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public void setUserAgent(final String userAgent) {
        this.userAgent = userAgent;
    }

    @Override
    public String get(final String urlAsString) {
        final URL url;
        try {
            url = new URL(urlAsString);
            if (url.getProtocol().equalsIgnoreCase("file")) {
                final File file = new File(url.getPath());
                this.throwRuntimeIfFileIsInvalid(file);
                return Files.toString(file, StandardCharsets.UTF_8);
            }
            return this.internalGet(url);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void throwRuntimeIfFileIsInvalid(final File file) {
        if(!file.exists()) {
            throw new RuntimeException("File does not exist '"+file.getAbsolutePath()+"'");
        }
        if(!file.isDirectory()) {
            throw new RuntimeException("File can't be a direcfory '"+file.getAbsolutePath()+"'");
        }
    }

    private String internalGet(final URL get) throws IOException, URISyntaxException {
        final HttpUriRequest request = RequestBuilder.get()
                .setUri(get.toURI())
                .setHeader("User-Agent", this.userAgent)
                .build();
        return this.executeRequest(request);
    }

    private String executeRequest(final HttpUriRequest request) throws IOException {
        final CloseableHttpResponse response = this.httpclient.execute(request);
        try {
            final HttpEntity entity = response.getEntity();
            if(entity == null) {
                throw new RuntimeException("No response to request "+ request);
            }
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        } finally {
            Closeables.close(response, true);
        }
    }

    @Override
    public String post(final String post, final BasicNameValuePair... params) {
        try {
            return this.internalPost(post, params);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String internalPost(final String post, final BasicNameValuePair... params) throws IOException {
        final HttpUriRequest request = RequestBuilder.post()
                .setUri(post)
                .addParameters(params)
                .setHeader("User-Agent", this.userAgent)
                .build();
        return this.executeRequest(request);
    }

}
