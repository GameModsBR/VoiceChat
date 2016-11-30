/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import org.xiph.speex.Bits;
import org.xiph.speex.Encoder;
import org.xiph.speex.NbEncoder;
import org.xiph.speex.SbEncoder;
import org.xiph.speex.Stereo;

public class SpeexEncoder {
    public static final String VERSION = "Java Speex Encoder v0.9.7 ($Revision: 1.6 $)";
    private Encoder encoder;
    private Bits bits = new Bits();
    private float[] rawData;
    private int sampleRate;
    private int channels;
    private int frameSize;

    public boolean init(int n, int n2, int n3, int n4) {
        switch (n) {
            case 0: {
                this.encoder = new NbEncoder();
                ((NbEncoder)this.encoder).nbinit();
                break;
            }
            case 1: {
                this.encoder = new SbEncoder();
                ((SbEncoder)this.encoder).wbinit();
                break;
            }
            case 2: {
                this.encoder = new SbEncoder();
                ((SbEncoder)this.encoder).uwbinit();
                break;
            }
            default: {
                return false;
            }
        }
        this.encoder.setQuality(n2);
        this.frameSize = this.encoder.getFrameSize();
        this.sampleRate = n3;
        this.channels = n4;
        this.rawData = new float[n4 * this.frameSize];
        this.bits.init();
        return true;
    }

    public Encoder getEncoder() {
        return this.encoder;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public int getChannels() {
        return this.channels;
    }

    public int getFrameSize() {
        return this.frameSize;
    }

    public int getProcessedData(byte[] arrby, int n) {
        int n2 = this.bits.getBufferSize();
        System.arraycopy(this.bits.getBuffer(), 0, arrby, n, n2);
        this.bits.init();
        return n2;
    }

    public int getProcessedDataByteSize() {
        return this.bits.getBufferSize();
    }

    public boolean processData(byte[] arrby, int n, int n2) {
        SpeexEncoder.mapPcm16bitLittleEndian2Float(arrby, n, this.rawData, 0, n2 / 2);
        return this.processData(this.rawData, n2 / 2);
    }

    public boolean processData(short[] arrs, int n, int n2) {
        int n3 = this.channels * this.frameSize;
        if (n2 != n3) {
            throw new IllegalArgumentException("SpeexEncoder requires " + n3 + " samples to process a Frame, not " + n2);
        }
        for (int i = 0; i < n2; ++i) {
            this.rawData[i] = arrs[n + i];
        }
        return this.processData(this.rawData, n2);
    }

    public boolean processData(float[] arrf, int n) {
        int n2 = this.channels * this.frameSize;
        if (n != n2) {
            throw new IllegalArgumentException("SpeexEncoder requires " + n2 + " samples to process a Frame, not " + n);
        }
        if (this.channels == 2) {
            Stereo.encode(this.bits, arrf, this.frameSize);
        }
        this.encoder.encode(this.bits, arrf);
        return true;
    }

    public static void mapPcm16bitLittleEndian2Float(byte[] arrby, int n, float[] arrf, int n2, int n3) {
        if (arrby.length - n < 2 * n3) {
            throw new IllegalArgumentException("Insufficient Samples to convert to floats");
        }
        if (arrf.length - n2 < n3) {
            throw new IllegalArgumentException("Insufficient float buffer to convert the samples");
        }
        for (int i = 0; i < n3; ++i) {
            arrf[n2 + i] = arrby[n + 2 * i] & 255 | arrby[n + 2 * i + 1] << 8;
        }
    }
}

