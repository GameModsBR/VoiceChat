/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.fml.common.eventhandler.EventBus
 */
package net.gliby.voicechat.common.api;

import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.common.VoiceChatServer;
import net.gliby.voicechat.common.networking.ServerStreamHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;

public class VoiceChatAPI {
    private static VoiceChatAPI instance;
    private ServerStreamHandler handler;
    private EventBus eventBus;

    public static VoiceChatAPI instance() {
        return instance;
    }

    public EventBus bus() {
        return this.eventBus;
    }

    public void init() {
        instance = this;
        this.eventBus = new EventBus();
        this.handler = new ServerStreamHandler(VoiceChat.getServerInstance());
        this.bus().register((Object)this.handler);
    }

    public void setCustomStreamHandler(Object eventHandler) {
        this.eventBus = null;
        this.eventBus = new EventBus();
        this.eventBus.register(eventHandler);
    }
}

