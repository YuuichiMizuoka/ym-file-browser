package net.vamoscantar.utils.http;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlUtils {

    private UrlUtils() {

    }

    public static String encodePath(String path) {
        try {
            return new URI(null, null, path, null).toASCIIString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("not a legal path", e);
        }
    }

    public static String sanitizePath(String path) {
        try {
            return stripLeadingPathTraversal(normalizePath(path));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("not a legal path", e);
        }
    }

    private static String normalizePath(String path) throws URISyntaxException {
        return new URI(path).normalize().getPath();
    }

    private static String stripLeadingPathTraversal(String path) {
        while (path.startsWith("/..")) {
            path = path.replaceFirst("/..", "");
        }
        return path;
    }

}
