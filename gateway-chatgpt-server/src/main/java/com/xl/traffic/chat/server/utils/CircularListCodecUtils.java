package com.xl.traffic.chat.server.utils;

import com.xl.traffic.chat.server.cache.ConcurrentCircularList;
import lombok.experimental.UtilityClass;

import java.io.*;


/**
 * CircularListCodecUtils
 *
 * @author chenx
 */
@UtilityClass
public class CircularListCodecUtils {
    public static final int SERVICE_MAX_CONTEXT_CACHE_RING_SIZE = 255;

    /**
     * encodeStringList
     *
     * @param circularList
     * @param circularListCapacity
     * @return
     * @throws IOException
     */
    public static byte[] encodeStringList(ConcurrentCircularList<String> circularList, int circularListCapacity) throws IOException {
        if (circularList == null || circularList.isEmpty()) {
            throw new RuntimeException("The input chatContextList is null or empty!");
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream dos = new DataOutputStream(out)) {
            int circularListSize = circularList.size();
            if (circularListCapacity < 0 || circularListCapacity > SERVICE_MAX_CONTEXT_CACHE_RING_SIZE) {
                throw new IllegalArgumentException("The input chatContextList.size() must be between 0 and 256!");
            }

            dos.writeByte((byte) circularListSize);
            dos.writeByte((byte) circularListCapacity);
            for (String item : circularList) {
                dos.writeUTF(item);
            }

            return out.toByteArray();
        }
    }

    /**
     * decodeStringList
     *
     * @param bytes
     * @return
     * @throws IOException
     */
    public static ConcurrentCircularList<String> decodeStringList(byte[] bytes) throws IOException {
        try (ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             DataInputStream dis = new DataInputStream(in)) {

            int circularListSize = Byte.toUnsignedInt(dis.readByte());
            int circularListCapacity = Byte.toUnsignedInt(dis.readByte());
            if (circularListSize > circularListCapacity) {
                throw new IllegalArgumentException("circularListSize must <= circularListCapacity!");
            }

            ConcurrentCircularList<String> circularList = new ConcurrentCircularList<>(circularListCapacity);
            for (int i = 0; i < circularListSize; i++) {
                circularList.add(dis.readUTF());
            }

            return circularList;
        }
    }
}
