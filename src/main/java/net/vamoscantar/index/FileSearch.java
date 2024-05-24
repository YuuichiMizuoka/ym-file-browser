package net.vamoscantar.index;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import net.vamoscantar.utils.io.FileUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

import static net.vamoscantar.utils.ConcurrentUtils.race;
import static net.vamoscantar.utils.io.FileUtils.SORT_BY_TYPE_AND_NAME;

@ApplicationScoped
@RequiredArgsConstructor
public class FileSearch {

    @ConfigProperty(name = "ym-file-browser.base")
    String base;

    @ConfigProperty(name = "ym-file-browser.search-cache.enabled")
    boolean cacheEnabled;

    private final FileIndex fileIndex;

    public List<File> search(File root, String searchQuery) throws ExecutionException, InterruptedException {
        if (cacheEnabled && pathCanBeRaced(root)) {
            return race(() -> findRecursivelyOnFileSystem(root, searchQuery), () -> findRecursivelyCache(root, searchQuery));
        }
        if (cacheEnabled) {
            return findRecursivelyCache(root, searchQuery);
        }
        return findRecursivelyOnFileSystem(root, searchQuery);
    }

    private boolean pathCanBeRaced(File root) {
        return !root.getAbsolutePath().equals(base);
    }

    private List<File> findRecursivelyOnFileSystem(File file, String searchQuery) {
        Predicate<Path> fileNameFilter = f -> lineMatchesQueries(f.toString(), searchQuery.toLowerCase().split(" "));
        return FileUtils.findRecursively(file, fileNameFilter).stream().sorted(SORT_BY_TYPE_AND_NAME).toList();
    }

    private List<File> findRecursivelyCache(File file, String searchQuery) {
        Predicate<String> fileNameFilter = l -> lineMatchesQueries(l, searchQuery.toLowerCase().split(" "));
        return fileIndex.search(file, fileNameFilter).stream().map(File::new).sorted(SORT_BY_TYPE_AND_NAME).toList();
    }

    private boolean lineMatchesQueries(String line, String[] queries) {
        for (var s : queries) {
            if ((line.toLowerCase().lastIndexOf(s) <= line.lastIndexOf("/"))) {
                return false;
            }
        }
        return true;
    }


}
