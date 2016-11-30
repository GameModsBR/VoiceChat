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

public class MinecraftClientVoiceEndPacket
extends MinecraftPacket
implements IMessageHandler<MinecraftClientVoiceEndPacket, IMessage> {
    int entityID;

    public MinecraftClientVoiceEndPacket() {
    }

    public MinecraftClientVoiceEndPacket(int entityID) {
        this.entityID = entityID;
    }

    public void fromBytes(ByteBuf buf) {
        this.entityID = buf.readInt();
    }

    public IMessage onMessage(MinecraftClientVoiceEndPacket packet, MessageContext ctx) {
        if (VoiceChat.getProxyInstance().getClientNetwork().isConnected()) {
            VoiceChat.getProxyInstance().getClientNetwork().getVoiceClient().handleEnd(packet.entityID);
        }
        return null;
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityID);
    }
}

