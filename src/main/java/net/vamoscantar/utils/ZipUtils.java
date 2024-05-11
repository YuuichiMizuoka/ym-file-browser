package net.vamoscantar.utils;

import net.vamoscantar.utils.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ZipUtils {

    private ZipUtils() {

    }

    public static void streamAsZip(OutputStream output, List<File> fileList, File fileRoot) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(output)) {
            writeToZip(zos, fileList, fileRoot);
        }
    }

    private static void writeToZip(ZipOutputStream zos, List<File> fileList, File fileRoot) throws IOException {
        for (var file : fileList) {
            if (file.isDirectory()) {
                writeToZip(zos, FileUtils.list(file), fileRoot);
            }
            if (file.isFile()) {
                writeFileToZip(zos, file, file.getAbsolutePath().replace(fileRoot.getAbsolutePath(), ""));
            }
        }
    }

    private static void writeFileToZip(ZipOutputStream zos, File file, String name) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        Files.copy(file.toPath(), zos);
        zos.closeEntry();
    }

}
