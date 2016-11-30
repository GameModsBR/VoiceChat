/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  com.google.common.io.ByteArrayDataOutput
 */
package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPPacket;

public class UDPServerVoiceEndPacket
extends UDPPacket {
    int entityID;

    public UDPServerVoiceEndPacket(int entityID) {
        this.entityID = entityID;
    }

    @Override
    public byte id() {
        return 2;
    }

    @Override
    public void write(ByteArrayDataOutput out) {
        out.writeInt(this.entityID);
    }
}

