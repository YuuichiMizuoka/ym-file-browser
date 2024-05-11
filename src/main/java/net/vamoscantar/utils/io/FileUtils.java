package net.vamoscantar.utils.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNullElse;
import static net.vamoscantar.utils.lang.StringUtils.isDigits;

public class FileUtils {

    private static final Pattern NUMERIC = Pattern.compile("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

    private FileUtils() {
        
    }

    public static final Comparator<File> SORT_SEMANTICALLY = (l, r) -> {
        String[] leftSegments = NUMERIC.split(l.getName());
        String[] rightSegments = NUMERIC.split(r.getName());

        return compareSegments(leftSegments, rightSegments);
    };

    public static final Comparator<File> SORT_BY_TYPE_AND_NAME = (l, r) -> {
        if (l.isDirectory() == r.isDirectory() || l.isFile() == r.isFile()) {
            return SORT_SEMANTICALLY.compare(l, r);
        }
        return l.isDirectory() ? -1 : 1;
    };

    public static File resolveSecurely(String base, String path) {
        Path basePath = Path.of(base);
        Path resolvedPath = Path.of(base, path);

        return resolvedPath.startsWith(basePath) ? resolvedPath.toFile() : basePath.toFile();
    }

    public static List<File> list(File file) {
        return stream(requireNonNullElse(file.listFiles(), new File[0])).toList();
    }

    public static List<File> list(File file, Comparator<File> sort) {
        return stream(requireNonNullElse(file.listFiles(), new File[0])).sorted(sort).toList();
    }

    public static List<File> list(File file, Predicate<File> filter, Comparator<File> sort) {
        return stream(requireNonNullElse(file.listFiles(), new File[0])).filter(filter).sorted(sort).toList();
    }

    public static List<File> findRecursively(File file, Predicate<Path> filter, Comparator<File> sort) throws IOException {
        try (var walk = Files.walk(file.toPath())) {
            return walk.filter(filter).map(Path::toFile).sorted(sort).toList();
        }
    }

    public static InputStream newInputStream(File file) throws IOException {
        return Files.newInputStream(file.toPath());
    }

    private static Integer compareSegments(String[] leftSegments, String[] rightSegments) {
        for (int i = 0; i < Math.min(leftSegments.length, rightSegments.length); i++) {
            int semanticCompare = compareSegment(leftSegments[i], rightSegments[i]);
            if (semanticCompare != 0)
                return semanticCompare;
        }
        return leftSegments.length - rightSegments.length;
    }

    private static int compareSegment(String leftSegment, String rightSegment) {
        int cmp = 0;
        if (isDigits(leftSegment) && isDigits(rightSegment))
            cmp = new BigInteger(leftSegment).compareTo(new BigInteger(rightSegment));

        if (cmp == 0)
            cmp = leftSegment.compareTo(rightSegment);

        return cmp;
    }

}
