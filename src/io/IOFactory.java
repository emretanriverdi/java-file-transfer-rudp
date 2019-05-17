package io;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;

public class IOFactory {
    public static byte[] getDestinationSource(final File f) throws IOException {
        final InputStream inputStreamF = new FileInputStream(f);
        if (f.length() > 2147483647L)
            throw new IOException();
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream((int) f.length());
        final byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStreamF.read(buffer)) != -1)
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        inputStreamF.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static void getSourceBytes(final byte[] bytes, final File file) {
        BufferedOutputStream bufferedOutputStream = null;
        try {
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
            bufferedOutputStream.write(bytes);
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                if (bufferedOutputStream != null)
                    bufferedOutputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (bufferedOutputStream != null)
                    bufferedOutputStream.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
}
