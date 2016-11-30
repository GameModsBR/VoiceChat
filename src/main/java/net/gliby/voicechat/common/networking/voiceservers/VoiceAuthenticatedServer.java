package net.gliby.voicechat.common.networking.voiceservers;

import java.util.HashMap;
import java.util.Map;

public abstract class VoiceAuthenticatedServer extends VoiceServer {

    public Map<String, net.minecraft.entity.player.EntityPlayerMP> waitingAuth = new HashMap<String, net.minecraft.entity.player.EntityPlayerMP>();


    public abstract void closeConnection(int var1);
}
