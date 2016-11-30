/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  com.google.common.io.ByteArrayDataOutput
 */
package net.gliby.voicechat.common.networking.voiceservers.udp;

import com.google.common.io.ByteArrayDataOutput;
import java.io.UnsupportedEncodingException;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPByteUtilities;
import net.gliby.voicechat.common.networking.voiceservers.udp.UDPPacket;

public class UDPClientAuthenticationPacket
extends UDPPacket {
    String hash;

    public UDPClientAuthenticationPacket(String hash) {
        this.hash = hash;
    }

    @Override
    public byte id() {
        return 0;
    }

    @Override
    public void write(ByteArrayDataOutput out) {
        try {
            UDPByteUtilities.writeBytes(this.hash.getBytes("UTF-8"), out);
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

