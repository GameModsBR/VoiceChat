/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.audio.SoundManager
 *  net.minecraftforge.fml.common.Loader
 *  net.minecraftforge.fml.common.ModContainer
 *  net.minecraftforge.fml.common.event.FMLInitializationEvent
 *  paulscode.sound.SoundSystemConfig
 *  paulscode.sound.codecs.CodecJOrbis
 *  paulscode.sound.codecs.CodecWav
 *  paulscode.sound.libraries.LibraryLWJGLOpenAL
 */
package net.gliby.voicechat.client;

import java.util.List;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.VoiceChatClient;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import ovr.paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecJOrbis;
import paulscode.sound.codecs.CodecWav;

public class UpdatedSoundManager {
    public UpdatedSoundManager(VoiceChatClient voiceChatClient, SoundManager soundManager) {
    }

    public void init(FMLInitializationEvent event) {
        for (ModContainer mod : Loader.instance().getModList()) {
            if (!mod.getModId().equals("soundfilters")) continue;
            VoiceChat.getLogger().info("Found Sound Filters mod, won't replace OpenAL library.");
            return;
        }
        try {
            SoundSystemConfig.removeLibrary(paulscode.sound.libraries.LibraryLWJGLOpenAL.class);
            SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
            SoundSystemConfig.setCodec((String)"ogg", CodecJOrbis.class);
            SoundSystemConfig.setCodec((String)"wav", CodecWav.class);
        }
        catch (Exception e) {
            VoiceChat.getLogger().info("Failed to replaced sound libraries, you won't be hearing any voice chat.");
            e.printStackTrace();
        }
        VoiceChat.getLogger().info("Successfully replaced sound libraries.");
    }
}

