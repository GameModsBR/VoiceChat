/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 *  net.minecraft.entity.player.EntityPlayerMP
 *  net.minecraft.network.NetHandlerPlayServer
 *  net.minecraftforge.fml.common.network.simpleimpl.IMessage
 *  net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler
 *  net.minecraftforge.fml.common.network.simpleimpl.MessageContext
 */
package net.gliby.voicechat.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.MinecraftPacket;
import net.gliby.voicechat.common.networking.voiceservers.VoiceServer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MinecraftServerVoicePacket
extends MinecraftPacket
implements IMessageHandler<MinecraftServerVoicePacket, IMessage> {
    private byte[] data;
    private byte divider;

    public MinecraftServerVoicePacket() {
    }

    public MinecraftServerVoicePacket(byte divider, byte[] data) {
        this.divider = divider;
        this.data = data;
    }

    public void fromBytes(ByteBuf buf) {
        this.divider = buf.readByte();
        this.data = new byte[buf.readableBytes()];
        buf.readBytes(this.data);
    }

    public IMessage onMessage(MinecraftServerVoicePacket packet, MessageContext ctx) {
        VoiceChat.getServerInstance().getVoiceServer().handleVoiceData(ctx.getServerHandler().playerEntity, packet.data, packet.divider, ctx.getServerHandler().playerEntity.getEntityId(), false);
        return null;
    }

    public void toBytes(ByteBuf buf) {
        buf.writeByte((int)this.divider);
        buf.writeBytes(this.data);
    }
}

