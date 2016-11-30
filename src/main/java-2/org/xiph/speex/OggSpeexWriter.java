/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.OggCrc;

public class OggSpeexWriter
extends AudioFileWriter {
    public static final int PACKETS_PER_OGG_PAGE = 250;
    private OutputStream out;
    private int mode;
    private int sampleRate;
    private int channels;
    private int nframes;
    private boolean vbr;
    private int size;
    private int streamSerialNumber;
    private byte[] dataBuffer;
    private int dataBufferPtr;
    private byte[] headerBuffer;
    private int headerBufferPtr;
    private int pageCount;
    private int packetCount;
    private long granulepos;

    public OggSpeexWriter() {
        if (this.streamSerialNumber == 0) {
            this.streamSerialNumber = new Random().nextInt();
        }
        this.dataBuffer = new byte[65565];
        this.dataBufferPtr = 0;
        this.headerBuffer = new byte[255];
        this.headerBufferPtr = 0;
        this.pageCount = 0;
        this.packetCount = 0;
        this.granulepos = 0;
    }

    public OggSpeexWriter(int n, int n2, int n3, int n4, boolean bl) {
        this();
        this.setFormat(n, n2, n3, n4, bl);
    }

    private void setFormat(int n, int n2, int n3, int n4, boolean bl) {
        this.mode = n;
        this.sampleRate = n2;
        this.channels = n3;
        this.nframes = n4;
        this.vbr = bl;
    }

    public void setSerialNumber(int n) {
        this.streamSerialNumber = n;
    }

    public void close() throws IOException {
        this.flush(true);
        this.out.close();
    }

    public void open(File file) throws IOException {
        file.delete();
        this.out = new FileOutputStream(file);
        this.size = 0;
    }

    public void open(String string) throws IOException {
        this.open(new File(string));
    }

    public void writeHeader(String string) throws IOException {
        byte[] arrby = OggSpeexWriter.buildOggPageHeader(2, 0, this.streamSerialNumber, this.pageCount++, 1, new byte[]{80});
        byte[] arrby2 = OggSpeexWriter.buildSpeexHeader(this.sampleRate, this.mode, this.channels, this.vbr, this.nframes);
        int n = OggCrc.checksum(0, arrby, 0, arrby.length);
        n = OggCrc.checksum(n, arrby2, 0, arrby2.length);
        OggSpeexWriter.writeInt(arrby, 22, n);
        this.out.write(arrby);
        this.out.write(arrby2);
        arrby = OggSpeexWriter.buildOggPageHeader(0, 0, this.streamSerialNumber, this.pageCount++, 1, new byte[]{(byte)(string.length() + 8)});
        arrby2 = OggSpeexWriter.buildSpeexComment(string);
        n = OggCrc.checksum(0, arrby, 0, arrby.length);
        n = OggCrc.checksum(n, arrby2, 0, arrby2.length);
        OggSpeexWriter.writeInt(arrby, 22, n);
        this.out.write(arrby);
        this.out.write(arrby2);
    }

    public void writePacket(byte[] arrby, int n, int n2) throws IOException {
        if (n2 <= 0) {
            return;
        }
        if (this.packetCount > 250) {
            this.flush(false);
        }
        System.arraycopy(arrby, n, this.dataBuffer, this.dataBufferPtr, n2);
        this.dataBufferPtr += n2;
        this.headerBuffer[this.headerBufferPtr++] = (byte)n2;
        ++this.packetCount;
        this.granulepos += (long)(this.nframes * (this.mode == 2 ? 640 : (this.mode == 1 ? 320 : 160)));
    }

    private void flush(boolean bl) throws IOException {
        byte[] arrby = OggSpeexWriter.buildOggPageHeader(bl ? 4 : 0, this.granulepos, this.streamSerialNumber, this.pageCount++, this.packetCount, this.headerBuffer);
        int n = OggCrc.checksum(0, arrby, 0, arrby.length);
        n = OggCrc.checksum(n, this.dataBuffer, 0, this.dataBufferPtr);
        OggSpeexWriter.writeInt(arrby, 22, n);
        this.out.write(arrby);
        this.out.write(this.dataBuffer, 0, this.dataBufferPtr);
        this.dataBufferPtr = 0;
        this.headerBufferPtr = 0;
        this.packetCount = 0;
    }
}

