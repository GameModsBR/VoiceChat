/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

public class OggCrc {
    private static int[] crc_lookup = new int[256];

    public static int checksum(int n, byte[] arrby, int n2, int n3) {
        int n4 = n2 + n3;
        while (n2 < n4) {
            n = n << 8 ^ crc_lookup[n >>> 24 & 255 ^ arrby[n2] & 255];
            ++n2;
        }
        return n;
    }

    static {
        for (int i = 0; i < crc_lookup.length; ++i) {
            int n = i << 24;
            for (int j = 0; j < 8; ++j) {
                if ((n & Integer.MIN_VALUE) != 0) {
                    n = n << 1 ^ 79764919;
                    continue;
                }
                n <<= 1;
            }
            OggCrc.crc_lookup[i] = n & -1;
        }
    }
}

