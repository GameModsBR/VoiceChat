/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  com.google.common.io.ByteArrayDataOutput
 */
package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPPacket;

public class UDPServerEntityDataPacket
extends UDPPacket {
    public int entityId;
    public String name;
    public double x;
    public double y;
    public double z;

    public UDPServerEntityDataPacket(String name, int entityId, double x, double y, double z) {
        this.name = name;
        this.entityId = entityId;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public byte id() {
        return 3;
    }

    @Override
    public void write(ByteArrayDataOutput out) {
        out.writeInt(this.entityId);
        out.writeUTF(this.name);
        out.writeDouble(this.x);
        out.writeDouble(this.y);
        out.writeDouble(this.z);
    }
}

