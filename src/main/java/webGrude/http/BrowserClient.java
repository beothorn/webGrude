package webGrude.http;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;

public interface BrowserClient {
	public String get(final String get) throws ClientProtocolException,IOException;
}