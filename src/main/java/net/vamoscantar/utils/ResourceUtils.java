package net.vamoscantar.utils;

import java.io.IOException;

import static java.lang.Thread.currentThread;

public class ResourceUtils {

    private ResourceUtils() {

    }

    public static String readResource(String resourceName) throws IOException {
        return new String(readResourceBytes(resourceName));
    }

    public static byte[] readResourceBytes(String resourceName) throws IOException {
        try (var is = currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            return is.readAllBytes();
        }
    }

}
