package net.gliby.voicechat.client.device;

import javax.sound.sampled.Mixer.Info;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.sound.sampled.TargetDataLine;

import org.apache.commons.lang3.SystemUtils;

public class Device {

    private TargetDataLine line;
    private Info info;


    public Device(TargetDataLine line, Info info) {
        this.line = line;
        this.info = info;
    }

    public String getDescription() {
        return this.info.getDescription();
    }

    public String getIdentifer() {
        return this.info.getName();
    }

    public Info getInfo() {
        return this.info;
    }

    public TargetDataLine getLine() {
        return this.line;
    }

    public String getName() {
    	String name = info.getName();
    	if (SystemUtils.IS_OS_WINDOWS) {
    		try {
				name = new String(info.getName().getBytes("Windows-1252"), "Windows-1251");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
    	}
        return name != null ? name : "none";
    }

    public String getVendor() {
        return this.info.getVendor();
    }

    public String getVersion() {
        return this.info.getVersion();
    }

    public void setDevice(Device device) {
        this.line = device.line;
        this.info = device.info;
    }
}
