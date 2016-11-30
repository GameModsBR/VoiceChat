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

public class UDPServerChunkVoicePacket
extends UDPPacket {
    byte[] data;
    boolean direct;
    byte chunkSize;
    int entityId;

    public UDPServerChunkVoicePacket(byte[] samples, int entityID, boolean direct, byte chunkSize) {
        this.data = samples;
        this.entityId = entityID;
        this.direct = direct;
        this.chunkSize = chunkSize;
    }

    @Override
    public byte id() {
        return 5;
    }

    @Override
    public void write(ByteArrayDataOutput out) {
        out.writeInt(this.entityId);
        out.writeByte((int)this.chunkSize);
        out.writeBoolean(this.direct);
        UDPByteUtilities.writeBytes(this.data, out);
    }
}

