package net.gliby.voicechat.common.networking.voiceservers;

import java.util.HashMap;
import java.util.Map;

public abstract class VoiceAuthenticatedServer extends VoiceServer {

   public Map waitingAuth = new HashMap();


   public abstract void closeConnection(int var1);
}
