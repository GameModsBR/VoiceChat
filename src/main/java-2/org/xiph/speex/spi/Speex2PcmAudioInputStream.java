/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex.spi;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import javax.sound.sampled.AudioFormat;
import org.xiph.speex.Bits;
import org.xiph.speex.Decoder;
import org.xiph.speex.NbDecoder;
import org.xiph.speex.SbDecoder;
import org.xiph.speex.spi.FilteredAudioInputStream;

public class Speex2PcmAudioInputStream
extends FilteredAudioInputStream {
    private boolean initialised = false;
    private int sampleRate;
    private int channelCount;
    private float[] decodedData;
    private byte[] outputData;
    private Bits bits = new Bits();
    private Decoder decoder;
    private int frameSize;
    private int framesPerPacket;
    private int streamSerialNumber;
    private int packetsPerOggPage;
    private int packetCount;
    private byte[] packetSizes = new byte[256];

    public Speex2PcmAudioInputStream(InputStream inputStream, AudioFormat audioFormat, long l) {
        this(inputStream, audioFormat, l, 2048);
    }

    public Speex2PcmAudioInputStream(InputStream inputStream, AudioFormat audioFormat, long l, int n) {
        super(inputStream, audioFormat, l, n);
    }

    protected void initialise(boolean bl) throws IOException {
        while (!this.initialised) {
            int n;
            int n2 = this.prebuf.length - this.precount - 1;
            int n3 = this.in.available();
            if (!bl && n3 <= 0) {
                return;
            }
            n2 = n3 > 0 ? Math.min(n3, n2) : n2;
            int n4 = this.in.read(this.prebuf, this.precount, n2);
            if (n4 < 0) {
                throw new StreamCorruptedException("Incomplete Ogg Headers");
            }
            if (n4 == 0) {
                // empty if block
            }
            this.precount += n4;
            if (this.decoder == null && this.precount >= 108) {
                if (!new String(this.prebuf, 0, 4).equals("OggS")) {
                    throw new StreamCorruptedException("The given stream does not appear to be Ogg.");
                }
                this.streamSerialNumber = Speex2PcmAudioInputStream.readInt(this.prebuf, 14);
                if (!new String(this.prebuf, 28, 8).equals("Speex   ")) {
                    throw new StreamCorruptedException("The given stream does not appear to be Ogg Speex.");
                }
                this.sampleRate = Speex2PcmAudioInputStream.readInt(this.prebuf, 64);
                this.channelCount = Speex2PcmAudioInputStream.readInt(this.prebuf, 76);
                this.framesPerPacket = Speex2PcmAudioInputStream.readInt(this.prebuf, 92);
                n = Speex2PcmAudioInputStream.readInt(this.prebuf, 68);
                switch (n) {
                    case 0: {
                        this.decoder = new NbDecoder();
                        ((NbDecoder)this.decoder).nbinit();
                        break;
                    }
                    case 1: {
                        this.decoder = new SbDecoder();
                        ((SbDecoder)this.decoder).wbinit();
                        break;
                    }
                    case 2: {
                        this.decoder = new SbDecoder();
                        ((SbDecoder)this.decoder).uwbinit();
                        break;
                    }
                }
                this.decoder.setPerceptualEnhancement(true);
                this.frameSize = this.decoder.getFrameSize();
                this.decodedData = new float[this.frameSize * this.channelCount];
                this.outputData = new byte[2 * this.frameSize * this.channelCount * this.framesPerPacket];
                this.bits.init();
            }
            if (this.decoder == null || this.precount < 135) continue;
            this.packetsPerOggPage = 255 & this.prebuf[134];
            if (this.precount < 135 + this.packetsPerOggPage) continue;
            n = 0;
            for (int i = 0; i < this.packetsPerOggPage; ++i) {
                n += 255 & this.prebuf[135 + i];
            }
            if (this.precount < 135 + this.packetsPerOggPage + n) continue;
            this.prepos = 135 + this.packetsPerOggPage + n;
            this.packetsPerOggPage = 0;
            this.packetCount = 255;
            this.initialised = true;
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    protected void fill() throws IOException {
        block10 : {
            int n;
            this.makeSpace();
            while (!this.initialised) {
                this.initialise(true);
            }
            while ((n = this.in.read(this.prebuf, this.precount, this.prebuf.length - this.precount)) >= 0) {
                if (n < 0) continue;
                this.precount += n;
                if (this.packetCount >= this.packetsPerOggPage) {
                    this.readOggPageHeader();
                }
                if (this.packetCount >= this.packetsPerOggPage || this.precount - this.prepos < this.packetSizes[this.packetCount]) continue;
                break block10;
            }
            do {
                byte by;
                if (this.prepos >= this.precount) {
                    return;
                }
                if (this.packetCount >= this.packetsPerOggPage) {
                    this.readOggPageHeader();
                }
                if (this.packetCount >= this.packetsPerOggPage) continue;
                if (this.precount - this.prepos < (by = this.packetSizes[this.packetCount++])) {
                    throw new StreamCorruptedException("Incompleted last Speex packet");
                }
                this.decode(this.prebuf, this.prepos, by);
                this.prepos += by;
                while (this.buf.length - this.count < this.outputData.length) {
                    int n2 = this.buf.length * 2;
                    byte[] arrby = new byte[n2];
                    System.arraycopy(this.buf, 0, arrby, 0, this.count);
                    this.buf = arrby;
                }
                System.arraycopy(this.outputData, 0, this.buf, this.count, this.outputData.length);
                this.count += this.outputData.length;
            } while (true);
        }
        while (this.precount - this.prepos >= this.packetSizes[this.packetCount] && this.packetCount < this.packetsPerOggPage) {
            byte by = this.packetSizes[this.packetCount++];
            this.decode(this.prebuf, this.prepos, by);
            this.prepos += by;
            while (this.buf.length - this.count < this.outputData.length) {
                int n = this.buf.length * 2;
                byte[] arrby = new byte[n];
                System.arraycopy(this.buf, 0, arrby, 0, this.count);
                this.buf = arrby;
            }
            System.arraycopy(this.outputData, 0, this.buf, this.count, this.outputData.length);
            this.count += this.outputData.length;
            if (this.packetCount < this.packetsPerOggPage) continue;
            this.readOggPageHeader();
        }
        System.arraycopy(this.prebuf, this.prepos, this.prebuf, 0, this.precount - this.prepos);
        this.precount -= this.prepos;
        this.prepos = 0;
    }

    protected void decode(byte[] arrby, int n, int n2) throws StreamCorruptedException {
        int n3 = 0;
        this.bits.read_from(arrby, n, n2);
        for (int i = 0; i < this.framesPerPacket; ++i) {
            int n4;
            this.decoder.decode(this.bits, this.decodedData);
            if (this.channelCount == 2) {
                this.decoder.decodeStereo(this.decodedData, this.frameSize);
            }
            for (n4 = 0; n4 < this.frameSize * this.channelCount; ++n4) {
                if (this.decodedData[n4] > 32767.0f) {
                    this.decodedData[n4] = 32767.0f;
                    continue;
                }
                if (this.decodedData[n4] >= -32768.0f) continue;
                this.decodedData[n4] = -32768.0f;
            }
            for (n4 = 0; n4 < this.frameSize * this.channelCount; ++n4) {
                short s = this.decodedData[n4] > 0.0f ? (short)((double)this.decodedData[n4] + 0.5) : (short)((double)this.decodedData[n4] - 0.5);
                this.outputData[n3++] = (byte)(s & 255);
                this.outputData[n3++] = (byte)(s >> 8 & 255);
            }
        }
    }

    public synchronized long skip(long l) throws IOException {
        while (!this.initialised) {
            this.initialise(true);
        }
        this.checkIfStillOpen();
        if (l <= 0) {
            return 0;
        }
        if (this.pos < this.count) {
            return super.skip(l);
        }
        int n = 2 * this.framesPerPacket * this.frameSize * this.channelCount;
        if (this.markpos < 0 && l >= (long)n) {
            if (this.packetCount >= this.packetsPerOggPage) {
                this.readOggPageHeader();
            }
            if (this.packetCount < this.packetsPerOggPage) {
                int n2;
                int n3 = 0;
                if (this.precount - this.prepos < this.packetSizes[this.packetCount] && (n2 = this.in.available()) > 0) {
                    int n4 = Math.min(this.prebuf.length - this.precount, n2);
                    int n5 = this.in.read(this.prebuf, this.precount, n4);
                    if (n5 < 0) {
                        throw new IOException("End of stream but there are still supposed to be packets to decode");
                    }
                    this.precount += n5;
                }
                while (this.precount - this.prepos >= this.packetSizes[this.packetCount] && this.packetCount < this.packetsPerOggPage && l >= (long)n) {
                    this.prepos += this.packetSizes[this.packetCount++];
                    n3 += n;
                    l -= (long)n;
                    if (this.packetCount < this.packetsPerOggPage) continue;
                    this.readOggPageHeader();
                }
                System.arraycopy(this.prebuf, this.prepos, this.prebuf, 0, this.precount - this.prepos);
                this.precount -= this.prepos;
                this.prepos = 0;
                return n3;
            }
        }
        return super.skip(l);
    }

    public synchronized int available() throws IOException {
        if (!this.initialised) {
            this.initialise(false);
            if (!this.initialised) {
                return 0;
            }
        }
        int n = super.available();
        if (this.packetCount >= this.packetsPerOggPage) {
            this.readOggPageHeader();
        }
        if (this.packetCount < this.packetsPerOggPage) {
            int n2 = this.precount - this.prepos + this.in.available();
            byte by = this.packetSizes[this.packetCount];
            int n3 = 0;
            while (by < n2 && this.packetCount + n3 < this.packetsPerOggPage) {
                n2 -= by;
                n += 2 * this.frameSize * this.framesPerPacket;
                by = this.packetSizes[this.packetCount + ++n3];
            }
        }
        return n;
    }

    private void readOggPageHeader() throws IOException {
        int n;
        int n2;
        int n3;
        int n4 = 0;
        if (this.precount - this.prepos < 27 && (n3 = this.in.available()) > 0) {
            n = Math.min(this.prebuf.length - this.precount, n3);
            n2 = this.in.read(this.prebuf, this.precount, n);
            if (n2 < 0) {
                throw new IOException("End of stream but available was positive");
            }
            this.precount += n2;
        }
        if (this.precount - this.prepos >= 27) {
            if (!new String(this.prebuf, this.prepos, 4).equals("OggS")) {
                throw new StreamCorruptedException("Lost Ogg Sync");
            }
            if (this.streamSerialNumber != Speex2PcmAudioInputStream.readInt(this.prebuf, this.prepos + 14)) {
                throw new StreamCorruptedException("Ogg Stream Serial Number mismatch");
            }
            n4 = 255 & this.prebuf[this.prepos + 26];
        }
        if (this.precount - this.prepos < 27 + n4 && (n3 = this.in.available()) > 0) {
            n = Math.min(this.prebuf.length - this.precount, n3);
            n2 = this.in.read(this.prebuf, this.precount, n);
            if (n2 < 0) {
                throw new IOException("End of stream but available was positive");
            }
            this.precount += n2;
        }
        if (this.precount - this.prepos >= 27 + n4) {
            System.arraycopy(this.prebuf, this.prepos + 27, this.packetSizes, 0, n4);
            this.packetCount = 0;
            this.prepos += 27 + n4;
            this.packetsPerOggPage = n4;
        }
    }

    private static int readInt(byte[] arrby, int n) {
        return arrby[n] & 255 | (arrby[n + 1] & 255) << 8 | (arrby[n + 2] & 255) << 16 | arrby[n + 3] << 24;
    }
}

