package net.gliby.voicechat.client.gui.options;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.device.Device;
import net.gliby.voicechat.client.gui.GuiBoostSlider;
import net.gliby.voicechat.client.gui.GuiCustomButton;
import net.gliby.voicechat.client.gui.GuiDropDownMenu;
import net.gliby.voicechat.client.keybindings.EnumBinding;
import net.gliby.voicechat.client.sound.MicrophoneTester;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GuiScreenOptionsWizard extends GuiScreen {

    private final VoiceChatClient voiceChat;
    private final GuiScreen parent;
    private final MicrophoneTester tester;
    private final Map<GuiButton, Integer> buttonMap = new HashMap<GuiButton, Integer>();
    private final int maxPages = 4;
    String title = "Voice Chat Setup Wizard.";
    String text = "";
    private boolean dirty;
    private String[] textBatch;
    private GuiDropDownMenu dropDown;
    private GuiCustomButton nextButton;
    private GuiCustomButton previousButton;
    private GuiCustomButton doneButton;
    private GuiCustomButton backButton;
    private GuiBoostSlider boostSlider;
    private int currentPage = 1;
    private int lastPage = -1;


    public GuiScreenOptionsWizard(VoiceChatClient voiceChat, GuiScreen parent) {
        this.voiceChat = voiceChat;
        this.parent = parent;
        this.tester = new MicrophoneTester(voiceChat);
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if ((button == this.nextButton || button == this.previousButton || this.doneButton == button || this.buttonMap.get(button) != null && this.buttonMap.get(button) == this.currentPage) && !this.dropDown.dropDownMenu) {
            switch (button.id) {
                case 0:
                    if (this.currentPage < 4) {
                        ++this.currentPage;
                    }
                    break;
                case 1:
                    if (this.currentPage >= 2) {
                        --this.currentPage;
                    }
                    break;
                case 2:
                    if (this.currentPage == 4) {
                        this.voiceChat.getSettings().setSetupNeeded(false);
                        this.mc.displayGuiScreen(null);
                    }
                    break;
                case 3:
                    this.voiceChat.getSettings().setSetupNeeded(false);
                    this.mc.displayGuiScreen(this.parent);
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
            case 1:
                this.title = "Gliby\'s Voice Chat " + I18n.format("menu.setupWizard");
                break;
            case 2:
                this.title = I18n.format("menu.selectInputDevice");
                this.dropDown.drawButton(this.mc, x, y);
                break;
            case 3:
                if (this.lastPage != this.currentPage) {
                    this.tester.start();
                }

                this.title = I18n.format("menu.adjustMicrophone");
                IndependentGUITexture.GUI_WIZARD.bindTexture(this.mc);
                GL11.glPushMatrix();
                GL11.glEnable(3042);
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(3008);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glTranslatef((float) (this.width / 2) - 39.75F, (float) (this.height / 2) - 67.5F, 0.0F);
                GL11.glScalef(2.0F, 2.0F, 0.0F);
                IndependentGUITexture.GUI_WIZARD.bindTexture(this.mc);
                this.drawTexturedModalRect(0, 0, 0, 127, 35, 20);
                float progress = this.tester.currentAmplitude;
                float procent = progress / 3.164557F;
                this.drawTexturedModalRect(3.35F, 0.0F, 35.0F, 127.0F, procent, 20.0F);
                GL11.glEnable(3008);
                GL11.glPopMatrix();
                String ratingText = I18n.format("menu.boostVoiceVolume");
                this.drawCenteredString(this.fontRendererObj, ratingText, this.width / 2, this.height / 2 - 26, -1);
                break;
            case 4:
                this.title = I18n.format("menu.finishWizard");
        }

        this.lastPage = this.currentPage;
    }

    @Override
    public void drawScreen(int x, int y, float tick) {
        this.drawDefaultBackground();
        IndependentGUITexture.GUI_WIZARD.bindTexture(this.mc);
        GL11.glPushMatrix();
        GL11.glTranslatef((float) (this.width / 2) - 142.5F, (float) (this.height / 2) - 94.5F, 0.0F);
        GL11.glScalef(1.5F, 1.5F, 0.0F);
        this.drawTexturedModalRect(0, 0, 0, 0, 190, 127);
        GL11.glPopMatrix();
        this.drawString(this.mc.fontRendererObj, this.currentPage + "/" + 4, this.width / 2 + 108, this.height / 2 + 67, -1);
        if (this.title != null) {
            this.drawString(this.mc.fontRendererObj, EnumChatFormatting.BOLD + this.title, this.width / 2 - this.mc.fontRendererObj.getStringWidth(this.title) / 2 - 12, this.height / 2 - 80, -1);
        }

        if (this.text != null) {
            this.fontRendererObj.drawSplitString(EnumChatFormatting.getTextWithoutFormattingCodes(this.text), this.width / 2 - 107 - 1 + 1, this.height / 2 - 65 + 1, 230, 0);
            this.fontRendererObj.drawSplitString(this.text, this.width / 2 - 107 - 1, this.height / 2 - 65, 230, -1);
        }

        for (int k = 0; k < this.buttonList.size(); ++k) {
            GuiButton guibutton = (GuiButton) this.buttonList.get(k);
            if (guibutton == this.nextButton || guibutton == this.previousButton || guibutton == this.doneButton || this.buttonMap.get(guibutton) != null && this.buttonMap.get(guibutton) == this.currentPage) {
                guibutton.drawButton(this.mc, x, y);
            }
        }

        this.drawPage(x, y, tick);
    }

    public void drawTexturedModalRect(float par1, float par2, float par3, float par4, float par5, float par6) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        renderer.pos((double) (par1 + 0.0F), (double) (par2 + par6), (double) this.zLevel).tex((double) ((par3 + 0.0F) * 0.00390625F), (double) ((par4 + par6) * 0.00390625F)).endVertex();
        renderer.pos((double) (par1 + par5), (double) (par2 + par6), (double) this.zLevel).tex((double) ((par3 + par5) * 0.00390625F), (double) ((par4 + par6) * 0.00390625F)).endVertex();
        renderer.pos((double) (par1 + par5), (double) (par2 + 0.0F), (double) this.zLevel).tex((double) ((par3 + par5) * 0.00390625F), (double) ((par4 + 0.0F) * 0.00390625F)).endVertex();
        renderer.pos((double) (par1 + 0.0F), (double) (par2 + 0.0F), (double) this.zLevel).tex((double) ((par3 + 0.0F) * 0.00390625F), (double) ((par4 + 0.0F) * 0.00390625F)).endVertex();
        tessellator.draw();
    }

    @Override
    public void initGui() {
        String[] array = new String[this.voiceChat.getSettings().getDeviceHandler().getDevices().size()];

        for (int i = 0; i < this.voiceChat.getSettings().getDeviceHandler().getDevices().size(); ++i) {
            array[i] = (this.voiceChat.getSettings().getDeviceHandler().getDevices().get(i)).getName();
        }

        this.dropDown = new GuiDropDownMenu(-1, this.width / 2 - 75, this.height / 2 - 55, 150, 20, this.voiceChat.getSettings().getInputDevice() != null ? this.voiceChat.getSettings().getInputDevice().getName() : "None", array);
        @SuppressWarnings("unchecked")
        List<GuiButton> buttonList = this.buttonList;
        buttonList.add(this.nextButton = new GuiCustomButton(0, this.width / 2 - 90, this.height / 2 + 60, 180, 20, I18n.format("menu.next") + " ->"));
        buttonList.add(this.previousButton = new GuiCustomButton(1, this.width / 2 - 90, this.height / 2, 180, 20, "<- " + I18n.format("menu.previous")));
        buttonList.add(this.doneButton = new GuiCustomButton(2, this.width / 2 - 90, this.height / 2, 180, 20, I18n.format("gui.done")));
        buttonList.add(this.backButton = new GuiCustomButton(3, this.width / 2 - 90, this.height / 2 + 18, 180, 20, I18n.format("gui.back")));
        buttonList.add(this.boostSlider = new GuiBoostSlider(900, this.width / 2 - 75, this.height / 2 - 15, "", I18n.format("menu.boost") + ": " + ((int) (this.voiceChat.getSettings().getInputBoost() * 5.0F) <= 0 ? I18n.format("options.off") : "" + (int) (this.voiceChat.getSettings().getInputBoost() * 5.0F) + "db"), 0.0F));
        this.boostSlider.sliderValue = this.voiceChat.getSettings().getInputBoost();
        this.doneButton.visible = false;
        this.buttonMap.put(this.backButton, 1);
        this.buttonMap.put(this.boostSlider, 3);
        this.dirty = true;
        this.textBatch = new String[]{I18n.format("menu.setupWizardPageOne", new Object[0]).replaceAll(Pattern.quote("$n"), "\n").replaceAll(Pattern.quote("$a"), this.voiceChat.keyManager.getKeyName(EnumBinding.OPEN_GUI_OPTIONS)), I18n.format("menu.setupWizardPageTwo", new Object[0]).replaceAll(Pattern.quote("$n"), "\n"), I18n.format("menu.setupWizardPageThree", new Object[0]).replaceAll(Pattern.quote("$n"), "\n"), I18n.format("menu.setupWizardPageFour", new Object[0]).replaceAll(Pattern.quote("$n"), "\n").replaceAll(Pattern.quote("$a"), this.voiceChat.keyManager.getKeyName(EnumBinding.OPEN_GUI_OPTIONS)).replaceAll(Pattern.quote("$b"), this.voiceChat.keyManager.getKeyName(EnumBinding.SPEAK))};
    }

    @Override
    public void mouseClicked(int x, int y, int b) {
        if (this.currentPage == 2) {
            if (this.dropDown.getMouseOverInteger() != -1 && this.dropDown.dropDownMenu && !this.voiceChat.getSettings().getDeviceHandler().isEmpty()) {
                Device l = this.voiceChat.getSettings().getDeviceHandler().getDevices().get(this.dropDown.getMouseOverInteger());
                if (l != null) {
                    this.voiceChat.getSettings().setInputDevice(l);
                    this.dropDown.setDisplayString(l.getName());
                }
            }

            if (this.dropDown.mousePressed(this.mc, x, y) && b == 0) {
                this.dropDown.playPressSound(this.mc.getSoundHandler());
                this.dropDown.dropDownMenu = !this.dropDown.dropDownMenu;
            }
        }

        if (b == 0) {
            for (int var8 = 0; var8 < this.buttonList.size(); ++var8) {
                GuiButton guibutton = (GuiButton) this.buttonList.get(var8);
                if ((guibutton == this.nextButton || guibutton == this.previousButton || this.doneButton == guibutton || this.buttonMap.get(guibutton) != null && this.buttonMap.get(guibutton) == this.currentPage) && guibutton.mousePressed(this.mc, x, y)) {
                    try {
                        super.mouseClicked(x, y, b);
                    } catch (IOException var7) {
                        var7.printStackTrace();
                    }
                }
            }
        }

    }

    @Override
    public void onGuiClosed() {
        if (this.tester.recording) {
            this.tester.stop();
        }

        this.voiceChat.getSettings().getConfiguration().save();
    }

    @Override
    public void updateScreen() {
        this.boostSlider.setDisplayString(I18n.format("menu.boost") + ": " + ((int) (this.voiceChat.getSettings().getInputBoost() * 5.0F) <= 0 ? I18n.format("options.off") : "" + (int) (this.voiceChat.getSettings().getInputBoost() * 5.0F) + "db"));
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
