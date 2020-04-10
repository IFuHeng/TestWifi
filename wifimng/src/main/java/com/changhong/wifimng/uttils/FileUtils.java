package com.changhong.wifimng.uttils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileUtils {
    public static String readFile(InputStream is) throws IOException {
        if (is == null)
            return null;

        return new String(readFromIputStream(is));
    }

    public static byte[] readFromIputStream(InputStream is) throws IOException {
        if (is == null)
            return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[128];
        do {
            int length = is.read(buf);
            if (length == -1)
                break;
            baos.write(buf, 0, length);
        } while (true);
        return baos.toByteArray();
    }
}
