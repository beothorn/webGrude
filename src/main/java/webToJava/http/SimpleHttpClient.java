package webToJava.http;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

public interface SimpleHttpClient {
 
	public String getToFile(final String link, final File destFile)throws ClientProtocolException, IOException;
	public String getOrNull(final String url);
	public String post(final String postUrl, final Map<String, String> params)throws ClientProtocolException, IOException;
	public String post(final String postUrl, final String params)throws ClientProtocolException, IOException;
	public String get(final String get) throws ClientProtocolException,IOException;
	public void close();
}