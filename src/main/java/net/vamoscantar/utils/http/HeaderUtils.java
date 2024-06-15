package net.vamoscantar.utils.http;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static net.vamoscantar.utils.lang.StringUtils.isBlank;

public class HeaderUtils {

    private HeaderUtils() {

    }

    public static long[] parseRangeHeader(String range, long contentLength) {
        if (range == null) {
            return new long[]{0, contentLength - 1};
        }

        String[] rangeTuple = range.split("=")[1].split("-");

        long rangeStart = isBlank(rangeTuple[0]) ? 0 : Long.parseLong(rangeTuple[0]);
        long rangeEnd = rangeTuple.length == 1 || isBlank(rangeTuple[1]) ? contentLength - 1 : Long.parseLong(rangeTuple[1]);

        if (rangeIsNotSatisfiable(rangeStart, rangeEnd, contentLength)) {
            throw new IllegalArgumentException("content range not satisfiable");
        }
        return new long[]{rangeStart, rangeEnd};
    }

    private static boolean rangeIsNotSatisfiable(long rangeStart, long rangeEnd, long fileLength) {
        return (rangeStart < 0 || rangeStart >= fileLength) || (rangeEnd < 0 || rangeEnd >= fileLength) || (rangeEnd < rangeStart);
    }

    public static String buildFileEtag(Path thumbPath) throws IOException {
        return "\"" + Files.getLastModifiedTime(thumbPath).toMillis() + "-" + thumbPath.toFile().length() + "\"";
    }

}
