/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  net.minecraftforge.fml.common.gameevent.InputEvent
 *  net.minecraftforge.fml.common.gameevent.InputEvent$KeyInputEvent
 *  net.minecraftforge.fml.common.gameevent.TickEvent
 *  net.minecraftforge.fml.common.gameevent.TickEvent$Type
 *  net.minecraftforge.fml.relauncher.Side
 */
package net.gliby.voicechat.client.keybindings;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.keybindings.KeyManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

public class KeyTickHandler {
    VoiceChatClient voiceChat;

    public KeyTickHandler(VoiceChatClient voiceChat) {
        this.voiceChat = voiceChat;
    }

    @SubscribeEvent
    public void tick(TickEvent event) {
        if (event.type == TickEvent.Type.PLAYER && event.side == Side.CLIENT) {
            this.voiceChat.keyManager.keyEvent(null);
        }
    }
}

