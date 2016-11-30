/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.GuiButton
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.resources.I18n
 *  net.minecraft.server.integrated.IntegratedServer
 *  net.minecraft.util.EnumChatFormatting
 *  net.minecraftforge.fml.common.ModMetadata
 *  org.lwjgl.opengl.GL11
 */
package net.gliby.voicechat.client.gui.options;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import net.gliby.gman.ModInfo;
import net.gliby.voicechat.client.Configuration;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.device.Device;
import net.gliby.voicechat.client.device.DeviceHandler;
import net.gliby.voicechat.client.gui.GuiBoostSlider;
import net.gliby.voicechat.client.gui.GuiCustomButton;
import net.gliby.voicechat.client.gui.GuiDropDownMenu;
import net.gliby.voicechat.client.gui.GuiScreenDonate;
import net.gliby.voicechat.client.gui.GuiScreenLocalMute;
import net.gliby.voicechat.client.gui.options.GuiScreenOptionsUI;
import net.gliby.voicechat.client.gui.options.GuiScreenOptionsWizard;
import net.gliby.voicechat.client.gui.options.GuiScreenVoiceChatOptionsAdvanced;
import net.gliby.voicechat.client.networking.ClientNetwork;
import net.gliby.voicechat.client.sound.MicrophoneTester;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.ModMetadata;
import org.lwjgl.opengl.GL11;

