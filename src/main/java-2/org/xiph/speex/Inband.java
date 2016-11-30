/*
 * Decompiled with CFR 0_118.
 */
package org.xiph.speex;

import java.io.StreamCorruptedException;
import org.xiph.speex.Bits;
import org.xiph.speex.Stereo;

public class Inband {
    private Stereo stereo;

    public Inband(Stereo stereo) {
        this.stereo = stereo;
    }

    public void speexInbandRequest(Bits bits) throws StreamCorruptedException {
        int n = bits.unpack(4);
        switch (n) {
            case 0: {
                bits.advance(1);
                break;
            }
            case 1: {
                bits.advance(1);
                break;
            }
            case 2: {
                bits.advance(4);
                break;
            }
            case 3: {
                bits.advance(4);
                break;
            }
            case 4: {
                bits.advance(4);
                break;
            }
            case 5: {
                bits.advance(4);
                break;
            }
            case 6: {
                bits.advance(4);
                break;
            }
            case 7: {
                bits.advance(4);
                break;
            }
            case 8: {
                bits.advance(8);
                break;
            }
            case 9: {
                this.stereo.init(bits);
                break;
            }
            case 10: {
                bits.advance(16);
                break;
            }
            case 11: {
                bits.advance(16);
                break;
            }
            case 12: {
                bits.advance(32);
                break;
            }
            case 13: {
                bits.advance(32);
                break;
            }
            case 14: {
                bits.advance(64);
                break;
            }
            case 15: {
                bits.advance(64);
                break;
            }
        }
    }

    public void userInbandRequest(Bits bits) throws StreamCorruptedException {
        int n = bits.unpack(4);
        bits.advance(5 + 8 * n);
    }
}

