package net.gliby.voicechat.common.networking.packets;

import io.netty.buffer.ByteBuf;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.networking.MinecraftPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MinecraftServerVoiceEndPacket extends MinecraftPacket implements IMessageHandler<MinecraftServerVoiceEndPacket, IMessage> {

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public IMessage onMessage(MinecraftServerVoiceEndPacket packet, MessageContext ctx) {
        VoiceChat.getServerInstance().getVoiceServer().handleVoiceData(ctx.getServerHandler().player, null, (byte) 0, ctx.getServerHandler().player.getEntityId(), true);
        return null;
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }
}
