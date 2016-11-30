/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.xiph.speex.AudioFileWriter;

public class RawWriter
extends AudioFileWriter {
    private OutputStream out;

    public void close() throws IOException {
        this.out.close();
    }

    public void open(File file) throws IOException {
        file.delete();
        this.out = new FileOutputStream(file);
    }

    public void open(String string) throws IOException {
        this.open(new File(string));
    }

    public void writeHeader(String string) throws IOException {
    }

    public void writePacket(byte[] arrby, int n, int n2) throws IOException {
        this.out.write(arrby, n, n2);
    }
}

