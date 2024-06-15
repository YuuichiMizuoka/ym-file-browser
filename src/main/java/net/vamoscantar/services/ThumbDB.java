package net.vamoscantar.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;

import static io.quarkus.runtime.util.HashUtil.sha256;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.nio.file.Files.*;
import static net.vamoscantar.utils.ResourceUtils.readResourceStream;
import static net.vamoscantar.utils.http.HeaderUtils.buildFileEtag;

@ApplicationScoped
public class ThumbDB {

    private static final int THUMB_SIZE = 256;

    @ConfigProperty(name = "ym-file-browser.search-cache.dir")
    String thumbCacheDir;

    public record Thumb(InputStream is, String etag) {

    }

    public Thumb determineThumb(Path path) throws IOException {
        Path thumbPath = determineThumbPath(path);

        if (exists(thumbPath) && withinOneSecond(getLastModifiedTime(thumbPath), getLastModifiedTime(path))) {
            return new Thumb(newInputStream(thumbPath), buildFileEtag(thumbPath));
        }

        if (isDirectory(path)) {
            return createDirectoryThumb();
        }

        try {
            return createFileThumb(path, thumbPath);
        } catch (Exception e) {
            Log.warn("Failed thumbnail creation for " + path, e);
        }

        return createDefaultFileThumb();
    }

    private Path determineThumbPath(Path path) {
        String hash = sha256(path.toString()) + ".png";
        return Path.of(thumbCacheDir, hash.substring(0, 2), hash).toAbsolutePath();
    }

    private static Thumb createDirectoryThumb() {
        return new Thumb(readResourceStream("/assets/icons/directory.png"), "\"dir-1\"");
    }

    private Thumb createFileThumb(Path path, Path thumbPath) throws IOException {
        String contentType = probeContentType(path);
        if (contentType != null && (contentType.equals("image/png") || contentType.equals("image/jpeg") || contentType.equals("image/gif"))) {
            return new Thumb(newInputStream(createNewImageThumb(path, thumbPath)), buildFileEtag(thumbPath));
        }
        return createDefaultFileThumb();
    }

    private static Thumb createDefaultFileThumb() {
        return new Thumb(readResourceStream("/assets/icons/file.png"), "\"file-1\"");
    }

    private static boolean withinOneSecond(FileTime left, FileTime right) {
        return Math.abs(left.toMillis() - right.toMillis()) < 1000;
    }

    private Path createNewImageThumb(Path image, Path thumbPath) throws IOException {
        deleteIfExists(thumbPath);
        makeSureFoldersExist(thumbPath.getParent());

        thumbPath.toFile().createNewFile();

        BufferedImage fileImage = ImageIO.read(image.toFile());
        if (fileImage == null) throw new IllegalArgumentException(image + " cant be read by any java image reader");

        var width = fileImage.getHeight() <= fileImage.getWidth() ? THUMB_SIZE : THUMB_SIZE / ((double) fileImage.getHeight() / fileImage.getWidth());
        var height = fileImage.getHeight() >= fileImage.getWidth() ? THUMB_SIZE : THUMB_SIZE / ((double) fileImage.getWidth() / fileImage.getHeight());

        Image scaledImage = fileImage.getScaledInstance((int) width, (int) height, Image.SCALE_SMOOTH);

        BufferedImage thumbnailBuffer = new BufferedImage(THUMB_SIZE, THUMB_SIZE, TYPE_INT_ARGB);
        thumbnailBuffer.createGraphics().drawImage(scaledImage, (int) ((THUMB_SIZE - width) / 2), (int) ((THUMB_SIZE - height) / 2), null);

        ImageIO.write(thumbnailBuffer, "png", thumbPath.toFile());
        setLastModifiedTime(thumbPath, getLastModifiedTime(image));
        return thumbPath;
    }

    private void makeSureFoldersExist(Path thumbFolderPath) throws IOException {
        if (!Files.exists(thumbFolderPath)) {
            Files.createDirectories(thumbFolderPath);
        }
    }
}
