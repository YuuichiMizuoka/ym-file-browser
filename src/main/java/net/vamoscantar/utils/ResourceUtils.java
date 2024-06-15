package net.vamoscantar.utils;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.Thread.currentThread;

public class ResourceUtils {

    private ResourceUtils() {

    }

    public static String readResource(String resourceName) throws IOException {
        return new String(readResourceBytes(resourceName));
    }

    public static byte[] readResourceBytes(String resourceName) throws IOException {
        try (var is = readResourceStream(resourceName)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resourceName);
            }
            return is.readAllBytes();
        }
    }

    public static InputStream readResourceStream(String resourceName) {
        return currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    }

}
