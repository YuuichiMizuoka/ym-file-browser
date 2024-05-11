package net.vamoscantar.templates;

import io.quarkus.qute.Qute;
import net.vamoscantar.utils.http.UrlUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static net.vamoscantar.utils.ResourceUtils.readResource;
import static net.vamoscantar.utils.lang.CollectionUtils.lastElement;
import static net.vamoscantar.utils.lang.StringUtils.buildTree;

public class DirectoryList {

    private static final String DIRECTORY_LIST_TEMPLATE = "templates/directoryList.html";

    public record FileListEntry(File file, String name, String path, String readableSize) {
    }

    public record PathPart(String pathLink, String pathName) {

    }

    public static String render(String fileRoot, String requestPath, List<File> fileList, String query) throws IOException {
        var requestPathParts = buildPathTree(requestPath);
        var entries = fileList.stream().map(f -> toEntry(fileRoot, f)).toList();

        return Qute.fmt(readResource(DIRECTORY_LIST_TEMPLATE))
                .contentType("text/html")
                .data("requestPath", requestPath)
                .data("requestPathParts", requestPathParts)
                .data("entries", entries)
                .data("query", query)
                .render();
    }

    private static List<PathPart> buildPathTree(String requestPath) {
        return buildTree(requestPath, "/").stream()
                .map(t -> new PathPart(UrlUtils.encodePath(t), lastElement(t.split("/"))))
                .toList();
    }

    private static FileListEntry toEntry(String fileRoot, File f) {
        return new FileListEntry(
                f,
                f.getName(),
                UrlUtils.encodePath(f.getAbsolutePath().replace(fileRoot, "")),
                f.isDirectory() ? "-" : getReadableSize(f.length())
        );
    }

    private static String getReadableSize(long length) {
        if (length < 1024) {
            return String.valueOf(length);
        }

        if (length < 1024 * 1024) {
            return length / 1024 + " KB";
        }

        if (length < 1024 * 1024 * 1024) {
            return length / 1024 / 1024 + " MB";
        }

        if (length < 1024L * 1024L * 1024L * 1024L) {
            return length / 1024 / 1024 / 1024 + " GB";
        }

        return length / 1024 / 1024 / 1024 / 1024 + " TB";
    }

}
