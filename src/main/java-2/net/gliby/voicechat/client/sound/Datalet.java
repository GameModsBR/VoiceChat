/*
 * Decompiled with CFR 0_118.
 */
package net.gliby.voicechat.client.sound;

public class Datalet {
    public final int id;
    public final byte[] data;
    final boolean direct;

    Datalet(boolean direct, int id, byte[] data) {
        this.direct = direct;
        this.id = id;
        this.data = data;
    }
}

