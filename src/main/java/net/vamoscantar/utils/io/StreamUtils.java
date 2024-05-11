package net.vamoscantar.utils.io;

import io.quarkus.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamUtils {

    private StreamUtils() {

    }

    public static void copyRange(OutputStream outputStream, InputStream inputStream, long skip, long end) throws IOException {
        long readBytes = 0;

        byte[] buffer = new byte[65536];
        long skipped = inputStream.skip(skip);

        if (skipped != skip) {
            Log.warn("skipping " + skip + " bytes in input stream failed, resulting stream might broken");
        }

        int bytesInBuffer = inputStream.read(buffer);
        while (bytesInBuffer >= 0 && readBytes + skip <= end) {
            readBytes += bytesInBuffer;

            int bytesToWrite = readBytes <= end ? bytesInBuffer : (int) ((end % 65536) + 1);
            outputStream.write(buffer, 0, bytesToWrite); // time blocking

            bytesInBuffer = inputStream.read(buffer);
        }

        outputStream.close();
        inputStream.close();
    }

}
