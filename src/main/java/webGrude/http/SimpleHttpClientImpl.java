package webGrude.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class SimpleHttpClientImpl implements BrowserClient {

	private final CloseableHttpClient httpclient;
	private String userAgent= "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13";

	public SimpleHttpClientImpl() {
		httpclient = HttpClients.createDefault();
	}
	
	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String get(final String get){
		
		URL url;
		try {
			url = new URL(get);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		
		if (url.getProtocol().equalsIgnoreCase("file")) {
			try {
				final FileInputStream fileInputStream = new FileInputStream(new File(url.getPath()));
				return IOUtils.toString(fileInputStream, "UTF-8");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		try {
			return internalGet(url);
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	private String internalGet(final URL get) throws IOException, ClientProtocolException, URISyntaxException {
		HttpUriRequest request = RequestBuilder.get()
                .setUri(get.toURI())
                .setHeader("User-Agent", userAgent)
                .build();
        CloseableHttpResponse execute = httpclient.execute(request);
        final HttpEntity entity =  execute.getEntity();
        final InputStream contentIS = entity.getContent();
        final Header contentType = entity.getContentType();
        final HeaderElement[] elements = contentType.getElements();
        final HeaderElement headerElement = elements[0];
        final NameValuePair parameterByName = headerElement.getParameterByName("charset");
        String encoding = "UTF-8";
        if(parameterByName != null)
            encoding = parameterByName.getValue();
        if(encoding != null  && encoding.equals("ISO-8859-1")){
            encoding = "CP1252";
        }
        final String content = IOUtils.toString(contentIS,encoding);
        contentIS.close();
        return content;
	}

}
