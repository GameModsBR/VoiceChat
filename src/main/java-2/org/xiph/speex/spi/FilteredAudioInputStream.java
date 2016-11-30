/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex.spi;

import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public abstract class FilteredAudioInputStream
extends AudioInputStream {
    public static final int DEFAULT_BUFFER_SIZE = 2048;
    protected InputStream in;
    protected byte[] buf;
    protected int count;
    protected int pos;
    protected int markpos;
    protected int marklimit;
    private final byte[] single = new byte[1];
    protected byte[] prebuf;
    protected int precount;
    protected int prepos;

    protected void checkIfStillOpen() throws IOException {
        if (this.in == null) {
            throw new IOException("Stream closed");
        }
    }

    public FilteredAudioInputStream(InputStream inputStream, AudioFormat audioFormat, long l) {
        this(inputStream, audioFormat, l, 2048);
    }

    public FilteredAudioInputStream(InputStream inputStream, AudioFormat audioFormat, long l, int n) {
        this(inputStream, audioFormat, l, n, n);
    }

    public FilteredAudioInputStream(InputStream inputStream, AudioFormat audioFormat, long l, int n, int n2) {
        super(inputStream, audioFormat, l);
        this.in = inputStream;
        if (n <= 0 || n2 <= 0) {
            throw new IllegalArgumentException("Buffer size <= 0");
        }
        this.buf = new byte[n];
        this.count = 0;
        this.prebuf = new byte[n2];
        this.precount = 0;
        this.marklimit = n;
        this.markpos = -1;
    }

    protected void fill() throws IOException {
        int n;
        this.makeSpace();
        while ((n = this.in.read(this.prebuf, this.precount, this.prebuf.length - this.precount)) >= 0) {
            if (n <= 0) continue;
            this.precount += n;
            break;
        }
    }

    protected void makeSpace() {
        if (this.markpos < 0) {
            this.pos = 0;
        } else if (this.pos >= this.buf.length) {
            if (this.markpos > 0) {
                int n = this.pos - this.markpos;
                System.arraycopy(this.buf, this.markpos, this.buf, 0, n);
                this.pos = n;
                this.markpos = 0;
            } else if (this.buf.length >= this.marklimit) {
                this.markpos = -1;
                this.pos = 0;
            } else {
                int n = this.pos * 2;
                if (n > this.marklimit) {
                    n = this.marklimit;
                }
                byte[] arrby = new byte[n];
                System.arraycopy(this.buf, 0, arrby, 0, this.pos);
                this.buf = arrby;
            }
        }
        this.count = this.pos;
    }

    public synchronized int read() throws IOException {
        if (this.read(this.single, 0, 1) == -1) {
            return -1;
        }
        return this.single[0] & 255;
    }

    public synchronized int read(byte[] arrby, int n, int n2) throws IOException {
        this.checkIfStillOpen();
        if (n < 0 || n > arrby.length || n2 < 0 || n + n2 > arrby.length || n + n2 < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (n2 == 0) {
            return 0;
        }
        int n3 = this.count - this.pos;
        if (n3 <= 0) {
            this.fill();
            n3 = this.count - this.pos;
            if (n3 <= 0) {
                return -1;
            }
        }
        int n4 = n3 < n2 ? n3 : n2;
        System.arraycopy(this.buf, this.pos, arrby, n, n4);
        this.pos += n4;
        return n4;
    }

    public synchronized long skip(long l) throws IOException {
        this.checkIfStillOpen();
        if (l <= 0) {
            return 0;
        }
        if (this.pos < this.count) {
            int n = this.count - this.pos;
            if ((long)n > l) {
                this.pos = (int)((long)this.pos + l);
                return l;
            }
            this.pos = this.count;
            return n;
        }
        this.fill();
        int n = this.count - this.pos;
        if (n <= 0) {
            return 0;
        }
        long l2 = (long)n < l ? (long)n : l;
        this.pos = (int)((long)this.pos + l2);
        return l2;
    }

    public synchronized int available() throws IOException {
        this.checkIfStillOpen();
        return this.count - this.pos;
    }

    public synchronized void mark(int n) {
        if (n > this.buf.length - this.pos) {
            byte[] arrby = n <= this.buf.length ? this.buf : new byte[n];
            System.arraycopy(this.buf, this.pos, arrby, 0, this.count - this.pos);
            this.buf = arrby;
            this.count -= this.pos;
            this.markpos = 0;
            this.pos = 0;
        } else {
            this.markpos = this.pos;
        }
        this.marklimit = n;
    }

    public synchronized void reset() throws IOException {
        this.checkIfStillOpen();
        if (this.markpos < 0) {
            throw new IOException("Attempt to reset when no mark is valid");
        }
        this.pos = this.markpos;
    }

    public boolean markSupported() {
        return true;
    }

    public synchronized void close() throws IOException {
        if (this.in == null) {
            return;
        }
        this.in.close();
        this.in = null;
        this.buf = null;
        this.prebuf = null;
    }
}

