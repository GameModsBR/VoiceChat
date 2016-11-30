package net.gliby.voicechat.client;

import net.gliby.voicechat.VoiceChat;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

import java.util.Iterator;

public class UpdatedSoundManager {

    public UpdatedSoundManager(VoiceChatClient voiceChatClient, SoundManager soundManager) {
    }

    public void init(FMLInitializationEvent event) {
        Iterator e = Loader.instance().getModList().iterator();

        ModContainer mod;
        do {
            if (!e.hasNext()) {
                try {
                    SoundSystemConfig.removeLibrary(LibraryLWJGLOpenAL.class);
                    SoundSystemConfig.addLibrary(ovr.paulscode.sound.libraries.LibraryLWJGLOpenAL.class);
                    SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
                    SoundSystemConfig.setCodec("wav", CodecWav.class);
                } catch (Exception var4) {
                    VoiceChat.getLogger().info("Failed to replaced sound libraries, you won\'t be hearing any voice chat.");
                    var4.printStackTrace();
                }

                VoiceChat.getLogger().info("Successfully replaced sound libraries.");
                return;
            }

            mod = (ModContainer) e.next();
        } while (!mod.getModId().equals("soundfilters"));

        VoiceChat.getLogger().info("Found Sound Filters mod, won\'t replace OpenAL library.");
    }
}
