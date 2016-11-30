/*
 * Decompiled with CFR 0_118.
 */
package net.gliby.voicechat.client.device;

import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import net.gliby.voicechat.client.device.Device;
import net.gliby.voicechat.client.sound.ClientStreamManager;

public class DeviceHandler {
    private final List<Device> devices = new ArrayList<Device>();

    public Device getDefaultDevice() {
        TargetDataLine line;
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, ClientStreamManager.getUniversalAudioFormat());
        if (!AudioSystem.isLineSupported(info)) {
            return null;
        }
        try {
            line = (TargetDataLine)AudioSystem.getLine(info);
        }
        catch (Exception ex) {
            return null;
        }
        if (line != null) {
            return this.getDeviceByLine(line);
        }
        return null;
    }

    private Device getDeviceByLine(TargetDataLine line) {
        for (int i = 0; i < this.devices.size(); ++i) {
            Device device = this.devices.get(i);
            if (!device.getLine().getLineInfo().equals(line.getLineInfo())) continue;
            return device;
        }
        return null;
    }

    public Device getDeviceByName(String deviceName) {
        for (int i = 0; i < this.devices.size(); ++i) {
            Device device = this.devices.get(i);
            if (!device.getName().equals(deviceName)) continue;
            return device;
        }
        return null;
    }

    public List<Device> getDevices() {
        return this.devices;
    }

    public boolean isEmpty() {
        return this.devices.isEmpty();
    }

    public List<Device> loadDevices() {
        Mixer.Info[] mixers;
        this.devices.clear();
        for (Mixer.Info info : mixers = AudioSystem.getMixerInfo()) {
            Mixer mixer = AudioSystem.getMixer(info);
            try {
                DataLine.Info tdlLineInfo = new DataLine.Info(TargetDataLine.class, ClientStreamManager.getUniversalAudioFormat());
                TargetDataLine tdl = (TargetDataLine)mixer.getLine(tdlLineInfo);
                if (info == null) continue;
                this.devices.add(new Device(tdl, info));
                continue;
            }
            catch (LineUnavailableException e) {
                continue;
            }
            catch (IllegalArgumentException e) {
                // empty catch block
            }
        }
        return this.devices;
    }
}

