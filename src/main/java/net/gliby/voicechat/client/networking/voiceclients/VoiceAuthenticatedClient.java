package net.gliby.voicechat.client.networking.voiceclients;

import net.gliby.voicechat.common.networking.voiceservers.EnumVoiceNetworkType;

public abstract class VoiceAuthenticatedClient extends VoiceClient {

    public final String hash;
    public boolean authed;
    private boolean connected;


    public VoiceAuthenticatedClient(EnumVoiceNetworkType enumVoiceServer, String hash) {
        super(enumVoiceServer);
        this.hash = hash;
    }

    public abstract void autheticate();

    public final String getHash() {
        return this.hash;
    }

    public boolean isAuthed() {
        return this.authed;
    }

    public void setAuthed(boolean authed) {
        this.authed = authed;
    }

    public final boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
