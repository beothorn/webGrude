package webGrude.http;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

public interface BrowserClient {
	public String get(final String get) throws ClientProtocolException,IOException;
}