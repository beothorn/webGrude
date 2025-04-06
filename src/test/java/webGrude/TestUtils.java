package webGrude;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtils {
    public static String readTestResource(String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource(resourceName);
        if (resource == null) {
            throw new IllegalArgumentException("Resource not found: " + resourceName);
        }
        try {
            return Files.readString(Paths.get(resource.toURI()), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
