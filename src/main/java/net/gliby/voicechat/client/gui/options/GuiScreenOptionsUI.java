package net.gliby.voicechat.client.gui.options;

import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.gui.GuiBoostSlider;
import net.gliby.voicechat.client.gui.GuiUIPlacement;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

public class GuiScreenOptionsUI extends GuiScreen {

   private final VoiceChatClient voiceChat;
   private final GuiScreen parent;
   private GuiBoostSlider opacity;


   public GuiScreenOptionsUI(VoiceChatClient voiceChat, GuiScreen parent) {
      this.voiceChat = voiceChat;
      this.parent = parent;
   }

   public void actionPerformed(GuiButton button) {
      switch(button.id) {
      case 0:
         this.mc.displayGuiScreen(this.parent);
         break;
      case 1:
         this.voiceChat.getSettings().resetUI(this.width, this.height);
         this.opacity.sliderValue = 1.0F;
         break;
      case 2:
         this.mc.displayGuiScreen(new GuiUIPlacement(this));
      }

   }

   public void drawScreen(int x, int y, float time) {
      this.drawDefaultBackground();
      GL11.glPushMatrix();
      GL11.glTranslatef((float)(this.width / 2) - (float)(this.fontRendererObj.getStringWidth("Gliby\'s Voice Chat Options") / 2) * 1.5F, 0.0F, 0.0F);
      GL11.glScalef(1.5F, 1.5F, 0.0F);
      this.drawString(this.mc.fontRendererObj, "Gliby\'s Voice Chat Options", 0, 6, -1);
      GL11.glPopMatrix();
      GL11.glPushMatrix();
      GL11.glTranslatef((float)(this.width / 2 - this.fontRendererObj.getStringWidth(I18n.format("menu.uiOptions", new Object[0])) / 2), 12.0F, 0.0F);
      this.drawString(this.mc.fontRendererObj, I18n.format("menu.uiOptions", new Object[0]), 0, 12, -1);
      GL11.glPopMatrix();
      super.drawScreen(x, y, time);
   }

   public void initGui() {
      this.buttonList.add(new GuiButton(0, this.width / 2 - 75, this.height - 34, 150, 20, I18n.format("gui.back", new Object[0])));
      this.buttonList.add(new GuiButton(1, this.width / 2 - 75, 73, 150, 20, I18n.format("menu.resetAll", new Object[0])));
      this.buttonList.add(new GuiButton(2, this.width / 2 - 150, 50, 150, 20, I18n.format("menu.uiPlacement", new Object[0])));
      this.buttonList.add(this.opacity = new GuiBoostSlider(-1, this.width / 2 + 2, 50, "", I18n.format("menu.uiOpacity", new Object[0]) + ": " + (this.voiceChat.getSettings().getUIOpacity() == 0.0F?I18n.format("options.off", new Object[0]):(int)(this.voiceChat.getSettings().getUIOpacity() * 100.0F) + "%"), 0.0F));
      this.opacity.sliderValue = this.voiceChat.getSettings().getUIOpacity();
   }

   public void onGuiClosed() {
      super.onGuiClosed();
      this.voiceChat.getSettings().getConfiguration().save();
   }

   public void updateScreen() {
      super.onGuiClosed();
      this.voiceChat.getSettings().setUIOpacity(this.opacity.sliderValue);
      this.opacity.setDisplayString(I18n.format("menu.uiOpacity", new Object[0]) + ": " + (this.voiceChat.getSettings().getUIOpacity() == 0.0F?I18n.format("options.off", new Object[0]):(int)(this.voiceChat.getSettings().getUIOpacity() * 100.0F) + "%"));
   }
}
