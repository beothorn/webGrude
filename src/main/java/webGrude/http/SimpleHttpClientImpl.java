package webGrude.http;

import okhttp3.*;

import java.io.IOException;
import java.util.Map;

public class SimpleHttpClientImpl implements BrowserClient {

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public String get(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        } catch (IOException e) {
            throw new RuntimeException("GET request failed for URL: " + url, e);
        }
    }

    @Override
    public String post(String url, Map<String, String>... params) {
        FormBody.Builder formBuilder = new FormBody.Builder();

        if (params != null && params.length > 0) {
            for (Map.Entry<String, String> entry : params[0].entrySet()) {
                formBuilder.add(entry.getKey(), entry.getValue());
            }
        }

        Request request = new Request.Builder()
                .url(url)
                .post(formBuilder.build())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            ResponseBody body = response.body();
            return body != null ? body.string() : "";
        } catch (IOException e) {
            throw new RuntimeException("POST request failed for URL: " + url, e);
        }
    }
}