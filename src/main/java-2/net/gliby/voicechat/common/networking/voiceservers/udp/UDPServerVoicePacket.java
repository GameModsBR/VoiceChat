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

public class UDPServerVoicePacket
extends UDPPacket {
    public int entityID;
    public boolean direct;
    public byte[] data;

    public UDPServerVoicePacket(byte[] data, int entityId, boolean global) {
        this.data = data;
        this.entityID = entityId;
        this.direct = global;
    }

    @Override
    public byte id() {
        return 1;
    }

    @Override
    public void write(ByteArrayDataOutput in) {
        in.writeInt(this.entityID);
        in.writeBoolean(this.direct);
        UDPByteUtilities.writeBytes(this.data, in);
    }
}

