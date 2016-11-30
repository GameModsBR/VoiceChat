/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import java.io.StreamCorruptedException;
import org.xiph.speex.Bits;

public interface Decoder {
    public int decode(Bits var1, float[] var2) throws StreamCorruptedException;

    public void decodeStereo(float[] var1, int var2);

    public void setPerceptualEnhancement(boolean var1);

    public boolean getPerceptualEnhancement();

    public int getFrameSize();

    public boolean getDtx();

    public float[] getPiGain();

    public float[] getExc();

    public float[] getInnov();
}

