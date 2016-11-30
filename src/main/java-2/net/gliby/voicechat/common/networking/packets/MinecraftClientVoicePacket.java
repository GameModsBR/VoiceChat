/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  net.minecraftforge.fml.common.network.simpleimpl.IMessage
 *  net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
 *  net.minecraftforge.fml.common.network.simpleimpl.MessageContext
 */
package net.gliby.voicechat.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.networking.ClientNetwork;
import net.gliby.voicechat.client.networking.voiceclients.VoiceClient;
import net.gliby.voicechat.common.networking.MinecraftPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MinecraftClientVoicePacket
extends MinecraftPacket
implements IMessageHandler<MinecraftClientVoicePacket, IMessage> {
    byte divider;
    byte[] samples;
    int entityID;
    boolean direct;

    public MinecraftClientVoicePacket() {
    }

    public MinecraftClientVoicePacket(byte divider, byte[] samples, int entityID, boolean direct) {
        this.divider = divider;
        this.samples = samples;
        this.entityID = entityID;
        this.direct = direct;
    }

    public void fromBytes(ByteBuf buf) {
        this.divider = buf.readByte();
        this.entityID = buf.readInt();
        this.direct = buf.readBoolean();
        this.samples = new byte[buf.readableBytes()];
        buf.readBytes(this.samples);
    }

    public IMessage onMessage(MinecraftClientVoicePacket packet, MessageContext ctx) {
        if (VoiceChat.getProxyInstance().getClientNetwork().isConnected()) {
            VoiceChat.getProxyInstance().getClientNetwork().getVoiceClient().handlePacket(packet.entityID, packet.samples, packet.divider, packet.direct);
        }
        return null;
    }

    public void toBytes(ByteBuf buf) {
        buf.writeByte((int)this.divider);
        buf.writeInt(this.entityID);
        buf.writeBoolean(this.direct);
        buf.writeBytes(this.samples);
    }
}

