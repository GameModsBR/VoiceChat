package net.gliby.voicechat.client.sound.thread;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStream;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.minecraft.client.Minecraft;
import org.lwjgl.util.vector.Vector3f;

public class ThreadUpdateStream implements Runnable {

    private static final int ARBITRARY_TIMEOUT = 400;
    private final Minecraft mc;
    private final VoiceChatClient voiceChat;
    private final ClientStreamManager manager;


    public ThreadUpdateStream(ClientStreamManager manager, VoiceChatClient voiceChatClient) {
        this.manager = manager;
        this.mc = Minecraft.getMinecraft();
        this.voiceChat = voiceChatClient;
    }

    @Override
    public void run() {
        while (true) {
            if (VoiceChatClient.getSoundManager().currentStreams.isEmpty()) {
                try {
                    synchronized (this) {
                        this.wait(2L);
                    }
                } catch (InterruptedException var10) {
                    var10.printStackTrace();
                }
            } else {
                for (int e1 = 0; e1 < VoiceChatClient.getSoundManager().currentStreams.size(); ++e1) {
                    ClientStream stream = VoiceChatClient.getSoundManager().currentStreams.get(e1);
                    String source = stream.generateSource();
                    if ((stream.needsEnd || stream.getLastTimeUpdatedMS() > 400) && !this.voiceChat.sndSystem.playing(source)) {
                        this.manager.killStream(stream);
                    }

                    if (stream.dirty) {
                        this.voiceChat.sndSystem.setVolume(source, 1.0F);
                        this.voiceChat.sndSystem.setAttenuation(source, 2);
                        this.voiceChat.sndSystem.setDistOrRoll(source, (float) this.voiceChat.getSettings().getSoundDistance());
                        stream.dirty = false;
                    }

                    if (stream.direct) {
                        Vector3f vector = stream.player.position();
                        this.voiceChat.sndSystem.setPosition(source, vector.x, vector.y, vector.z);
                    } else {
                        this.voiceChat.sndSystem.setPosition(source, (float) this.mc.thePlayer.posX, (float) this.mc.thePlayer.posY, (float) this.mc.thePlayer.posZ);
                    }

                    stream.player.update(this.mc.theWorld);
                }

                try {
                    synchronized (this) {
                        this.wait(25L);
                    }
                } catch (InterruptedException var8) {
                    var8.printStackTrace();
                }
            }
        }
    }
}
