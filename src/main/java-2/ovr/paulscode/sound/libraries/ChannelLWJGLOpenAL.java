/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.openal.AL10
 *  paulscode.sound.Channel
 *  paulscode.sound.SoundBuffer
 *  paulscode.sound.Source
 */
package ovr.paulscode.sound.libraries;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import ovr.paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.Channel;
import paulscode.sound.SoundBuffer;
import paulscode.sound.Source;

public class ChannelLWJGLOpenAL
extends Channel {
    public IntBuffer ALSource;
    public int ALformat;
    public int sampleRate;
    public float millisPreviouslyPlayed = 0.0f;

    public ChannelLWJGLOpenAL(int type, IntBuffer src) {
        super(type);
        this.libraryType = LibraryLWJGLOpenAL.class;
        this.ALSource = src;
    }

    public boolean attachBuffer(IntBuffer buf) {
        if (this.errorCheck(this.channelType != 0, "Sound buffers may only be attached to normal sources.")) {
            return false;
        }
        AL10.alSourcei((int)this.ALSource.get(0), (int)4105, (int)buf.get(0));
        if (this.attachedSource != null && this.attachedSource.soundBuffer != null && this.attachedSource.soundBuffer.audioFormat != null) {
            this.setAudioFormat(this.attachedSource.soundBuffer.audioFormat);
        }
        return this.checkALError();
    }

    public int buffersProcessed() {
        if (this.channelType != 1) {
            return 0;
        }
        int processed = AL10.alGetSourcei((int)this.ALSource.get(0), (int)4118);
        if (this.checkALError()) {
            return 0;
        }
        return processed;
    }

    private boolean checkALError() {
        switch (AL10.alGetError()) {
            case 0: {
                return false;
            }
            case 40961: {
                this.errorMessage("Invalid name parameter.");
                return true;
            }
            case 40962: {
                this.errorMessage("Invalid parameter.");
                return true;
            }
            case 40963: {
                return false;
            }
            case 40964: {
                this.errorMessage("Illegal call.");
                return true;
            }
            case 40965: {
                this.errorMessage("Unable to allocate memory.");
                return true;
            }
        }
        this.errorMessage("An unrecognized error occurred.");
        return true;
    }

    public void cleanup() {
        if (this.ALSource != null) {
            try {
                AL10.alSourceStop((IntBuffer)this.ALSource);
                AL10.alGetError();
            }
            catch (Exception e) {
                // empty catch block
            }
            try {
                AL10.alDeleteSources((IntBuffer)this.ALSource);
                AL10.alGetError();
            }
            catch (Exception e) {
                // empty catch block
            }
            this.ALSource.clear();
        }
        this.ALSource = null;
        super.cleanup();
    }

    public void close() {
        try {
            AL10.alSourceStop((int)this.ALSource.get(0));
            AL10.alGetError();
        }
        catch (Exception e) {
            // empty catch block
        }
        if (this.channelType == 1) {
            this.flush();
        }
    }

    public int feedRawAudioData(byte[] buffer) {
        IntBuffer intBuffer;
        if (this.errorCheck(this.channelType != 1, "Raw audio data can only be fed to streaming sources.")) {
            return -1;
        }
        ByteBuffer byteBuffer = (ByteBuffer)BufferUtils.createByteBuffer((int)buffer.length).put(buffer).flip();
        int processed = AL10.alGetSourcei((int)this.ALSource.get(0), (int)4118);
        if (processed > 0) {
            intBuffer = BufferUtils.createIntBuffer((int)processed);
            AL10.alGenBuffers((IntBuffer)intBuffer);
            if (this.errorCheck(this.checkALError(), "Error clearing stream buffers in method 'feedRawAudioData'")) {
                return -1;
            }
            AL10.alSourceUnqueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
            if (this.errorCheck(this.checkALError(), "Error unqueuing stream buffers in method 'feedRawAudioData'")) {
                return -1;
            }
            intBuffer.rewind();
            while (intBuffer.hasRemaining()) {
                int i = intBuffer.get();
                if (AL10.alIsBuffer((int)i)) {
                    this.millisPreviouslyPlayed += this.millisInBuffer(i);
                }
                this.checkALError();
            }
            AL10.alDeleteBuffers((IntBuffer)intBuffer);
            this.checkALError();
        }
        intBuffer = BufferUtils.createIntBuffer((int)1);
        AL10.alGenBuffers((IntBuffer)intBuffer);
        if (this.errorCheck(this.checkALError(), "Error generating stream buffers in method 'preLoadBuffers'")) {
            return -1;
        }
        AL10.alBufferData((int)intBuffer.get(0), (int)this.ALformat, (ByteBuffer)byteBuffer, (int)this.sampleRate);
        if (this.checkALError()) {
            return -1;
        }
        AL10.alSourceQueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
        if (this.checkALError()) {
            return -1;
        }
        if (this.attachedSource != null && this.attachedSource.channel == this && this.attachedSource.active() && !this.playing()) {
            AL10.alSourcePlay((int)this.ALSource.get(0));
            this.checkALError();
        }
        return processed;
    }

    public void flush() {
        IntBuffer intBuffer = BufferUtils.createIntBuffer((int)1);
        for (int queued = AL10.alGetSourcei((int)this.ALSource.get((int)0), (int)4117); queued > 0; --queued) {
            try {
                AL10.alSourceUnqueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
            }
            catch (Exception e) {
                return;
            }
            if (!this.checkALError()) continue;
            return;
        }
        this.millisPreviouslyPlayed = 0.0f;
    }

    public float millisecondsPlayed() {
        float offset = AL10.alGetSourcei((int)this.ALSource.get(0), (int)4134);
        float bytesPerFrame = 1.0f;
        switch (this.ALformat) {
            case 4352: {
                bytesPerFrame = 1.0f;
                break;
            }
            case 4353: {
                bytesPerFrame = 2.0f;
                break;
            }
            case 4354: {
                bytesPerFrame = 2.0f;
                break;
            }
            case 4355: {
                bytesPerFrame = 4.0f;
                break;
            }
        }
        offset = offset / bytesPerFrame / (float)this.sampleRate * 1000.0f;
        if (this.channelType == 1) {
            offset += this.millisPreviouslyPlayed;
        }
        return offset;
    }

    public float millisInBuffer(int alBufferi) {
        return (float)AL10.alGetBufferi((int)alBufferi, (int)8196) / (float)AL10.alGetBufferi((int)alBufferi, (int)8195) / ((float)AL10.alGetBufferi((int)alBufferi, (int)8194) / 8.0f) / (float)this.sampleRate * 1000.0f;
    }

    public void pause() {
        AL10.alSourcePause((int)this.ALSource.get(0));
        this.checkALError();
    }

    public void play() {
        AL10.alSourcePlay((int)this.ALSource.get(0));
        this.checkALError();
    }

    public boolean playing() {
        int state = AL10.alGetSourcei((int)this.ALSource.get(0), (int)4112);
        if (this.checkALError()) {
            return false;
        }
        return state == 4114;
    }

    public boolean preLoadBuffers(LinkedList<byte[]> bufferList) {
        int processed;
        IntBuffer streamBuffers;
        if (this.errorCheck(this.channelType != 1, "Buffers may only be queued for streaming sources.")) {
            return false;
        }
        if (this.errorCheck(bufferList == null, "Buffer List null in method 'preLoadBuffers'")) {
            return false;
        }
        boolean playing = this.playing();
        if (playing) {
            AL10.alSourceStop((int)this.ALSource.get(0));
            this.checkALError();
        }
        if ((processed = AL10.alGetSourcei((int)this.ALSource.get(0), (int)4118)) > 0) {
            streamBuffers = BufferUtils.createIntBuffer((int)processed);
            AL10.alGenBuffers((IntBuffer)streamBuffers);
            if (this.errorCheck(this.checkALError(), "Error clearing stream buffers in method 'preLoadBuffers'")) {
                return false;
            }
            AL10.alSourceUnqueueBuffers((int)this.ALSource.get(0), (IntBuffer)streamBuffers);
            if (this.errorCheck(this.checkALError(), "Error unqueuing stream buffers in method 'preLoadBuffers'")) {
                return false;
            }
        }
        if (playing) {
            AL10.alSourcePlay((int)this.ALSource.get(0));
            this.checkALError();
        }
        streamBuffers = BufferUtils.createIntBuffer((int)bufferList.size());
        AL10.alGenBuffers((IntBuffer)streamBuffers);
        if (this.errorCheck(this.checkALError(), "Error generating stream buffers in method 'preLoadBuffers'")) {
            return false;
        }
        ByteBuffer byteBuffer = null;
        for (int i = 0; i < bufferList.size(); ++i) {
            byteBuffer = (ByteBuffer)BufferUtils.createByteBuffer((int)bufferList.get(i).length).put(bufferList.get(i)).flip();
            try {
                AL10.alBufferData((int)streamBuffers.get(i), (int)this.ALformat, (ByteBuffer)byteBuffer, (int)this.sampleRate);
            }
            catch (Exception e) {
                this.errorMessage("Error creating buffers in method 'preLoadBuffers'");
                this.printStackTrace(e);
                return false;
            }
            if (!this.errorCheck(this.checkALError(), "Error creating buffers in method 'preLoadBuffers'")) continue;
            return false;
        }
        try {
            AL10.alSourceQueueBuffers((int)this.ALSource.get(0), (IntBuffer)streamBuffers);
        }
        catch (Exception e) {
            this.errorMessage("Error queuing buffers in method 'preLoadBuffers'");
            this.printStackTrace(e);
            return false;
        }
        if (this.errorCheck(this.checkALError(), "Error queuing buffers in method 'preLoadBuffers'")) {
            return false;
        }
        AL10.alSourcePlay((int)this.ALSource.get(0));
        if (this.errorCheck(this.checkALError(), "Error playing source in method 'preLoadBuffers'")) {
            return false;
        }
        return true;
    }

    public boolean queueBuffer(byte[] buffer) {
        if (this.errorCheck(this.channelType != 1, "Buffers may only be queued for streaming sources.")) {
            return false;
        }
        ByteBuffer byteBuffer = (ByteBuffer)BufferUtils.createByteBuffer((int)buffer.length).put(buffer).flip();
        IntBuffer intBuffer = BufferUtils.createIntBuffer((int)1);
        AL10.alSourceUnqueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
        if (this.checkALError()) {
            return false;
        }
        if (AL10.alIsBuffer((int)intBuffer.get(0))) {
            this.millisPreviouslyPlayed += this.millisInBuffer(intBuffer.get(0));
        }
        this.checkALError();
        AL10.alBufferData((int)intBuffer.get(0), (int)this.ALformat, (ByteBuffer)byteBuffer, (int)this.sampleRate);
        if (this.checkALError()) {
            return false;
        }
        AL10.alSourceQueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
        if (this.checkALError()) {
            return false;
        }
        return true;
    }

    public void rewind() {
        if (this.channelType == 1) {
            return;
        }
        AL10.alSourceRewind((int)this.ALSource.get(0));
        if (!this.checkALError()) {
            this.millisPreviouslyPlayed = 0.0f;
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    public void setAudioFormat(AudioFormat audioFormat) {
        int soundFormat = 0;
        if (audioFormat.getChannels() == 1) {
            if (audioFormat.getSampleSizeInBits() == 8) {
                soundFormat = 4352;
            } else {
                if (audioFormat.getSampleSizeInBits() != 16) {
                    this.errorMessage("Illegal sample size in method 'setAudioFormat'");
                    return;
                }
                soundFormat = 4353;
            }
        } else {
            if (audioFormat.getChannels() != 2) {
                this.errorMessage("Audio data neither mono nor stereo in method 'setAudioFormat'");
                return;
            }
            if (audioFormat.getSampleSizeInBits() == 8) {
                soundFormat = 4354;
            } else {
                if (audioFormat.getSampleSizeInBits() != 16) {
                    this.errorMessage("Illegal sample size in method 'setAudioFormat'");
                    return;
                }
                soundFormat = 4355;
            }
        }
        this.ALformat = soundFormat;
        this.sampleRate = (int)audioFormat.getSampleRate();
    }

    public void setFormat(int format, int rate) {
        this.ALformat = format;
        this.sampleRate = rate;
    }

    public void stop() {
        AL10.alSourceStop((int)this.ALSource.get(0));
        if (!this.checkALError()) {
            this.millisPreviouslyPlayed = 0.0f;
        }
    }
}

