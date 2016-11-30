/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.entity.player.EntityPlayerMP
 */
package net.gliby.voicechat.common.networking.entityhandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.networking.ServerNetwork;
import net.gliby.voicechat.common.networking.ServerStream;
import net.gliby.voicechat.common.networking.ServerStreamManager;
import net.gliby.voicechat.common.networking.voiceservers.VoiceAuthenticatedServer;
import net.gliby.voicechat.common.networking.voiceservers.VoiceServer;
import net.minecraft.entity.player.EntityPlayerMP;

public class EntityHandler {
    private final ServerStreamManager dataManager;
    private final VoiceChatServer voiceChat;

    public EntityHandler(VoiceChatServer voiceChat) {
        this.voiceChat = voiceChat;
        this.dataManager = voiceChat.serverNetwork.dataManager;
    }

    public void connected(EntityPlayerMP speaker) {
    }

    public void disconnected(int id) {
        VoiceServer voiceServer;
        ServerStream stream = this.dataManager.streaming.get(id);
        if (stream != null) {
            this.dataManager.killStream(stream);
        }
        if ((voiceServer = this.voiceChat.getVoiceServer()) instanceof VoiceAuthenticatedServer) {
            ((VoiceAuthenticatedServer)voiceServer).closeConnection(id);
        }
    }

    public void whileSpeaking(ServerStream stream, EntityPlayerMP speaker, EntityPlayerMP receiver) {
        if (!stream.entities.contains(receiver.getEntityId())) {
            this.dataManager.giveEntity(receiver, speaker);
            stream.entities.add(receiver.getEntityId());
        }
    }
}

