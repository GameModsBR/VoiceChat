/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  com.google.common.io.ByteArrayDataOutput
 */
package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPByteUtilities;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPPacket;

public class UDPClientVoicePacket
extends UDPPacket {
    byte[] samples;
    byte divider;

    public UDPClientVoicePacket(byte divider, byte[] samples) {
        this.samples = samples;
        this.divider = divider;
    }

    @Override
    public byte id() {
        return 1;
    }

    @Override
    public void write(ByteArrayDataOutput out) {
        UDPByteUtilities.writeBytes(this.samples, out);
        out.writeByte((int)this.divider);
    }
}

