/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  com.google.common.io.ByteArrayDataOutput
 */
package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;

public abstract class UDPPacket {
    public abstract byte id();

    public abstract void write(ByteArrayDataOutput var1);
}

