/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import org.xiph.speex.AudioFileWriter;
import org.xiph.speex.Encoder;
import org.xiph.speex.OggCrc;
import org.xiph.speex.SpeexEncoder;
import org.xiph.speex.spi.FilteredAudioInputStream;
import org.xiph.speex.spi.SpeexEncoding;

public class Pcm2SpeexAudioInputStream
extends FilteredAudioInputStream {
    public static final int DEFAULT_BUFFER_SIZE = 2560;
    public static final int DEFAULT_SAMPLERATE = 8000;
    public static final int DEFAULT_CHANNELS = 1;
    public static final int DEFAULT_QUALITY = 3;
    public static final int DEFAULT_FRAMES_PER_PACKET = 1;
    public static final int DEFAULT_PACKETS_PER_OGG_PAGE = 20;
    public static final int UNKNOWN = -1;
    private SpeexEncoder encoder;
    private int mode;
    private int frameSize;
    private int framesPerPacket;
    private int channels;
    private String comment = null;
    private int granulepos = 0;
    private int streamSerialNumber;
    private int packetsPerOggPage;
    private int packetCount;
    private int pageCount;
    private int oggCount;
    private boolean first;

    public Pcm2SpeexAudioInputStream(InputStream inputStream, AudioFormat audioFormat, long l) {
        this(-1, -1, inputStream, audioFormat, l, 2560);
    }

    public Pcm2SpeexAudioInputStream(int n, int n2, InputStream inputStream, AudioFormat audioFormat, long l) {
        this(n, n2, inputStream, audioFormat, l, 2560);
    }

    public Pcm2SpeexAudioInputStream(InputStream inputStream, AudioFormat audioFormat, long l, int n) {
        this(-1, -1, inputStream, audioFormat, l, n);
    }

    public Pcm2SpeexAudioInputStream(int n, int n2, InputStream inputStream, AudioFormat audioFormat, long l, int n3) {
        super(inputStream, audioFormat, l, n3);
        if (this.streamSerialNumber == 0) {
            this.streamSerialNumber = new Random().nextInt();
        }
        this.packetsPerOggPage = 20;
        this.packetCount = 0;
        this.pageCount = 0;
        this.framesPerPacket = 1;
        int n4 = (int)audioFormat.getSampleRate();
        if (n4 < 0) {
            n4 = 8000;
        }
        this.channels = audioFormat.getChannels();
        if (this.channels < 0) {
            this.channels = 1;
        }
        if (n < 0) {
            n = n4 < 12000 ? 0 : (n4 < 24000 ? 1 : 2);
        }
        this.mode = n;
        AudioFormat.Encoding encoding = audioFormat.getEncoding();
        if (n2 < 0) {
            n2 = encoding instanceof SpeexEncoding ? ((SpeexEncoding)encoding).getQuality() : 3;
        }
        this.encoder = new SpeexEncoder();
        this.encoder.init(n, n2, n4, this.channels);
        if (encoding instanceof SpeexEncoding && ((SpeexEncoding)encoding).isVBR()) {
            this.setVbr(true);
        } else {
            this.setVbr(false);
        }
        this.frameSize = 2 * this.channels * this.encoder.getFrameSize();
        this.comment = "Encoded with Java Speex Encoder v0.9.7 ($Revision: 1.6 $)";
        this.first = true;
    }

    public void setSerialNumber(int n) {
        if (this.first) {
            this.streamSerialNumber = n;
        }
    }

    public void setFramesPerPacket(int n) {
        if (n <= 0) {
            n = 1;
        }
        this.framesPerPacket = n;
    }

    public void setPacketsPerOggPage(int n) {
        if (n <= 0) {
            n = 20;
        }
        if (n > 255) {
            n = 255;
        }
        this.packetsPerOggPage = n;
    }

    public void setComment(String string, boolean bl) {
        this.comment = string;
        if (bl) {
            this.comment = this.comment + "Java Speex Encoder v0.9.7 ($Revision: 1.6 $)";
        }
    }

    public void setQuality(int n) {
        this.encoder.getEncoder().setQuality(n);
        if (this.encoder.getEncoder().getVbr()) {
            this.encoder.getEncoder().setVbrQuality(n);
        }
    }

    public void setVbr(boolean bl) {
        this.encoder.getEncoder().setVbr(bl);
    }

    public Encoder getEncoder() {
        return this.encoder.getEncoder();
    }

    protected void fill() throws IOException {
        block18 : {
            this.makeSpace();
            if (this.first) {
                this.writeHeaderFrames();
                this.first = false;
            }
            do {
                int n;
                if (this.prebuf.length - this.prepos < this.framesPerPacket * this.frameSize * this.packetsPerOggPage) {
                    n = this.prepos + this.framesPerPacket * this.frameSize * this.packetsPerOggPage;
                    byte[] arrby = new byte[n];
                    System.arraycopy(this.prebuf, 0, arrby, 0, this.precount);
                    this.prebuf = arrby;
                }
                if ((n = this.in.read(this.prebuf, this.precount, this.prebuf.length - this.precount)) < 0) {
                    if ((this.precount - this.prepos) % 2 != 0) {
                        throw new StreamCorruptedException("Incompleted last PCM sample when stream ended");
                    }
                    while (this.prepos < this.precount) {
                        int n2;
                        if (this.precount - this.prepos < this.framesPerPacket * this.frameSize) {
                            while (this.precount < this.prepos + this.framesPerPacket * this.frameSize) {
                                this.prebuf[this.precount] = 0;
                                ++this.precount;
                            }
                        }
                        if (this.packetCount == 0) {
                            this.writeOggPageHeader(this.packetsPerOggPage, 0);
                        }
                        for (n2 = 0; n2 < this.framesPerPacket; ++n2) {
                            this.encoder.processData(this.prebuf, this.prepos, this.frameSize);
                            this.prepos += this.frameSize;
                        }
                        n2 = this.encoder.getProcessedDataByteSize();
                        while (this.buf.length - this.oggCount < n2) {
                            int n3 = this.buf.length * 2;
                            byte[] arrby = new byte[n3];
                            System.arraycopy(this.buf, 0, arrby, 0, this.oggCount);
                            this.buf = arrby;
                        }
                        this.buf[this.count + 27 + this.packetCount] = (byte)(255 & n2);
                        this.encoder.getProcessedData(this.buf, this.oggCount);
                        this.oggCount += n2;
                        ++this.packetCount;
                        if (this.packetCount < this.packetsPerOggPage) continue;
                        this.writeOggPageChecksum();
                        return;
                    }
                    if (this.packetCount > 0) {
                        this.buf[this.count + 5] = 4;
                        this.buf[this.count + 26] = (byte)(255 & this.packetCount);
                        System.arraycopy(this.buf, this.count + 27 + this.packetsPerOggPage, this.buf, this.count + 27 + this.packetCount, this.oggCount - (this.count + 27 + this.packetsPerOggPage));
                        this.oggCount -= this.packetsPerOggPage - this.packetCount;
                        this.writeOggPageChecksum();
                    }
                    return;
                }
                if (n > 0) {
                    this.precount += n;
                    if (this.precount - this.prepos < this.framesPerPacket * this.frameSize * this.packetsPerOggPage) continue;
                    while (this.precount - this.prepos >= this.framesPerPacket * this.frameSize * this.packetsPerOggPage) {
                        if (this.packetCount == 0) {
                            this.writeOggPageHeader(this.packetsPerOggPage, 0);
                        }
                        while (this.packetCount < this.packetsPerOggPage) {
                            int n4;
                            for (n4 = 0; n4 < this.framesPerPacket; ++n4) {
                                this.encoder.processData(this.prebuf, this.prepos, this.frameSize);
                                this.prepos += this.frameSize;
                            }
                            n4 = this.encoder.getProcessedDataByteSize();
                            while (this.buf.length - this.oggCount < n4) {
                                int n5 = this.buf.length * 2;
                                byte[] arrby = new byte[n5];
                                System.arraycopy(this.buf, 0, arrby, 0, this.oggCount);
                                this.buf = arrby;
                            }
                            this.buf[this.count + 27 + this.packetCount] = (byte)(255 & n4);
                            this.encoder.getProcessedData(this.buf, this.oggCount);
                            this.oggCount += n4;
                            ++this.packetCount;
                        }
                        if (this.packetCount < this.packetsPerOggPage) continue;
                        this.writeOggPageChecksum();
                    }
                    System.arraycopy(this.prebuf, this.prepos, this.prebuf, 0, this.precount - this.prepos);
                    this.precount -= this.prepos;
                    this.prepos = 0;
                    return;
                }
                if (this.precount < this.prebuf.length) break block18;
                if (this.prepos <= 0) break;
                System.arraycopy(this.prebuf, this.prepos, this.prebuf, 0, this.precount - this.prepos);
                this.precount -= this.prepos;
                this.prepos = 0;
            } while (true);
            return;
        }
    }

    public synchronized int available() throws IOException {
        int n = super.available();
        int n2 = this.precount - this.prepos + this.in.available();
        if (this.encoder.getEncoder().getVbr()) {
            switch (this.mode) {
                case 0: {
                    return n + (27 + 2 * this.packetsPerOggPage) * (n2 / (this.packetsPerOggPage * this.framesPerPacket * 320));
                }
                case 1: {
                    return n + (27 + 2 * this.packetsPerOggPage) * (n2 / (this.packetsPerOggPage * this.framesPerPacket * 640));
                }
                case 2: {
                    return n + (27 + 3 * this.packetsPerOggPage) * (n2 / (this.packetsPerOggPage * this.framesPerPacket * 1280));
                }
            }
            return n;
        }
        int n3 = this.encoder.getEncoder().getEncodedFrameSize();
        if (this.channels > 1) {
            n3 += 17;
        }
        n3 *= this.framesPerPacket;
        n3 = n3 + 7 >> 3;
        int n4 = 27 + this.packetsPerOggPage * (n3 + 1);
        switch (this.mode) {
            case 0: {
                int n5 = this.framesPerPacket * 320 * this.encoder.getChannels();
                return n += n4 * (n2 / (this.packetsPerOggPage * n5));
            }
            case 1: {
                int n6 = this.framesPerPacket * 640 * this.encoder.getChannels();
                return n += n4 * (n2 / (this.packetsPerOggPage * n6));
            }
            case 2: {
                int n7 = this.framesPerPacket * 1280 * this.encoder.getChannels();
                return n += n4 * (n2 / (this.packetsPerOggPage * n7));
            }
        }
        return n;
    }

    private void writeOggPageHeader(int n, int n2) {
        while (this.buf.length - this.count < 27 + n) {
            int n3 = this.buf.length * 2;
            byte[] arrby = new byte[n3];
            System.arraycopy(this.buf, 0, arrby, 0, this.count);
            this.buf = arrby;
        }
        AudioFileWriter.writeOggPageHeader(this.buf, this.count, n2, this.granulepos, this.streamSerialNumber, this.pageCount++, n, new byte[n]);
        this.oggCount = this.count + 27 + n;
    }

    private void writeOggPageChecksum() {
        this.granulepos += this.framesPerPacket * this.frameSize * this.packetCount / 2;
        AudioFileWriter.writeLong(this.buf, this.count + 6, this.granulepos);
        int n = OggCrc.checksum(0, this.buf, this.count, this.oggCount - this.count);
        AudioFileWriter.writeInt(this.buf, this.count + 22, n);
        this.count = this.oggCount;
        this.packetCount = 0;
    }

    private void writeHeaderFrames() {
        int n;
        int n2 = this.comment.length();
        if (n2 > 247) {
            this.comment = this.comment.substring(0, 247);
            n2 = 247;
        }
        while (this.buf.length - this.count < n2 + 144) {
            n = this.buf.length * 2;
            byte[] arrby = new byte[n];
            System.arraycopy(this.buf, 0, arrby, 0, this.count);
            this.buf = arrby;
        }
        AudioFileWriter.writeOggPageHeader(this.buf, this.count, 2, this.granulepos, this.streamSerialNumber, this.pageCount++, 1, new byte[]{80});
        this.oggCount = this.count + 28;
        AudioFileWriter.writeSpeexHeader(this.buf, this.oggCount, this.encoder.getSampleRate(), this.mode, this.encoder.getChannels(), this.encoder.getEncoder().getVbr(), this.framesPerPacket);
        this.oggCount += 80;
        n = OggCrc.checksum(0, this.buf, this.count, this.oggCount - this.count);
        AudioFileWriter.writeInt(this.buf, this.count + 22, n);
        this.count = this.oggCount;
        AudioFileWriter.writeOggPageHeader(this.buf, this.count, 0, this.granulepos, this.streamSerialNumber, this.pageCount++, 1, new byte[]{(byte)(n2 + 8)});
        this.oggCount = this.count + 28;
        AudioFileWriter.writeSpeexComment(this.buf, this.oggCount, this.comment);
        this.oggCount += n2 + 8;
        n = OggCrc.checksum(0, this.buf, this.count, this.oggCount - this.count);
        AudioFileWriter.writeInt(this.buf, this.count + 22, n);
        this.count = this.oggCount;
        this.packetCount = 0;
    }
}

