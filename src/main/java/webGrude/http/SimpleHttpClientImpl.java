package webGrude.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class SimpleHttpClientImpl implements BrowserClient {

	private final CloseableHttpClient httpclient;

	public SimpleHttpClientImpl() {
        httpclient = HttpClients.createDefault();
	}

	public String get(final String get) throws ClientProtocolException, IOException {
        HttpUriRequest request = RequestBuilder.get()
                .setUri(get)
                .setHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13")
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
