/*
 * Decompiled with CFR 0_118.
 *
 * Could not load the following classes:
 *  net.minecraft.client.audio.SoundHandler
 *  net.minecraft.client.audio.SoundManager
 *  net.minecraftforge.client.event.sound.SoundLoadEvent
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  net.minecraftforge.fml.relauncher.ReflectionHelper
 *  paulscode.sound.SoundSystem
 */
package net.gliby.voicechat.client.sound;

import net.gliby.voicechat.client.VoiceChatClient;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import paulscode.sound.SoundSystem;

import javax.sound.sampled.AudioFormat;

public class SoundSystemWrapper {
    public static volatile boolean running;
    private final SoundManager soundManager;
    public SoundSystem sndSystem;

    public SoundSystemWrapper(SoundHandler soundHandler) {
        this.soundManager = ReflectionHelper.getPrivateValue(SoundHandler.class, soundHandler, 5);
        this.sndSystem = ReflectionHelper.getPrivateValue(SoundManager.class, this.soundManager, 5);
        running = true;
        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                SoundSystemWrapper.running = false;
            }
        });
    }

    public void feedRawAudioData(String identifier, byte[] bs) {
        this.fix();
        this.sndSystem.feedRawAudioData(identifier, bs);
    }

    private void fix() {
        while (this.sndSystem.randomNumberGenerator == null && running) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            this.refresh();
            VoiceChatClient.getSoundManager().reload();
        }
    }

    private void fixThreaded() {
        if (this.sndSystem.randomNumberGenerator == null) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    SoundSystemWrapper.this.fix();
                }
            }).start();
        }
    }

    public void flush(String identifier) {
        this.fix();
        this.sndSystem.flush(identifier);
    }

    @SubscribeEvent
    public void loadEvent(SoundLoadEvent event) {
        this.fixThreaded();
    }

    public boolean playing(String string) {
        this.fix();
        return this.sndSystem.playing(string);
    }

    public void rawDataStream(AudioFormat format, boolean priority, String identifier, float x, float y, float z, int attModel, float distOrRoll) {
        this.fix();
        this.sndSystem.rawDataStream(format, priority, identifier, x, y, z, attModel, distOrRoll);
    }

    public void refresh() {
        this.sndSystem = ReflectionHelper.getPrivateValue(SoundManager.class, this.soundManager, 5);
    }

    public void setAttenuation(String generateSource, int att) {
        this.fix();
        this.sndSystem.setAttenuation(generateSource, att);
    }

    public void setDistOrRoll(String generateSource, float soundDistance) {
        this.fix();
        this.sndSystem.setDistOrRoll(generateSource, soundDistance);
    }

    public void setPitch(String identifier, float f) {
        this.fix();
        this.sndSystem.setPitch(identifier, f);
    }

    public void setPosition(String string, float x, float y, float z) {
        this.fix();
        this.sndSystem.setPosition(string, x, y, z);
    }

    public void setVelocity(String string, float motX, float motY, float motZ) {
        this.fix();
        this.sndSystem.setVelocity(string, motX, motY, motZ);
    }

    public void setVolume(String identifier, float worldVolume) {
        this.fix();
        this.sndSystem.setVolume(identifier, worldVolume);
    }

}

