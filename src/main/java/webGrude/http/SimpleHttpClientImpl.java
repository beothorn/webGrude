package webGrude.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DecompressingHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

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

	private void setFirefoxAsAgent(final HttpRequestBase httpGet) {
		httpGet.setHeader("User-Agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.2.13) Gecko/20101206 Ubuntu/10.10 (maverick) Firefox/3.6.13");
	}

	private HttpResponse execute(final HttpUriRequest httpost) throws ClientProtocolException, IOException {
		return httpclient.execute(httpost);
	}

	private String executeAndGetResponseContents(final HttpUriRequest httpost)throws IOException, ClientProtocolException {
		final HttpResponse response = execute(httpost);
	    final HttpEntity entity = response.getEntity();
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
