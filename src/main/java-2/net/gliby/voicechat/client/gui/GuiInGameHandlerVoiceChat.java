/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EnumPlayerModelParts
 *  net.minecraft.util.ResourceLocation
 *  net.minecraftforge.client.event.RenderGameOverlayEvent
 *  net.minecraftforge.client.event.RenderGameOverlayEvent$ElementType
 *  net.minecraftforge.client.event.RenderGameOverlayEvent$Post
 *  net.minecraftforge.client.event.RenderGameOverlayEvent$Text
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  org.lwjgl.Sys
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.util.vector.Vector2f
 */
package net.gliby.voicechat.client.gui;

import java.util.ArrayList;
import java.util.List;
import net.gliby.voicechat.VoiceChat;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.debug.Statistics;
import net.gliby.voicechat.client.gui.EnumUIPlacement;
import net.gliby.voicechat.client.gui.UIPosition;
import net.gliby.voicechat.client.gui.ValueFormat;
import net.gliby.voicechat.client.gui.options.GuiScreenOptionsWizard;
import net.gliby.voicechat.client.sound.ClientStream;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.gliby.voicechat.common.MathUtility;
import net.gliby.voicechat.common.PlayerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.Sys;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class GuiInGameHandlerVoiceChat
extends Gui {
    private long lastFrame;
    private long lastFPS;
    private float fade = 0.0f;
    private final VoiceChatClient voiceChat;
    private ScaledResolution res;
    private Vector2f position;
    private final Minecraft mc;
    private UIPosition positionUI;

    public GuiInGameHandlerVoiceChat(VoiceChatClient voiceChat) {
        this.voiceChat = voiceChat;
        this.mc = Minecraft.getMinecraft();
    }

    public void calcDelta() {
        if (this.getTime() - this.lastFPS > 1000) {
            this.lastFPS += 1000;
        }
    }

    public int getDelta() {
        long time = this.getTime();
        int delta = (int)(time - this.lastFrame);
        this.lastFrame = time;
        return delta;
    }

    private Vector2f getPosition(int width, int height, UIPosition uiPositionSpeak) {
        return uiPositionSpeak.type == 0 ? new Vector2f(uiPositionSpeak.x * (float)width, uiPositionSpeak.y * (float)height) : new Vector2f(uiPositionSpeak.x, uiPositionSpeak.y);
    }

    public long getTime() {
        return Sys.getTime() * 1000 / Sys.getTimerResolution();
    }

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Text text) {
        if (text.type == RenderGameOverlayEvent.ElementType.DEBUG && VoiceChat.getProxyInstance().getSettings().isDebug()) {
            VoiceChat.getProxyInstance();
            Statistics stats = VoiceChatClient.getStatistics();
            if (stats != null) {
                int settings = 1 | ValueFormat.PRECISION(2) | 192;
                String encodedAvg = ValueFormat.format(stats.getEncodedAverageDataReceived(), settings);
                String decodedAvg = ValueFormat.format(stats.getDecodedAverageDataReceived(), settings);
                String encodedData = ValueFormat.format(stats.getEncodedDataReceived(), settings);
                String decodedData = ValueFormat.format(stats.getDecodedDataReceived(), settings);
                text.right.add("Voice Chat Debug Info");
                text.right.add("VC Data [ENC AVG]: " + encodedAvg + "");
                text.right.add("VC Data [DEC AVG]: " + decodedAvg + "");
                text.right.add("VC Data [ENC REC]: " + encodedData + "");
                text.right.add("VC Data [DEC REC]: " + decodedData + "");
            }
        }
    }

    @SubscribeEvent
    public void renderInGameGui(RenderGameOverlayEvent.Post event) {
        if (event.type == RenderGameOverlayEvent.ElementType.HOTBAR) {
            if (this.res == null) {
                this.getDelta();
                this.lastFPS = this.getTime();
                if (this.voiceChat.getSettings().isSetupNeeded()) {
                    this.mc.displayGuiScreen((GuiScreen)new GuiScreenOptionsWizard(this.voiceChat, null));
                }
            }
            this.res = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
            int width = this.res.getScaledWidth();
            int height = this.res.getScaledHeight();
            int delta = this.getDelta();
            this.calcDelta();
            this.fade = !VoiceChat.getProxyInstance().isRecorderActive() ? (this.fade > 0.0f ? (this.fade -= 0.01f * (float)delta) : 0.0f) : (this.fade < 1.0f && VoiceChat.getProxyInstance().isRecorderActive() ? (this.fade += 0.01f * (float)delta) : 1.0f);
            if (this.fade != 0.0f) {
                this.positionUI = this.voiceChat.getSettings().getUIPositionSpeak();
                this.position = this.getPosition(width, height, this.positionUI);
                if (this.positionUI.scale != 0.0f) {
                    GL11.glPushMatrix();
                    GL11.glEnable((int)3042);
                    GL11.glBlendFunc((int)770, (int)771);
                    GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)(this.fade * this.voiceChat.getSettings().getUIOpacity()));
                    IndependentGUITexture.TEXTURES.bindTexture(this.mc);
                    GL11.glTranslatef((float)(this.position.x + (float)this.positionUI.info.offsetX), (float)(this.position.y + (float)this.positionUI.info.offsetY), (float)0.0f);
                    GL11.glScalef((float)this.positionUI.scale, (float)this.positionUI.scale, (float)1.0f);
                    this.drawTexturedModalRect(0, 0, 0, 0, 54, 46);
                    switch ((int)((float)(Minecraft.getSystemTime() % 1000) / 350.0f)) {
                        case 0: {
                            this.drawTexturedModalRect(12, -3, 0, 47, 22, 49);
                            break;
                        }
                        case 1: {
                            this.drawTexturedModalRect(31, -3, 23, 47, 14, 49);
                            break;
                        }
                        case 2: {
                            this.drawTexturedModalRect(40, -3, 38, 47, 16, 49);
                        }
                    }
                    this.mc.getTextureManager().bindTexture(this.mc.thePlayer.getLocationSkin());
                    GL11.glTranslatef((float)0.0f, (float)14.0f, (float)0.0f);
                    GL11.glScalef((float)2.4f, (float)2.4f, (float)0.0f);
                    Gui.drawScaledCustomSizeModalRect((int)0, (int)0, (float)8.0f, (float)8.0f, (int)8, (int)8, (int)8, (int)8, (float)64.0f, (float)64.0f);
                    if (this.mc.thePlayer != null && this.mc.thePlayer.func_175148_a(EnumPlayerModelParts.HAT)) {
                        Gui.drawScaledCustomSizeModalRect((int)0, (int)0, (float)40.0f, (float)8.0f, (int)8, (int)8, (int)8, (int)8, (float)64.0f, (float)64.0f);
                    }
                    GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                    GL11.glDisable((int)3042);
                    GL11.glPopMatrix();
                }
            }
            if (!VoiceChatClient.getSoundManager().currentStreams.isEmpty() && this.voiceChat.getSettings().isVoicePlateAllowed()) {
                float scale = 0.0f;
                this.positionUI = this.voiceChat.getSettings().getUIPositionPlate();
                this.position = this.getPosition(width, height, this.positionUI);
                GL11.glEnable((int)3042);
                GL11.glBlendFunc((int)770, (int)771);
                for (int i = 0; i < VoiceChatClient.getSoundManager().currentStreams.size(); ++i) {
                    ClientStream stream = VoiceChatClient.getSoundManager().currentStreams.get(i);
                    if (stream == null) continue;
                    String s = stream.player.entityName();
                    boolean playerExists = stream.player.getPlayer() != null;
                    int length = this.mc.fontRendererObj.getStringWidth(s);
                    scale = 0.75f * this.positionUI.scale;
                    GL11.glPushMatrix();
                    GL11.glTranslatef((float)(this.position.x + (float)this.positionUI.info.offsetX), (float)(this.position.y + (float)this.positionUI.info.offsetY + (float)(i * 23) * scale), (float)0.0f);
                    GL11.glScalef((float)scale, (float)scale, (float)0.0f);
                    GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)this.voiceChat.getSettings().getUIOpacity());
                    GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.0f);
                    IndependentGUITexture.TEXTURES.bindTexture(this.mc);
                    this.drawTexturedModalRect(0, 0, 56, stream.special * 22, 109, 22);
                    GL11.glPushMatrix();
                    scale = MathUtility.clamp(50.5f / (float)length, 0.0f, 1.25f);
                    GL11.glTranslatef((float)(25.0f + scale / 2.0f), (float)(11.0f - (float)(this.mc.fontRendererObj.FONT_HEIGHT - 1) * scale / 2.0f), (float)0.0f);
                    GL11.glScalef((float)scale, (float)scale, (float)0.0f);
                    this.drawString(this.mc.fontRendererObj, s, 0, 0, -1);
                    GL11.glPopMatrix();
                    GL11.glPushMatrix();
                    if (playerExists) {
                        IndependentGUITexture.bindPlayer(this.mc, stream.player.getPlayer());
                    } else {
                        IndependentGUITexture.bindDefaultPlayer(this.mc);
                    }
                    GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)this.voiceChat.getSettings().getUIOpacity());
                    GL11.glTranslatef((float)3.25f, (float)3.25f, (float)0.0f);
                    GL11.glScalef((float)2.0f, (float)2.0f, (float)0.0f);
                    Gui.drawScaledCustomSizeModalRect((int)0, (int)0, (float)8.0f, (float)8.0f, (int)8, (int)8, (int)8, (int)8, (float)64.0f, (float)64.0f);
                    if (this.mc.thePlayer != null && this.mc.thePlayer.func_175148_a(EnumPlayerModelParts.HAT)) {
                        Gui.drawScaledCustomSizeModalRect((int)0, (int)0, (float)40.0f, (float)8.0f, (int)8, (int)8, (int)8, (int)8, (float)64.0f, (float)64.0f);
                    }
                    GL11.glPopMatrix();
                    GL11.glPopMatrix();
                }
                GL11.glDisable((int)3042);
            }
            if (VoiceChatClient.getSoundManager().currentStreams.isEmpty()) {
                VoiceChatClient.getSoundManager().volumeControlStop();
            } else if (this.voiceChat.getSettings().isVolumeControlled()) {
                VoiceChatClient.getSoundManager().volumeControlStart();
            }
        }
    }
}

