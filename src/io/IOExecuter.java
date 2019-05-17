package io;

public class IOExecuter {
    public static byte[] execute(final byte[]... bytes) {
        int i = 0;
        int length = 0;
        for (byte[] aByte : bytes)
            length += aByte.length;
        final byte[] packet = new byte[length];
        int j = 0;
        while (j < bytes.length) {
            if (bytes[j].length > 0) {
                System.arraycopy(bytes[j], 0, packet, i, bytes[j].length);
                i += bytes[j].length;
            }
            ++j;
        }
        return packet;
    }
}
