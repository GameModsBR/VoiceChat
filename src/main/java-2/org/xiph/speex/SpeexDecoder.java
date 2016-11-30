/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import java.io.StreamCorruptedException;
import org.xiph.speex.Bits;
import org.xiph.speex.Decoder;
import org.xiph.speex.NbDecoder;
import org.xiph.speex.SbDecoder;

public class SpeexDecoder {
    public static final String VERSION = "Java Speex Decoder v0.9.7 ($Revision: 1.4 $)";
    private int sampleRate = 0;
    private int channels = 0;
    private float[] decodedData;
    private short[] outputData;
    private int outputSize;
    private Bits bits = new Bits();
    private Decoder decoder;
    private int frameSize;

    public boolean init(int n, int n2, int n3, boolean bl) {
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
            default: {
                return false;
            }
        }
        this.decoder.setPerceptualEnhancement(bl);
        this.frameSize = this.decoder.getFrameSize();
        this.sampleRate = n2;
        this.channels = n3;
        int n4 = n2 * n3;
        this.decodedData = new float[n4 * 2];
        this.outputData = new short[n4 * 2];
        this.outputSize = 0;
        this.bits.init();
        return true;
    }

    public int getSampleRate() {
        return this.sampleRate;
    }

    public int getChannels() {
        return this.channels;
    }

    public int getProcessedData(byte[] arrby, int n) {
        int n2;
        if (this.outputSize <= 0) {
            return this.outputSize;
        }
        for (n2 = 0; n2 < this.outputSize; ++n2) {
            int n3 = n + (n2 << 1);
            arrby[n3] = (byte)(this.outputData[n2] & 255);
            arrby[n3 + 1] = (byte)(this.outputData[n2] >> 8 & 255);
        }
        n2 = this.outputSize * 2;
        this.outputSize = 0;
        return n2;
    }

    public int getProcessedData(short[] arrs, int n) {
        if (this.outputSize <= 0) {
            return this.outputSize;
        }
        System.arraycopy(this.outputData, 0, arrs, n, this.outputSize);
        int n2 = this.outputSize;
        this.outputSize = 0;
        return n2;
    }

    public int getProcessedDataByteSize() {
        return this.outputSize * 2;
    }

    public void processData(byte[] arrby, int n, int n2) throws StreamCorruptedException {
        if (arrby == null) {
            this.processData(true);
        } else {
            this.bits.read_from(arrby, n, n2);
            this.processData(false);
        }
    }

    public void processData(boolean bl) throws StreamCorruptedException {
        int n;
        if (bl) {
            this.decoder.decode(null, this.decodedData);
        } else {
            this.decoder.decode(this.bits, this.decodedData);
        }
        if (this.channels == 2) {
            this.decoder.decodeStereo(this.decodedData, this.frameSize);
        }
        for (n = 0; n < this.frameSize * this.channels; ++n) {
            if (this.decodedData[n] > 32767.0f) {
                this.decodedData[n] = 32767.0f;
                continue;
            }
            if (this.decodedData[n] >= -32768.0f) continue;
            this.decodedData[n] = -32768.0f;
        }
        n = 0;
        while (n < this.frameSize * this.channels) {
            this.outputData[this.outputSize] = this.decodedData[n] > 0.0f ? (short)((double)this.decodedData[n] + 0.5) : (short)((double)this.decodedData[n] - 0.5);
            ++n;
            ++this.outputSize;
        }
    }
}

