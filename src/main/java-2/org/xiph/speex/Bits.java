/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

public class Bits {
    public static final int DEFAULT_BUFFER_SIZE = 1024;
    private byte[] bytes;
    private int bytePtr;
    private int bitPtr;

    public void init() {
        this.bytes = new byte[1024];
        this.bytePtr = 0;
        this.bitPtr = 0;
    }

    public void advance(int n) {
        this.bytePtr += n >> 3;
        this.bitPtr += n & 7;
        if (this.bitPtr > 7) {
            this.bitPtr -= 8;
            ++this.bytePtr;
        }
    }

    protected void setBuffer(byte[] arrby) {
        this.bytes = arrby;
    }

    public int peek() {
        return (this.bytes[this.bytePtr] & 255) >> 7 - this.bitPtr & 1;
    }

    public void read_from(byte[] arrby, int n, int n2) {
        for (int i = 0; i < n2; ++i) {
            this.bytes[i] = arrby[n + i];
        }
        this.bytePtr = 0;
        this.bitPtr = 0;
    }

    public int unpack(int n) {
        int n2 = 0;
        while (n != 0) {
            n2 <<= 1;
            n2 |= (this.bytes[this.bytePtr] & 255) >> 7 - this.bitPtr & 1;
            ++this.bitPtr;
            if (this.bitPtr == 8) {
                this.bitPtr = 0;
                ++this.bytePtr;
            }
            --n;
        }
        return n2;
    }

    public void pack(int n, int n2) {
        int n3;
        int n4 = n;
        while (this.bytePtr + (n2 + this.bitPtr >> 3) >= this.bytes.length) {
            n3 = this.bytes.length * 2;
            byte[] arrby = new byte[n3];
            System.arraycopy(this.bytes, 0, arrby, 0, this.bytes.length);
            this.bytes = arrby;
        }
        while (n2 > 0) {
            n3 = n4 >> n2 - 1 & 1;
            byte[] arrby = this.bytes;
            int n5 = this.bytePtr++;
            arrby[n5] = (byte)(arrby[n5] | n3 << 7 - this.bitPtr);
            ++this.bitPtr;
            if (this.bitPtr == 8) {
                this.bitPtr = 0;
            }
            --n2;
        }
    }

    public byte[] getBuffer() {
        return this.bytes;
    }

    public int getBufferSize() {
        return this.bytePtr + (this.bitPtr > 0 ? 1 : 0);
    }
}

