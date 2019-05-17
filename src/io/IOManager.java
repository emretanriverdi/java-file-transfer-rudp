package io;

public class IOManager {
    public static byte[] getBytes(final int number) {
        return new byte[]{(byte) (number >>> 24 & 0xFF), (byte) (number >>> 16 & 0xFF), (byte) (number >>> 8 & 0xFF), (byte) (number & 0xFF)};
    }

    public static int getBytes(final byte[] bytes) {
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }
}
