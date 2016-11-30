/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.xiph.speex.AudioFileWriter;

public class PcmWaveWriter
extends AudioFileWriter {
    public static final short WAVE_FORMAT_PCM = 1;
    public static final short WAVE_FORMAT_SPEEX = -24311;
    public static final int[][][] WAVE_FRAME_SIZES = new int[][][]{{{8, 8, 8, 1, 1, 2, 2, 2, 2, 2, 2}, {2, 1, 1, 7, 7, 8, 8, 8, 8, 3, 3}}, {{8, 8, 8, 2, 1, 1, 2, 2, 2, 2, 2}, {1, 2, 2, 8, 7, 6, 3, 3, 3, 3, 3}}, {{8, 8, 8, 1, 2, 2, 1, 1, 1, 1, 1}, {2, 1, 1, 7, 8, 3, 6, 6, 5, 5, 5}}};
    public static final int[][][] WAVE_BITS_PER_FRAME = new int[][][]{{{43, 79, 119, 160, 160, 220, 220, 300, 300, 364, 492}, {60, 96, 136, 177, 177, 237, 237, 317, 317, 381, 509}}, {{79, 115, 155, 196, 256, 336, 412, 476, 556, 684, 844}, {96, 132, 172, 213, 273, 353, 429, 493, 573, 701, 861}}, {{83, 151, 191, 232, 292, 372, 448, 512, 592, 720, 880}, {100, 168, 208, 249, 309, 389, 465, 529, 609, 737, 897}}};
    private RandomAccessFile raf;
    private int mode;
    private int quality;
    private int sampleRate;
    private int channels;
    private int nframes;
    private boolean vbr;
    private int size = 0;
    private boolean isPCM;

    public PcmWaveWriter() {
    }

    public PcmWaveWriter(int n, int n2) {
        this();
        this.setPCMFormat(n, n2);
    }

    public PcmWaveWriter(int n, int n2, int n3, int n4, int n5, boolean bl) {
        this();
        this.setSpeexFormat(n, n2, n3, n4, n5, bl);
    }

    private void setPCMFormat(int n, int n2) {
        this.channels = n2;
        this.sampleRate = n;
        this.isPCM = true;
    }

    private void setSpeexFormat(int n, int n2, int n3, int n4, int n5, boolean bl) {
        this.mode = n;
        this.quality = n2;
        this.sampleRate = n3;
        this.channels = n4;
        this.nframes = n5;
        this.vbr = bl;
        this.isPCM = false;
    }

    public void close() throws IOException {
        this.raf.seek(4);
        int n = (int)this.raf.length() - 8;
        PcmWaveWriter.writeInt(this.raf, n);
        this.raf.seek(40);
        PcmWaveWriter.writeInt(this.raf, this.size);
        this.raf.close();
    }

    public void open(File file) throws IOException {
        file.delete();
        this.raf = new RandomAccessFile(file, "rw");
        this.size = 0;
    }

    public void open(String string) throws IOException {
        this.open(new File(string));
    }

    public void writeHeader(String string) throws IOException {
        byte[] arrby = "RIFF".getBytes();
        this.raf.write(arrby, 0, arrby.length);
        PcmWaveWriter.writeInt(this.raf, 0);
        arrby = "WAVE".getBytes();
        this.raf.write(arrby, 0, arrby.length);
        arrby = "fmt ".getBytes();
        this.raf.write(arrby, 0, arrby.length);
        if (this.isPCM) {
            PcmWaveWriter.writeInt(this.raf, 16);
            PcmWaveWriter.writeShort(this.raf, 1);
            PcmWaveWriter.writeShort(this.raf, (short)this.channels);
            PcmWaveWriter.writeInt(this.raf, this.sampleRate);
            PcmWaveWriter.writeInt(this.raf, this.sampleRate * this.channels * 2);
            PcmWaveWriter.writeShort(this.raf, (short)(this.channels * 2));
            PcmWaveWriter.writeShort(this.raf, 16);
        } else {
            int n = string.length();
            PcmWaveWriter.writeInt(this.raf, (int)((short)(100 + n)));
            PcmWaveWriter.writeShort(this.raf, -24311);
            PcmWaveWriter.writeShort(this.raf, (short)this.channels);
            PcmWaveWriter.writeInt(this.raf, this.sampleRate);
            PcmWaveWriter.writeInt(this.raf, PcmWaveWriter.calculateEffectiveBitrate(this.mode, this.channels, this.quality) + 7 >> 3);
            PcmWaveWriter.writeShort(this.raf, (short)PcmWaveWriter.calculateBlockSize(this.mode, this.channels, this.quality));
            PcmWaveWriter.writeShort(this.raf, (short)this.quality);
            PcmWaveWriter.writeShort(this.raf, (short)(82 + n));
            this.raf.writeByte(1);
            this.raf.writeByte(0);
            this.raf.write(PcmWaveWriter.buildSpeexHeader(this.sampleRate, this.mode, this.channels, this.vbr, this.nframes));
            this.raf.writeBytes(string);
        }
        arrby = "data".getBytes();
        this.raf.write(arrby, 0, arrby.length);
        PcmWaveWriter.writeInt(this.raf, 0);
    }

    public void writePacket(byte[] arrby, int n, int n2) throws IOException {
        this.raf.write(arrby, n, n2);
        this.size += n2;
    }

    private static final int calculateEffectiveBitrate(int n, int n2, int n3) {
        return (WAVE_FRAME_SIZES[n - 1][n2 - 1][n3] * WAVE_BITS_PER_FRAME[n - 1][n2 - 1][n3] + 7 >> 3) * 50 * 8 / WAVE_BITS_PER_FRAME[n - 1][n2 - 1][n3];
    }

    private static final int calculateBlockSize(int n, int n2, int n3) {
        return WAVE_FRAME_SIZES[n - 1][n2 - 1][n3] * WAVE_BITS_PER_FRAME[n - 1][n2 - 1][n3] + 7 >> 3;
    }
}

