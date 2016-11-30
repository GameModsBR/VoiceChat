/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.multiplayer.WorldClient
 *  org.lwjgl.util.vector.Vector3f
 */
package net.gliby.voicechat.client.sound.thread;

import java.util.List;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStream;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.client.sound.SoundSystemWrapper;
import net.gliby.voicechat.common.PlayerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import org.lwjgl.util.vector.Vector3f;

public class ThreadUpdateStream
implements Runnable {
    private static final int ARBITRARY_TIMEOUT = 400;
    private final Minecraft mc;
    private final VoiceChatClient voiceChat;
    private final ClientStreamManager manager;

    public ThreadUpdateStream(ClientStreamManager manager, VoiceChatClient voiceChatClient) {
        this.manager = manager;
        this.mc = Minecraft.getMinecraft();
        this.voiceChat = voiceChatClient;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        do {
            if (!VoiceChatClient.getSoundManager().currentStreams.isEmpty()) {
                for (int i2 = 0; i2 < VoiceChatClient.getSoundManager().currentStreams.size(); ++i2) {
                    ClientStream stream = VoiceChatClient.getSoundManager().currentStreams.get(i2);
                    String source = stream.generateSource();
                    if ((stream.needsEnd || stream.getLastTimeUpdatedMS() > 400) && !this.voiceChat.sndSystem.playing(source)) {
                        this.manager.killStream(stream);
                    }
                    if (stream.dirty) {
                        this.voiceChat.sndSystem.setVolume(source, 1.0f);
                        this.voiceChat.sndSystem.setAttenuation(source, 2);
                        this.voiceChat.sndSystem.setDistOrRoll(source, this.voiceChat.getSettings().getSoundDistance());
                        stream.dirty = false;
                    }
                    if (stream.direct) {
                        Vector3f vector = stream.player.position();
                        this.voiceChat.sndSystem.setPosition(source, vector.x, vector.y, vector.z);
                    } else {
                        this.voiceChat.sndSystem.setPosition(source, (float)this.mc.thePlayer.posX, (float)this.mc.thePlayer.posY, (float)this.mc.thePlayer.posZ);
                    }
                    stream.player.update(this.mc.theWorld);
                }
                try {
                    ThreadUpdateStream i2 = this;
                    synchronized (i2) {
                        this.wait(25);
                    }
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            try {
                ThreadUpdateStream e = this;
                synchronized (e) {
                    this.wait(2);
                    continue;
                }
            }
            catch (InterruptedException e1) {
                e1.printStackTrace();
                continue;
            }
            break;
        } while (true);
    }
}

