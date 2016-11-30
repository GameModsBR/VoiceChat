/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.audio.SoundHandler
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.GuiButton
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.renderer.Tessellator
 *  net.minecraft.client.renderer.WorldRenderer
 *  net.minecraft.client.resources.I18n
 *  net.minecraft.util.EnumChatFormatting
 *  org.lwjgl.opengl.GL11
 */
package net.gliby.voicechat.client.gui.options;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import net.gliby.voicechat.client.Configuration;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.device.Device;
import net.gliby.voicechat.client.device.DeviceHandler;
import net.gliby.voicechat.client.gui.GuiBoostSlider;
import net.gliby.voicechat.client.gui.GuiCustomButton;
import net.gliby.voicechat.client.gui.GuiDropDownMenu;
import net.gliby.voicechat.client.keybindings.EnumBinding;
import net.gliby.voicechat.client.keybindings.KeyManager;
import net.gliby.voicechat.client.sound.MicrophoneTester;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

public class GuiScreenOptionsWizard
extends GuiScreen {
    private final VoiceChatClient voiceChat;
    private final GuiScreen parent;
    private boolean dirty;
    private String[] textBatch;
    private GuiDropDownMenu dropDown;
    private final MicrophoneTester tester;
    private GuiCustomButton nextButton;
    private GuiCustomButton previousButton;
    private GuiCustomButton doneButton;
    private GuiCustomButton backButton;
    private GuiBoostSlider boostSlider;
    private final Map<GuiButton, Integer> buttonMap = new HashMap<GuiButton, Integer>();
    private int currentPage = 1;
    private int lastPage = -1;
    private final int maxPages = 4;
    String title = "Voice Chat Setup Wizard.";
    String text = "";

    public GuiScreenOptionsWizard(VoiceChatClient voiceChat, GuiScreen parent) {
        this.voiceChat = voiceChat;
        this.parent = parent;
        this.tester = new MicrophoneTester(voiceChat);
    }

    public void actionPerformed(GuiButton button) {
        if ((button == this.nextButton || button == this.previousButton || this.doneButton == button || this.buttonMap.get((Object)button) != null && this.buttonMap.get((Object)button) == this.currentPage) && !this.dropDown.dropDownMenu) {
            switch (button.id) {
                case 0: {
                    if (this.currentPage >= 4) break;
                    ++this.currentPage;
                    break;
                }
                case 1: {
                    if (this.currentPage < 2) break;
                    --this.currentPage;
                    break;
                }
                case 2: {
                    if (this.currentPage != 4) break;
                    this.voiceChat.getSettings().setSetupNeeded(false);
                    this.mc.displayGuiScreen(null);
                    break;
                }
                case 3: {
                    this.voiceChat.getSettings().setSetupNeeded(false);
                    this.mc.displayGuiScreen(this.parent);
                }
            }
        }
    }

    public void drawPage(int x, int y, float tick) {
        if (this.tester.recording && this.currentPage != 3) {
            this.tester.stop();
        }
        if (this.currentPage != 2 && this.dropDown.dropDownMenu) {
            this.dropDown.dropDownMenu = false;
        }
        if (!this.text.equals(this.textBatch[this.currentPage - 1])) {
            this.text = this.textBatch[this.currentPage - 1];
        }
        switch (this.currentPage) {
            case 1: {
                this.title = "Gliby's Voice Chat " + I18n.format((String)"menu.setupWizard", (Object[])new Object[0]);
                break;
            }
            case 2: {
                this.title = I18n.format((String)"menu.selectInputDevice", (Object[])new Object[0]);
                this.dropDown.drawButton(this.mc, x, y);
                break;
            }
            case 3: {
                if (this.lastPage != this.currentPage) {
                    this.tester.start();
                }
                this.title = I18n.format((String)"menu.adjustMicrophone", (Object[])new Object[0]);
                IndependentGUITexture.GUI_WIZARD.bindTexture(this.mc);
                GL11.glPushMatrix();
                GL11.glEnable((int)3042);
                GL11.glEnable((int)3042);
                GL11.glBlendFunc((int)770, (int)771);
                GL11.glDisable((int)3008);
                GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                GL11.glTranslatef((float)((float)(this.width / 2) - 39.75f), (float)((float)(this.height / 2) - 67.5f), (float)0.0f);
                GL11.glScalef((float)2.0f, (float)2.0f, (float)0.0f);
                IndependentGUITexture.GUI_WIZARD.bindTexture(this.mc);
                this.drawTexturedModalRect(0, 0, 0, 127, 35, 20);
                float progress = this.tester.currentAmplitude;
                float procent = progress / 3.164557f;
                this.drawTexturedModalRect(3.35f, 0.0f, 35.0f, 127.0f, procent, 20.0f);
                GL11.glEnable((int)3008);
                GL11.glPopMatrix();
                String ratingText = I18n.format((String)"menu.boostVoiceVolume", (Object[])new Object[0]);
                this.drawCenteredString(this.fontRendererObj, ratingText, this.width / 2, this.height / 2 - 26, -1);
                break;
            }
            case 4: {
                this.title = I18n.format((String)"menu.finishWizard", (Object[])new Object[0]);
            }
        }
        this.lastPage = this.currentPage;
    }

    public void drawScreen(int x, int y, float tick) {
        this.drawDefaultBackground();
        IndependentGUITexture.GUI_WIZARD.bindTexture(this.mc);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)((float)(this.width / 2) - 142.5f), (float)((float)(this.height / 2) - 94.5f), (float)0.0f);
        GL11.glScalef((float)1.5f, (float)1.5f, (float)0.0f);
        this.drawTexturedModalRect(0, 0, 0, 0, 190, 127);
        GL11.glPopMatrix();
        this.drawString(this.mc.fontRendererObj, "" + this.currentPage + "/" + 4, this.width / 2 + 108, this.height / 2 + 67, -1);
        if (this.title != null) {
            this.drawString(this.mc.fontRendererObj, (Object)EnumChatFormatting.BOLD + this.title, this.width / 2 - this.mc.fontRendererObj.getStringWidth(this.title) / 2 - 12, this.height / 2 - 80, -1);
        }
        if (this.text != null) {
            this.fontRendererObj.drawSplitString(EnumChatFormatting.getTextWithoutFormattingCodes((String)this.text), this.width / 2 - 107 - 1 + 1, this.height / 2 - 65 + 1, 230, 0);
            this.fontRendererObj.drawSplitString(this.text, this.width / 2 - 107 - 1, this.height / 2 - 65, 230, -1);
        }
        for (int k = 0; k < this.buttonList.size(); ++k) {
            GuiButton guibutton = (GuiButton)this.buttonList.get(k);
            if (guibutton != this.nextButton && guibutton != this.previousButton && guibutton != this.doneButton && (this.buttonMap.get((Object)guibutton) == null || this.buttonMap.get((Object)guibutton) != this.currentPage)) continue;
            guibutton.drawButton(this.mc, x, y);
        }
        this.drawPage(x, y, tick);
    }

    public void drawTexturedModalRect(float par1, float par2, float par3, float par4, float par5, float par6) {
        float f = 0.00390625f;
        float f1 = 0.00390625f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.startDrawingQuads();
        renderer.addVertexWithUV((double)(par1 + 0.0f), (double)(par2 + par6), (double)this.zLevel, (double)((par3 + 0.0f) * 0.00390625f), (double)((par4 + par6) * 0.00390625f));
        renderer.addVertexWithUV((double)(par1 + par5), (double)(par2 + par6), (double)this.zLevel, (double)((par3 + par5) * 0.00390625f), (double)((par4 + par6) * 0.00390625f));
        renderer.addVertexWithUV((double)(par1 + par5), (double)(par2 + 0.0f), (double)this.zLevel, (double)((par3 + par5) * 0.00390625f), (double)((par4 + 0.0f) * 0.00390625f));
        renderer.addVertexWithUV((double)(par1 + 0.0f), (double)(par2 + 0.0f), (double)this.zLevel, (double)((par3 + 0.0f) * 0.00390625f), (double)((par4 + 0.0f) * 0.00390625f));
        tessellator.draw();
    }

    public void initGui() {
        String[] array = new String[this.voiceChat.getSettings().getDeviceHandler().getDevices().size()];
        for (int i = 0; i < this.voiceChat.getSettings().getDeviceHandler().getDevices().size(); ++i) {
            array[i] = this.voiceChat.getSettings().getDeviceHandler().getDevices().get(i).getName();
        }
        this.dropDown = new GuiDropDownMenu(-1, this.width / 2 - 75, this.height / 2 - 55, 150, 20, this.voiceChat.getSettings().getInputDevice() != null ? this.voiceChat.getSettings().getInputDevice().getName() : "None", array);
        this.nextButton = new GuiCustomButton(0, this.width / 2 - 90, this.height / 2 + 60, 180, 20, I18n.format((String)"menu.next", (Object[])new Object[0]) + " ->");
        this.buttonList.add(this.nextButton);
        this.previousButton = new GuiCustomButton(1, this.width / 2 - 90, this.height / 2, 180, 20, "<- " + I18n.format((String)"menu.previous", (Object[])new Object[0]));
        this.buttonList.add(this.previousButton);
        this.doneButton = new GuiCustomButton(2, this.width / 2 - 90, this.height / 2, 180, 20, I18n.format((String)"gui.done", (Object[])new Object[0]));
        this.buttonList.add(this.doneButton);
        this.backButton = new GuiCustomButton(3, this.width / 2 - 90, this.height / 2 + 18, 180, 20, I18n.format((String)"gui.back", (Object[])new Object[0]));
        this.buttonList.add(this.backButton);
        this.boostSlider = new GuiBoostSlider(900, this.width / 2 - 75, this.height / 2 - 15, "", I18n.format((String)"menu.boost", (Object[])new Object[0]) + ": " + ((int)(this.voiceChat.getSettings().getInputBoost() * 5.0f) <= 0 ? I18n.format((String)"options.off", (Object[])new Object[0]) : new StringBuilder().append("").append((int)(this.voiceChat.getSettings().getInputBoost() * 5.0f)).append("db").toString()), 0.0f);
        this.buttonList.add(this.boostSlider);
        this.boostSlider.sliderValue = this.voiceChat.getSettings().getInputBoost();
        this.doneButton.visible = false;
        this.buttonMap.put(this.backButton, 1);
        this.buttonMap.put(this.boostSlider, 3);
        this.dirty = true;
        this.textBatch = new String[]{I18n.format((String)"menu.setupWizardPageOne", (Object[])new Object[0]).replaceAll(Pattern.quote("$n"), "\n").replaceAll(Pattern.quote("$a"), this.voiceChat.keyManager.getKeyName(EnumBinding.OPEN_GUI_OPTIONS)), I18n.format((String)"menu.setupWizardPageTwo", (Object[])new Object[0]).replaceAll(Pattern.quote("$n"), "\n"), I18n.format((String)"menu.setupWizardPageThree", (Object[])new Object[0]).replaceAll(Pattern.quote("$n"), "\n"), I18n.format((String)"menu.setupWizardPageFour", (Object[])new Object[0]).replaceAll(Pattern.quote("$n"), "\n").replaceAll(Pattern.quote("$a"), this.voiceChat.keyManager.getKeyName(EnumBinding.OPEN_GUI_OPTIONS)).replaceAll(Pattern.quote("$b"), this.voiceChat.keyManager.getKeyName(EnumBinding.SPEAK))};
    }

    public void mouseClicked(int x, int y, int b) {
        if (this.currentPage == 2) {
            Device device;
            if (this.dropDown.getMouseOverInteger() != -1 && this.dropDown.dropDownMenu && !this.voiceChat.getSettings().getDeviceHandler().isEmpty() && (device = this.voiceChat.getSettings().getDeviceHandler().getDevices().get(this.dropDown.getMouseOverInteger())) != null) {
                this.voiceChat.getSettings().setInputDevice(device);
                this.dropDown.setDisplayString(device.getName());
            }
            if (this.dropDown.mousePressed(this.mc, x, y) && b == 0) {
                this.dropDown.playPressSound(this.mc.getSoundHandler());
                boolean bl = this.dropDown.dropDownMenu = !this.dropDown.dropDownMenu;
            }
        }
        if (b == 0) {
            for (int l = 0; l < this.buttonList.size(); ++l) {
                GuiButton guibutton = (GuiButton)this.buttonList.get(l);
                if (guibutton != this.nextButton && guibutton != this.previousButton && this.doneButton != guibutton && (this.buttonMap.get((Object)guibutton) == null || this.buttonMap.get((Object)guibutton) != this.currentPage) || !guibutton.mousePressed(this.mc, x, y)) continue;
                try {
                    super.mouseClicked(x, y, b);
                    continue;
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onGuiClosed() {
        if (this.tester.recording) {
            this.tester.stop();
        }
        this.voiceChat.getSettings().getConfiguration().save();
    }

    public void updateScreen() {
        this.boostSlider.setDisplayString(I18n.format((String)"menu.boost", (Object[])new Object[0]) + ": " + ((int)(this.voiceChat.getSettings().getInputBoost() * 5.0f) <= 0 ? I18n.format((String)"options.off", (Object[])new Object[0]) : new StringBuilder().append("").append((int)(this.voiceChat.getSettings().getInputBoost() * 5.0f)).append("db").toString()));
        this.voiceChat.getSettings().setInputBoost(this.boostSlider.sliderValue);
        if (this.lastPage != this.currentPage || this.dirty) {
            if (this.currentPage == 1) {
                this.previousButton.visible = false;
                this.doneButton.visible = false;
                this.nextButton.xPosition = this.width / 2 - 90;
                this.nextButton.yPosition = this.height / 2 + 60;
                this.nextButton.setWidth(180);
                this.nextButton.setHeight(20);
            } else if (this.currentPage == 2) {
                this.previousButton.visible = false;
                this.doneButton.visible = false;
                this.nextButton.xPosition = this.width / 2 - 90;
                this.nextButton.yPosition = this.height / 2 + 60;
                this.nextButton.setWidth(180);
                this.nextButton.setHeight(20);
            } else if (this.currentPage == 4) {
                this.nextButton.visible = false;
                this.doneButton.visible = true;
                this.doneButton.xPosition = this.width / 2;
                this.doneButton.yPosition = this.height / 2 + 60;
                this.doneButton.setWidth(95);
                this.doneButton.setHeight(20);
                this.previousButton.xPosition = this.width / 2 - 95;
                this.previousButton.yPosition = this.height / 2 + 60;
                this.previousButton.setWidth(95);
                this.previousButton.setHeight(20);
            } else {
                this.previousButton.visible = true;
                this.nextButton.visible = true;
                this.doneButton.visible = false;
                this.nextButton.xPosition = this.width / 2;
                this.nextButton.yPosition = this.height / 2 + 60;
                this.nextButton.setWidth(95);
                this.nextButton.setHeight(20);
                this.previousButton.xPosition = this.width / 2 - 95;
                this.previousButton.yPosition = this.height / 2 + 60;
                this.previousButton.setWidth(95);
                this.previousButton.setHeight(20);
            }
            this.dirty = false;
        }
    }
}

