package net.vamoscantar.services;

import io.quarkus.logging.Log;
import io.quarkus.scheduler.Scheduled;
import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.SneakyThrows;
import net.vamoscantar.utils.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.function.Predicate;

import static java.time.ZonedDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static net.vamoscantar.utils.TimeUtils.toEpoch;

@ApplicationScoped
public class FileIndex {

    @ConfigProperty(name = "ym-file-browser.base")
    String base;

    @ConfigProperty(name = "ym-file-browser.search-cache.dir")
    String cacheDirectory;

    @ConfigProperty(name = "ym-file-browser.search-cache.enabled")
    boolean cacheEnabled;

    @SneakyThrows
    public List<String> search(File root, Predicate<String> nameFilter) {
        String relativePath = relativePath(Path.of(base), root.toPath());

        try (var lines = Files.lines(determineIndexFile().toPath()).parallel()) {
            return lines
                    .filter(l -> l.startsWith(relativePath))
                    .filter(nameFilter)
                    .map(l -> base + "/" + l).toList();
        }
    }

    @Blocking
    @Scheduled(every = "3h")
    public void makeSureIndexFileIsUpToDate() throws IOException {
        if (!cacheEnabled) return;

        Log.info("Started Index update");

        File indexFile = determineIndexFile();
        if (indexFile.lastModified() >= toEpoch(now().minusHours(3))) {
            Log.info("Skipping index file rebuild since last index build was less then 6 hours ago");
            return;
        }

        ZonedDateTime indexTime = now();
        Log.info("Started updating file index at " + indexTime);

        File newIndexFile = determineIndexFile(indexTime);
        writeFileTree(Path.of(base), newIndexFile);

        FileUtils.move(newIndexFile, indexFile);
        Log.info("Finished index file update, took " + MILLIS.between(indexTime, now()) + "ms");
    }

    private File determineIndexFile() {
        return new File(cacheDirectory, "index.txt");
    }

    private File determineIndexFile(ZonedDateTime indexingTime) {
        return new File(cacheDirectory, "index" + toEpoch(indexingTime) + ".txt");
    }

    private static void writeFileTree(Path root, File file) throws IOException {
        try (var files = Files.walk(root); var fileWriter = new BufferedWriter(new FileWriter(file))) {
            files.forEach(f -> writeLine(fileWriter, relativePath(root, f)));
        }
    }

    private static String relativePath(Path root, Path f) {
        return f.toString().replaceFirst(root.toString(), "");
    }

    @SneakyThrows
    private static void writeLine(BufferedWriter fileWriter, String line) {
        fileWriter.write(line + "\n");
    }

}
