/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex.spi;

import javax.sound.sampled.AudioFileFormat;

public class SpeexFileFormatType
extends AudioFileFormat.Type {
    public static final AudioFileFormat.Type SPEEX = new SpeexFileFormatType("SPEEX", "spx");

    public SpeexFileFormatType(String string, String string2) {
        super(string, string2);
    }
}

