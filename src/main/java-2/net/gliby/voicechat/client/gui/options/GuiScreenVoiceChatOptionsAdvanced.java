/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.GuiButton
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.resources.I18n
 *  org.lwjgl.opengl.GL11
 */
package net.gliby.voicechat.client.gui.options;

import java.util.List;
import net.gliby.voicechat.client.Configuration;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.gui.GuiBoostSlider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

public class GuiScreenVoiceChatOptionsAdvanced
extends GuiScreen {
    private final VoiceChatClient voiceChat;
    private GuiButton encodingMode;
    private GuiButton enhancedDecoding;
    private GuiButton serverConnection;
    private GuiButton volumeControlButton;
    private GuiBoostSlider qualitySlider;
    private final GuiScreen parent;

    public GuiScreenVoiceChatOptionsAdvanced(VoiceChatClient voiceChat, GuiScreen parent) {
        this.voiceChat = voiceChat;
        this.parent = parent;
    }

    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: {
                this.voiceChat.getSettings().getConfiguration().save();
                this.mc.displayGuiScreen(this.parent);
                break;
            }
            case 1: {
                this.resetAdvancedOptions();
                break;
            }
            case 5: {
                int mode = this.voiceChat.getSettings().getEncodingMode();
                mode = mode < 2 ? ++mode : 0;
                this.voiceChat.getSettings().setEncodingMode(mode);
                this.encodingMode.displayString = I18n.format((String)"menu.encodingMode", (Object[])new Object[0]) + ": " + this.voiceChat.getSettings().getEncodingModeString();
                break;
            }
            case 6: {
                this.voiceChat.getSettings().setPerceptualEnchantment(!this.voiceChat.getSettings().isPerceptualEnchantmentAllowed());
                this.enhancedDecoding.displayString = I18n.format((String)"menu.enhancedDecoding", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().isPerceptualEnchantmentAllowed() ? I18n.format((String)"options.on", (Object[])new Object[0]) : I18n.format((String)"options.off", (Object[])new Object[0]));
                break;
            }
            case 7: {
                this.voiceChat.getSettings().setSnooperAllowed(false);
                this.serverConnection.displayString = I18n.format((String)"menu.allowSnooper", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().isSnooperAllowed() ? I18n.format((String)"options.on", (Object[])new Object[0]) : I18n.format((String)"options.off", (Object[])new Object[0]));
                break;
            }
            case 8: {
                this.voiceChat.getSettings().setVolumeControl(!this.voiceChat.getSettings().isVolumeControlled());
                this.volumeControlButton.displayString = I18n.format((String)"menu.volumeControl", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().isVolumeControlled() ? I18n.format((String)"options.on", (Object[])new Object[0]) : I18n.format((String)"options.off", (Object[])new Object[0]));
                VoiceChatClient.getSoundManager().volumeControlStop();
            }
        }
    }

    public void drawScreen(int x, int y, float time) {
        this.drawDefaultBackground();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)(this.width / 2) - (float)(this.fontRendererObj.getStringWidth("Gliby's Voice Chat Options") / 2) * 1.5f), (float)0.0f, (float)0.0f);
        GL11.glScalef((float)1.5f, (float)1.5f, (float)0.0f);
        this.drawString(this.mc.fontRendererObj, "Gliby's Voice Chat Options", 0, 6, -1);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.width / 2 - this.fontRendererObj.getStringWidth(I18n.format((String)"menu.advancedOptions", (Object[])new Object[0])) / 2), (float)12.0f, (float)0.0f);
        this.drawString(this.mc.fontRendererObj, I18n.format((String)"menu.advancedOptions", (Object[])new Object[0]), 0, 12, -1);
        GL11.glPopMatrix();
        if ((int)(this.voiceChat.getSettings().getEncodingQuality() * 10.0f) <= 2) {
            this.drawCenteredString(this.mc.fontRendererObj, I18n.format((String)"menu.encodingMessage", (Object[])new Object[0]), this.width / 2, this.height - 50, -255);
        }
        super.drawScreen(x, y, time);
    }

    public void initGui() {
        this.buttonList.add(new GuiButton(0, this.width / 2 - 75, this.height - 34, 150, 20, I18n.format((String)"gui.back", (Object[])new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 + 77, this.height - 34, 75, 20, I18n.format((String)"controls.reset", (Object[])new Object[0])));
        this.qualitySlider = new GuiBoostSlider(4, this.width / 2 + 2, 74, "", I18n.format((String)"menu.encodingQuality", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().getEncodingQuality() == 0.0f ? "0" : String.valueOf((int)(this.voiceChat.getSettings().getEncodingQuality() * 10.0f))), 0.0f);
        this.qualitySlider.sliderValue = this.voiceChat.getSettings().getEncodingQuality();
        this.encodingMode = new GuiButton(5, this.width / 2 - 152, 98, 150, 20, I18n.format((String)"menu.encodingMode", (Object[])new Object[0]) + ": " + this.voiceChat.getSettings().getEncodingModeString());
        this.enhancedDecoding = new GuiButton(6, this.width / 2 - 152, 50, 150, 20, I18n.format((String)"menu.enhancedDecoding", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().isPerceptualEnchantmentAllowed() ? I18n.format((String)"options.on", (Object[])new Object[0]) : I18n.format((String)"options.off", (Object[])new Object[0])));
        this.buttonList.add(this.enhancedDecoding);
        this.serverConnection = new GuiButton(7, this.width / 2 + 2, 50, 150, 20, I18n.format((String)"menu.allowSnooper", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().isSnooperAllowed() ? I18n.format((String)"options.on", (Object[])new Object[0]) : I18n.format((String)"options.off", (Object[])new Object[0])));
        this.buttonList.add(this.serverConnection);
        this.volumeControlButton = new GuiButton(8, this.width / 2 - 152, 74, 150, 20, I18n.format((String)"menu.volumeControl", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().isVolumeControlled() ? I18n.format((String)"options.on", (Object[])new Object[0]) : I18n.format((String)"options.off", (Object[])new Object[0])));
        this.buttonList.add(this.volumeControlButton);
        this.buttonList.add(this.qualitySlider);
        this.buttonList.add(this.encodingMode);
        this.serverConnection.enabled = false;
        this.encodingMode.enabled = false;
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        this.voiceChat.getSettings().getConfiguration().save();
    }

    public void resetAdvancedOptions() {
        this.qualitySlider.sliderValue = 0.6f;
        this.voiceChat.getSettings().setEncodingQuality(this.qualitySlider.sliderValue);
        this.qualitySlider.displayString = this.qualitySlider.idValue = I18n.format((String)"menu.encodingQuality", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().getEncodingQuality() == 0.0f ? "0" : String.valueOf((int)(this.voiceChat.getSettings().getEncodingQuality() * 10.0f)));
        this.voiceChat.getSettings().setEncodingMode(1);
        this.encodingMode.displayString = I18n.format((String)"menu.encodingMode", (Object[])new Object[0]) + ": " + this.voiceChat.getSettings().getEncodingModeString();
        this.voiceChat.getSettings().setPerceptualEnchantment(true);
        this.enhancedDecoding.displayString = I18n.format((String)"menu.enhancedDecoding", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().isPerceptualEnchantmentAllowed() ? I18n.format((String)"options.on", (Object[])new Object[0]) : I18n.format((String)"options.off", (Object[])new Object[0]));
        this.voiceChat.getSettings().setSnooperAllowed(false);
        this.serverConnection.displayString = I18n.format((String)"menu.allowSnooper", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().isSnooperAllowed() ? I18n.format((String)"options.on", (Object[])new Object[0]) : I18n.format((String)"options.off", (Object[])new Object[0]));
        this.voiceChat.getSettings().setVolumeControl(true);
        this.volumeControlButton.displayString = I18n.format((String)"menu.volumeControl", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().isVolumeControlled() ? I18n.format((String)"options.on", (Object[])new Object[0]) : I18n.format((String)"options.off", (Object[])new Object[0]));
    }

    public void updateScreen() {
        this.voiceChat.getSettings().setEncodingQuality(this.qualitySlider.sliderValue);
        this.qualitySlider.setDisplayString(I18n.format((String)"menu.encodingQuality", (Object[])new Object[0]) + ": " + (this.voiceChat.getSettings().getEncodingQuality() == 0.0f ? "0" : String.valueOf((int)(this.voiceChat.getSettings().getEncodingQuality() * 10.0f))));
    }
}

