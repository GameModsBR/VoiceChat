/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import java.io.DataOutput;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AudioFileWriter {
    public abstract void close() throws IOException;

    public abstract void open(File var1) throws IOException;

    public abstract void open(String var1) throws IOException;

    public abstract void writeHeader(String var1) throws IOException;

    public abstract void writePacket(byte[] var1, int var2, int var3) throws IOException;

    public static int writeOggPageHeader(byte[] arrby, int n, int n2, long l, int n3, int n4, int n5, byte[] arrby2) {
        AudioFileWriter.writeString(arrby, n, "OggS");
        arrby[n + 4] = 0;
        arrby[n + 5] = (byte)n2;
        AudioFileWriter.writeLong(arrby, n + 6, l);
        AudioFileWriter.writeInt(arrby, n + 14, n3);
        AudioFileWriter.writeInt(arrby, n + 18, n4);
        AudioFileWriter.writeInt(arrby, n + 22, 0);
        arrby[n + 26] = (byte)n5;
        System.arraycopy(arrby2, 0, arrby, n + 27, n5);
        return n5 + 27;
    }

    public static byte[] buildOggPageHeader(int n, long l, int n2, int n3, int n4, byte[] arrby) {
        byte[] arrby2 = new byte[n4 + 27];
        AudioFileWriter.writeOggPageHeader(arrby2, 0, n, l, n2, n3, n4, arrby);
        return arrby2;
    }

    public static int writeSpeexHeader(byte[] arrby, int n, int n2, int n3, int n4, boolean bl, int n5) {
        AudioFileWriter.writeString(arrby, n, "Speex   ");
        AudioFileWriter.writeString(arrby, n + 8, "speex-1.0");
        System.arraycopy(new byte[11], 0, arrby, n + 17, 11);
        AudioFileWriter.writeInt(arrby, n + 28, 1);
        AudioFileWriter.writeInt(arrby, n + 32, 80);
        AudioFileWriter.writeInt(arrby, n + 36, n2);
        AudioFileWriter.writeInt(arrby, n + 40, n3);
        AudioFileWriter.writeInt(arrby, n + 44, 4);
        AudioFileWriter.writeInt(arrby, n + 48, n4);
        AudioFileWriter.writeInt(arrby, n + 52, -1);
        AudioFileWriter.writeInt(arrby, n + 56, 160 << n3);
        AudioFileWriter.writeInt(arrby, n + 60, bl ? 1 : 0);
        AudioFileWriter.writeInt(arrby, n + 64, n5);
        AudioFileWriter.writeInt(arrby, n + 68, 0);
        AudioFileWriter.writeInt(arrby, n + 72, 0);
        AudioFileWriter.writeInt(arrby, n + 76, 0);
        return 80;
    }

    public static byte[] buildSpeexHeader(int n, int n2, int n3, boolean bl, int n4) {
        byte[] arrby = new byte[80];
        AudioFileWriter.writeSpeexHeader(arrby, 0, n, n2, n3, bl, n4);
        return arrby;
    }

    public static int writeSpeexComment(byte[] arrby, int n, String string) {
        int n2 = string.length();
        AudioFileWriter.writeInt(arrby, n, n2);
        AudioFileWriter.writeString(arrby, n + 4, string);
        AudioFileWriter.writeInt(arrby, n + n2 + 4, 0);
        return n2 + 8;
    }

    public static byte[] buildSpeexComment(String string) {
        byte[] arrby = new byte[string.length() + 8];
        AudioFileWriter.writeSpeexComment(arrby, 0, string);
        return arrby;
    }

    public static void writeShort(DataOutput dataOutput, short s) throws IOException {
        dataOutput.writeByte(255 & s);
        dataOutput.writeByte(255 & s >>> 8);
    }

    public static void writeInt(DataOutput dataOutput, int n) throws IOException {
        dataOutput.writeByte(255 & n);
        dataOutput.writeByte(255 & n >>> 8);
        dataOutput.writeByte(255 & n >>> 16);
        dataOutput.writeByte(255 & n >>> 24);
    }

    public static void writeShort(OutputStream outputStream, short s) throws IOException {
        outputStream.write(255 & s);
        outputStream.write(255 & s >>> 8);
    }

    public static void writeInt(OutputStream outputStream, int n) throws IOException {
        outputStream.write(255 & n);
        outputStream.write(255 & n >>> 8);
        outputStream.write(255 & n >>> 16);
        outputStream.write(255 & n >>> 24);
    }

    public static void writeLong(OutputStream outputStream, long l) throws IOException {
        outputStream.write((int)(255 & l));
        outputStream.write((int)(255 & l >>> 8));
        outputStream.write((int)(255 & l >>> 16));
        outputStream.write((int)(255 & l >>> 24));
        outputStream.write((int)(255 & l >>> 32));
        outputStream.write((int)(255 & l >>> 40));
        outputStream.write((int)(255 & l >>> 48));
        outputStream.write((int)(255 & l >>> 56));
    }

    public static void writeShort(byte[] arrby, int n, int n2) {
        arrby[n] = (byte)(255 & n2);
        arrby[n + 1] = (byte)(255 & n2 >>> 8);
    }

    public static void writeInt(byte[] arrby, int n, int n2) {
        arrby[n] = (byte)(255 & n2);
        arrby[n + 1] = (byte)(255 & n2 >>> 8);
        arrby[n + 2] = (byte)(255 & n2 >>> 16);
        arrby[n + 3] = (byte)(255 & n2 >>> 24);
    }

    public static void writeLong(byte[] arrby, int n, long l) {
        arrby[n] = (byte)(255 & l);
        arrby[n + 1] = (byte)(255 & l >>> 8);
        arrby[n + 2] = (byte)(255 & l >>> 16);
        arrby[n + 3] = (byte)(255 & l >>> 24);
        arrby[n + 4] = (byte)(255 & l >>> 32);
        arrby[n + 5] = (byte)(255 & l >>> 40);
        arrby[n + 6] = (byte)(255 & l >>> 48);
        arrby[n + 7] = (byte)(255 & l >>> 56);
    }

    public static void writeString(byte[] arrby, int n, String string) {
        byte[] arrby2 = string.getBytes();
        System.arraycopy(arrby2, 0, arrby, n, arrby2.length);
    }
}

