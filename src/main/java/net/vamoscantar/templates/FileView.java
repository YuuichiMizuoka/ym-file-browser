package net.vamoscantar.templates;

import io.quarkus.qute.Qute;
import net.vamoscantar.utils.http.UrlUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.probeContentType;
import static net.vamoscantar.utils.ResourceUtils.readResource;
import static net.vamoscantar.utils.lang.CollectionUtils.lastElement;
import static net.vamoscantar.utils.lang.StringUtils.buildTree;

public class FileView {

    private FileView() {

    }

    private static final String FILE_VIEW_TEMPLATE = "templates/fileView.html";

    public static String render(String fileRoot, String requestPath, File file, File leftNeighbour, File rightNeighbour) throws IOException {
        var parentPathTree = removeLast(buildPathTree(requestPath));

        return Qute.fmt(readResource(FILE_VIEW_TEMPLATE))
                .contentType("text/html")
                .data("requestPath", requestPath)
                .data("requestPathParts", parentPathTree)
                .data("filePath", UrlUtils.encodePath(file.getAbsolutePath().replace(fileRoot, "")))
                .data("fileType", probeContentType(file.toPath()))
                .data("fileName", file.getName())
                .data("previousPath", toPath(fileRoot, leftNeighbour))
                .data("nextPath", toPath(fileRoot, rightNeighbour))
                .render();
    }

    private static String toPath(String fileRoot, File file) {
        if (file == null) {
            return null;
        }
        return UrlUtils.encodePath(file.getAbsolutePath().replace(fileRoot, ""));
    }

    private static List<DirectoryList.PathPart> buildPathTree(String requestPath) {
        return buildTree(requestPath, "/").stream()
                .map(t -> new DirectoryList.PathPart(UrlUtils.encodePath(t), lastElement(t.split("/"))))
                .toList();
    }

    private static List<DirectoryList.PathPart> removeLast(List<DirectoryList.PathPart> pathParts) {
        var copy = new ArrayList<>(pathParts);
        copy.remove(copy.size() - 1);
        return copy;
    }

}
