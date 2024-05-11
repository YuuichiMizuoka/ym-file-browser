package net.vamoscantar;


import io.vertx.core.http.HttpServerRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import net.vamoscantar.templates.DirectoryList;
import net.vamoscantar.templates.FileView;
import net.vamoscantar.utils.ZipUtils;
import net.vamoscantar.utils.http.HeaderUtils;
import net.vamoscantar.utils.http.UrlUtils;
import net.vamoscantar.utils.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static jakarta.ws.rs.core.MediaType.TEXT_HTML;
import static jakarta.ws.rs.core.Response.Status.OK;
import static jakarta.ws.rs.core.Response.Status.PARTIAL_CONTENT;
import static net.vamoscantar.utils.ResourceUtils.readResourceBytes;
import static net.vamoscantar.utils.io.FileUtils.SORT_BY_TYPE_AND_NAME;
import static net.vamoscantar.utils.io.StreamUtils.copyRange;
import static net.vamoscantar.utils.lang.StringUtils.containsIgnoringCase;
import static net.vamoscantar.utils.lang.StringUtils.hasText;

@Path("/")
public class AppEndpoint {

    @ConfigProperty(name = "ym-file-browser.base")
    String base;

    @GET
    @Path("/{paths: .*}")
    public Response serveRequest(@Context HttpServerRequest request) throws IOException {
        String requestPath = UrlUtils.sanitizePath(request.path());
        File requestFile = FileUtils.resolveSecurely(base, requestPath);

        if (request.getParam("asset") != null) {
            return buildAssetResponse(requestPath);
        }

        if (requestFile.isFile()) {
            boolean showAsView = request.getParam("view") != null;

            if (showAsView) {
                return buildFileViewResponse(requestPath, requestFile, determineFileNeighbours(requestFile));
            } else {
                return buildBinaryResponse(requestFile, request.getHeader("Range"), request.getHeader("If-None-Match"));
            }
        }

        if (requestFile.isDirectory()) {
            List<File> fileList = determineFileList(requestFile, request.getParam("q"));

            boolean showAsZip = request.getParam("zip") != null;
            if (showAsZip) {
                return buildZipResponse(requestFile, fileList);
            } else {
                return buildDirectoyListResponse(requestPath, fileList, request.getParam("q"));
            }
        }

        throw new NotFoundException("Resource not Found");
    }

    private List<File> determineFileNeighbours(File requestedFile) {
        var neighbourFiles = FileUtils.list(requestedFile.getParentFile(), File::isFile, SORT_BY_TYPE_AND_NAME);
        var requestedFileIndex = neighbourFiles.indexOf(requestedFile);

        var leftNeighbour = requestedFileIndex <= 0 ? null : neighbourFiles.get(requestedFileIndex - 1);
        var rightNeighbour = requestedFileIndex >= neighbourFiles.size() - 1 ? null : neighbourFiles.get(requestedFileIndex + 1);

        return Arrays.asList(leftNeighbour, rightNeighbour);
    }

    private List<File> determineFileList(File file, String searchQuery) throws IOException {
        if (searchQuery != null && !searchQuery.isBlank()) {
            return FileUtils.findRecursively(file, path -> containsIgnoringCase(path.getFileName().toString(), searchQuery), SORT_BY_TYPE_AND_NAME);
        }
        return FileUtils.list(file, SORT_BY_TYPE_AND_NAME);
    }

    private Response buildAssetResponse(String requestPath) throws IOException {
        var assetResponse = switch (requestPath) {
            case "/index.css" -> Response.ok(readResourceBytes("assets" + requestPath), "text/css");
            case "/favicon.ico" -> Response.ok(readResourceBytes("assets" + requestPath), "image/x-icon");
            default -> throw new NotFoundException("asset not found or allowlisted");
        };
        return assetResponse.header("Cache-Control", "no-transform, max-age=604800").build();
    }

    private Response buildFileViewResponse(String requestPath, File file, List<File> neighbours) throws IOException {
        return Response.ok(FileView.render(base, requestPath, file, neighbours.get(0), neighbours.get(1)), TEXT_HTML).build();
    }

    private Response buildBinaryResponse(File file, String rangeHeader, String ifNoneMatchHeader) throws IOException {
        long[] range = HeaderUtils.parseRangeHeader(rangeHeader, file.length());

        String fileType = Files.probeContentType(file.toPath());
        String etag = "\"" + Files.getLastModifiedTime(file.toPath()).toMillis() + "-" + file.length() + "\"";

        if (etag.equals(ifNoneMatchHeader)) {
            return Response.notModified().header("ETag", etag).build();
        }

        return Response.status(hasText(rangeHeader) ? PARTIAL_CONTENT : OK)
                .entity((StreamingOutput) output -> {
                    try (InputStream fileStream = FileUtils.newInputStream(file)) {
                        copyRange(output, fileStream, range[0], range[1]);
                    }
                })
                .type(fileType)
                .header("Content-Disposition", (fileType == null ? "attachment; " : "inline; ") + file.getName())
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", range[1] + 1 - range[0])
                .header("Content-Range", String.format("%s %d-%d/%d", "bytes", range[0], range[1], file.length()))
                .header("ETag", etag)
                .build();
    }

    private Response buildZipResponse(File fileRoot, List<File> fileList) {
        return Response.ok((StreamingOutput) output -> ZipUtils.streamAsZip(output, fileList, fileRoot))
                .header("Content-Disposition", "attachment;filename=" + fileRoot.getName() + ".zip")
                .build();
    }

    private Response buildDirectoyListResponse(String requestPath, List<File> fileList, String query) throws IOException {
        return Response.ok(DirectoryList.render(base, requestPath, fileList, query), TEXT_HTML).build();
    }

}
