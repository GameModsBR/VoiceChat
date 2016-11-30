/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.fml.common.network.simpleimpl.IMessage
 */
package net.gliby.voicechat.client.networking.voiceclients;

import java.util.concurrent.ConcurrentHashMap;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.networking.voiceclients.VoiceClient;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.client.sound.SoundPreProcessor;
import net.gliby.voicechat.common.PlayerProxy;
import net.gliby.voicechat.common.networking.packets.MinecraftServerVoiceEndPacket;
import net.gliby.voicechat.common.networking.packets.MinecraftServerVoicePacket;
import net.gliby.voicechat.common.networking.voiceservers.EnumVoiceNetworkType;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class MinecraftVoiceClient
extends VoiceClient {
    private final ClientStreamManager soundManager;

    public MinecraftVoiceClient(EnumVoiceNetworkType enumVoiceServer) {
        super(enumVoiceServer);
        VoiceChat.getProxyInstance();
        this.soundManager = VoiceChatClient.getSoundManager();
    }

    @Override
    public void handleEnd(int id) {
        this.soundManager.alertEnd(id);
    }

    @Override
    public void handleEntityPosition(int entityID, double x, double y, double z) {
        PlayerProxy proxy = this.soundManager.playerData.get(entityID);
        if (proxy != null) {
            proxy.setPosition(x, y, z);
        }
    }

    @Override
    public void handlePacket(int entityID, byte[] data, int chunkSize, boolean direct) {
        this.soundManager.getSoundPreProcessor().process(entityID, data, chunkSize, direct);
    }

    @Override
    public void sendVoiceData(byte division, byte[] samples, boolean end) {
        if (end) {
            VoiceChat.getDispatcher().sendToServer((IMessage)new MinecraftServerVoiceEndPacket());
        } else {
            VoiceChat.getDispatcher().sendToServer((IMessage)new MinecraftServerVoicePacket(division, samples));
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }
}