public class GuiScreenVoiceChatOptions
extends GuiScreen {
    private final VoiceChatClient voiceChat;
    private final MicrophoneTester tester;
    private GuiCustomButton advancedOptions;
    private GuiCustomButton mutePlayer;
    private GuiBoostSlider boostSlider;
    private GuiBoostSlider voiceVolume;
    private GuiDropDownMenu dropDown;
    private GuiButton UIPosition;
    private GuiButton microphoneMode;
    private List<String> warningMessages;
    private String updateMessage;

    public GuiScreenVoiceChatOptions(VoiceChatClient voiceChat) {
        this.voiceChat = voiceChat;
        this.tester = new MicrophoneTester(voiceChat);
    }

    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 899: {
                if (this.dropDown.dropDownMenu) break;
                this.mc.displayGuiScreen((GuiScreen)new GuiScreenVoiceChatOptionsAdvanced(this.voiceChat, this));
                break;
            }
            case 898: {
                if (this.dropDown.dropDownMenu) break;
                this.mc.displayGuiScreen((GuiScreen)new GuiScreenOptionsWizard(this.voiceChat, this));
                break;
            }
            case 0: {
                if (!(button instanceof GuiDropDownMenu) || this.voiceChat.getSettings().getDeviceHandler().isEmpty()) break;
                ((GuiDropDownMenu)button).dropDownMenu = !((GuiDropDownMenu)button).dropDownMenu;
                break;
            }
            case 1: {
                this.voiceChat.getSettings().getConfiguration().save();
                this.mc.displayGuiScreen((GuiScreen)new GuiScreenDonate(this.voiceChat.getModInfo(), VoiceChatClient.getModMetadata(), this));
                break;
            }
            case 2: {
                if (!this.tester.recording) {
                    this.tester.start();
                } else {
                    this.tester.stop();
                }
                button.displayString = this.tester.recording ? I18n.format((String)"menu.microphoneStopTest", (Object[])new Object[0]) : I18n.format((String)"menu.microphoneTest", (Object[])new Object[0]);
                break;
            }
            case 3: {
                this.voiceChat.getSettings().getConfiguration().save();
                this.mc.displayGuiScreen(null);
                break;
            }
            case 4: {
                this.mc.displayGuiScreen((GuiScreen)new GuiScreenOptionsUI(this.voiceChat, this));
                break;
            }
            case 897: {
                if (this.dropDown.dropDownMenu) break;
                this.mc.displayGuiScreen((GuiScreen)new GuiScreenLocalMute(this, this.voiceChat));
                break;
            }
            case 5: {
                if (!this.dropDown.dropDownMenu) {
                    this.microphoneMode.visible = true;
                    this.microphoneMode.enabled = true;
                    this.voiceChat.getSettings().setSpeakMode(this.voiceChat.getSettings().getSpeakMode() == 0 ? 1 : 0);
                    this.microphoneMode.displayString = I18n.format((String)"menu.speakMode", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().getSpeakMode() == 0 ? I18n.format((String)"menu.speakModePushToTalk", (Object[])new Object[0]) : I18n.format((String)"menu.speakModeToggleToTalk", (Object[])new Object[0]));
                    break;
                }
                if (!this.voiceChat.getSettings().getDeviceHandler().isEmpty()) break;
                this.microphoneMode.visible = false;
                this.microphoneMode.enabled = false;
            }
        }
    }

    public void drawScreen(int x, int y, float tick) {
        this.drawDefaultBackground();
        GL11.glPushMatrix();
        float scale = 1.5f;
        GL11.glTranslatef((float)((float)(this.width / 2) - (float)(this.fontRendererObj.getStringWidth("Gliby's Voice Chat Options") / 2) * 1.5f), (float)0.0f, (float)0.0f);
        GL11.glScalef((float)1.5f, (float)1.5f, (float)0.0f);
        this.drawString(this.fontRendererObj, "Gliby's Voice Chat Options", 0, 6, -1);
        GL11.glPopMatrix();
        for (int i = 0; i < this.warningMessages.size(); ++i) {
            int warnY = i * this.fontRendererObj.FONT_HEIGHT + this.height / 2 + 66 - this.fontRendererObj.FONT_HEIGHT * this.warningMessages.size() / 2;
            this.drawCenteredString(this.fontRendererObj, this.warningMessages.get(i), this.width / 2, warnY, -1);
        }
        super.drawScreen(x, y, tick);
    }

    public boolean inBounds(int x, int y, int posX, int posY, int width, int height) {
        return x >= posX && y >= posY && x < posX + width && y < posY + height;
    }

    public void initGui() {
        String[] array = new String[this.voiceChat.getSettings().getDeviceHandler().getDevices().size()];
        for (int i = 0; i < this.voiceChat.getSettings().getDeviceHandler().getDevices().size(); ++i) {
            array[i] = this.voiceChat.getSettings().getDeviceHandler().getDevices().get(i).getName();
        }
        int heightOffset = 55;
        this.dropDown = new GuiDropDownMenu(0, this.width / 2 - 152, this.height / 2 - 55, 150, 20, this.voiceChat.getSettings().getInputDevice() != null ? this.voiceChat.getSettings().getInputDevice().getName() : "None", array);
        this.microphoneMode = new GuiButton(5, this.width / 2 - 152, this.height / 2 + 25 - 55, 150, 20, I18n.format((String)"menu.speakMode", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().getSpeakMode() == 0 ? I18n.format((String)"menu.speakModePushToTalk", (Object[])new Object[0]) : I18n.format((String)"menu.speakModeToggleToTalk", (Object[])new Object[0])));
        this.UIPosition = new GuiButton(4, this.width / 2 + 2, this.height / 2 + 25 - 55, 150, 20, I18n.format((String)"menu.uiOptions", (Object[])new Object[0]));
        this.voiceVolume = new GuiBoostSlider(910, this.width / 2 + 2, this.height / 2 - 25 - 55, "", I18n.format((String)"menu.worldVolume", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().getWorldVolume() == 0.0f ? I18n.format((String)"options.off", (Object[])new Object[0]) : new StringBuilder().append(String.valueOf((int)(this.voiceChat.getSettings().getWorldVolume() * 100.0f))).append("%").toString()), 0.0f);
        this.voiceVolume.sliderValue = this.voiceChat.getSettings().getWorldVolume();
        this.boostSlider = new GuiBoostSlider(900, this.width / 2 + 2, this.height / 2 - 55, "", I18n.format((String)"menu.boost", (Object[])new Object[0]) + ": " + ((int)(this.voiceChat.getSettings().getInputBoost() * 5.0f) <= 0 ? I18n.format((String)"options.off", (Object[])new Object[0]) : new StringBuilder().append("").append((int)(this.voiceChat.getSettings().getInputBoost() * 5.0f)).append("db").toString()), 0.0f);
        this.boostSlider.sliderValue = this.voiceChat.getSettings().getInputBoost();
        this.advancedOptions = new GuiCustomButton(899, this.width / 2 + 2, this.height / 2 + 49 - 55, 150, 20, I18n.format((String)"menu.advancedOptions", (Object[])new Object[0]));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 151, this.height - 34, 75, 20, I18n.format((String)"menu.gman.supportGliby", (Object[])new Object[0])));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 152, this.height / 2 - 25 - 55, 150, 20, !this.tester.recording ? I18n.format((String)"menu.microphoneTest", (Object[])new Object[0]) : I18n.format((String)"menu.microphoneStopTest", (Object[])new Object[0])));
        this.buttonList.add(new GuiButton(3, this.width / 2 - 75, this.height - 34, 150, 20, I18n.format((String)"menu.returnToGame", (Object[])new Object[0])));
        this.buttonList.add(this.advancedOptions);
        this.buttonList.add(new GuiCustomButton(898, this.width / 2 - 152, this.height / 2 + 49 - 55, 150, 20, I18n.format((String)"menu.openOptionsWizard", (Object[])new Object[0])));
        this.buttonList.add(this.UIPosition);
        this.buttonList.add(this.microphoneMode);
        this.buttonList.add(this.boostSlider);
        this.buttonList.add(this.voiceVolume);
        this.mutePlayer = new GuiCustomButton(897, this.width / 2 - 152, this.height / 2 + 73 - 55, 304, 20, I18n.format((String)"menu.mutePlayers", (Object[])new Object[0]));
        this.buttonList.add(this.mutePlayer);
        this.buttonList.add(this.dropDown);
        if (this.voiceChat.getSettings().getDeviceHandler().isEmpty()) {
            ((GuiButton)this.buttonList.get((int)0)).enabled = false;
            ((GuiButton)this.buttonList.get((int)3)).enabled = false;
            this.boostSlider.enabled = false;
            this.mutePlayer.enabled = false;
            this.microphoneMode.enabled = false;
            this.mutePlayer.enabled = false;
        }
        super.initGui();
        this.warningMessages = new ArrayList<String>();
        if (this.voiceChat.getSettings().getDeviceHandler().isEmpty()) {
            this.warningMessages.add((Object)EnumChatFormatting.DARK_RED + "No input devices found, add input device and restart Minecraft.");
        }
        if (this.voiceChat.getModInfo().updateNeeded()) {
            this.updateMessage = I18n.format((String)"menu.downloadLatest", (Object[])new Object[0]) + "\u00a7b " + this.voiceChat.getModInfo().updateURL;
            this.warningMessages.add(this.updateMessage);
            this.warningMessages.add((Object)EnumChatFormatting.RED + I18n.format((String)"menu.modOutdated", (Object[])new Object[0]));
        }
        if (this.mc.isSingleplayer() && !this.mc.getIntegratedServer().getPublic()) {
            this.warningMessages.add((Object)EnumChatFormatting.RED + I18n.format((String)"menu.warningSingleplayer", (Object[])new Object[0]));
        }
        if (!this.voiceChat.getClientNetwork().isConnected() && !this.mc.isSingleplayer()) {
            this.warningMessages.add((Object)EnumChatFormatting.RED + I18n.format((String)"Server doesn't support voice chat.", (Object[])new Object[0]));
        }
    }

    public void keyTyped(char c, int key) {
        if (key == 1) {
            this.voiceChat.getSettings().getConfiguration().save();
            this.mc.displayGuiScreen((GuiScreen)null);
            this.mc.setIngameFocus();
        }
    }

    public void mouseClicked(int x, int y, int b) {
        if (b == 0) {
            if (!this.voiceChat.getModInfo().updateNeeded()) {
                // empty if block
            }
            for (int i = 0; i < this.warningMessages.size(); ++i) {
                String s = this.warningMessages.get(i);
                if (!s.equals(this.updateMessage)) continue;
                int warnY = i * this.fontRendererObj.FONT_HEIGHT + this.height / 2 + 66 - this.fontRendererObj.FONT_HEIGHT * this.warningMessages.size() / 2;
                int length = this.fontRendererObj.getStringWidth(s);
                if (!this.inBounds(x, y, this.width / 2 - length / 2, warnY, length, this.fontRendererObj.FONT_HEIGHT)) continue;
                this.openURL(this.voiceChat.modInfo.updateURL);
            }
            if (this.dropDown.getMouseOverInteger() != -1 && this.dropDown.dropDownMenu && !this.voiceChat.getSettings().getDeviceHandler().isEmpty()) {
                Device device = this.voiceChat.getSettings().getDeviceHandler().getDevices().get(this.dropDown.getMouseOverInteger());
                if (device == null) {
                    return;
                }
                this.voiceChat.getSettings().setInputDevice(device);
                this.dropDown.setDisplayString(device.getName());
            }
        }
        try {
            super.mouseClicked(x, y, b);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onGuiClosed() {
        if (this.tester.recording) {
            this.tester.stop();
        }
    }

    private void openURL(String par1URI) {
        try {
            Class oclass = Class.forName("java.awt.Desktop");
            Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null, new Object[0]);
            oclass.getMethod("browse", URI.class).invoke(object, new URI(par1URI));
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public void updateScreen() {
        this.voiceChat.getSettings().setWorldVolume(this.voiceVolume.sliderValue);
        this.voiceChat.getSettings().setInputBoost(this.boostSlider.sliderValue);
        this.voiceVolume.setDisplayString(I18n.format((String)"menu.worldVolume", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().getWorldVolume() == 0.0f ? I18n.format((String)"options.off", (Object[])new Object[0]) : new StringBuilder().append((int)(this.voiceChat.getSettings().getWorldVolume() * 100.0f)).append("%").toString()));
        this.boostSlider.setDisplayString(I18n.format((String)"menu.boost", (Object[])new Object[0]) + ": " + ((int)(this.voiceChat.getSettings().getInputBoost() * 5.0f) <= 0 ? I18n.format((String)"options.off", (Object[])new Object[0]) : new StringBuilder().append((int)(this.voiceChat.getSettings().getInputBoost() * 5.0f)).append("db").toString()));
        this.advancedOptions.allowed = !this.dropDown.dropDownMenu;
        this.mutePlayer.allowed = !this.dropDown.dropDownMenu;
    }
}

