/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.renderer.OpenGlHelper
 *  net.minecraft.client.renderer.entity.RenderManager
 *  net.minecraft.client.settings.GameSettings
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.EntityLivingBase
 *  net.minecraft.entity.player.EnumPlayerModelParts
 *  net.minecraftforge.client.event.RenderWorldLastEvent
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  org.lwjgl.opengl.GL11
 */
package net.gliby.voicechat.client.render;

import java.util.List;
import net.gliby.voicechat.client.Settings;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.sound.ClientStream;
import net.gliby.voicechat.client.sound.ClientStreamManager;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.gliby.voicechat.common.MathUtility;
import net.gliby.voicechat.common.PlayerProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class RenderPlayerVoiceIcon
extends Gui {
    private final VoiceChatClient voiceChat;
    private final Minecraft mc;

    public RenderPlayerVoiceIcon(VoiceChatClient voiceChat, Minecraft mc) {
        this.voiceChat = voiceChat;
        this.mc = mc;
    }

    private void enableEntityLighting(Entity entity, float partialTicks) {
        int i1 = entity.getBrightnessForRender(partialTicks);
        if (entity.isBurning()) {
            i1 = 15728880;
        }
        int j = i1 % 65536;
        int k = i1 / 65536;
        OpenGlHelper.setLightmapTextureCoords((int)OpenGlHelper.lightmapTexUnit, (float)((float)j / 1.0f), (float)((float)k / 1.0f));
        OpenGlHelper.setActiveTexture((int)OpenGlHelper.lightmapTexUnit);
        GL11.glEnable((int)3553);
        OpenGlHelper.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
    }

    public void disableEntityLighting() {
        OpenGlHelper.setActiveTexture((int)OpenGlHelper.lightmapTexUnit);
        GL11.glDisable((int)3553);
        OpenGlHelper.setActiveTexture((int)OpenGlHelper.defaultTexUnit);
    }

    @SubscribeEvent
    public void render(RenderWorldLastEvent event) {
        if (!VoiceChatClient.getSoundManager().currentStreams.isEmpty() && this.voiceChat.getSettings().isVoiceIconAllowed()) {
            GL11.glDisable((int)2929);
            GL11.glEnable((int)3042);
            OpenGlHelper.glBlendFunc((int)770, (int)771, (int)1, (int)0);
            this.translateWorld(this.mc, event.partialTicks);
            int i = 0;
            while ((float)i < MathUtility.clamp(VoiceChatClient.getSoundManager().currentStreams.size(), 0.0f, this.voiceChat.getSettings().getMaximumRenderableVoiceIcons())) {
                EntityLivingBase entity;
                ClientStream stream = VoiceChatClient.getSoundManager().currentStreams.get(i);
                if (stream.player.getPlayer() != null && stream.player.usesEntity && !(entity = (EntityLivingBase)stream.player.getPlayer()).isInvisible() && !this.mc.gameSettings.hideGUI) {
                    GL11.glPushMatrix();
                    this.enableEntityLighting((Entity)entity, event.partialTicks);
                    GL11.glNormal3f((float)0.0f, (float)1.0f, (float)0.0f);
                    GL11.glDepthMask((boolean)false);
                    this.translateEntity((Entity)entity, event.partialTicks);
                    GL11.glRotatef((float)(- Minecraft.getMinecraft().getRenderManager().playerViewY), (float)0.0f, (float)1.0f, (float)0.0f);
                    GL11.glTranslatef((float)-0.25f, (float)(entity.height + 0.7f), (float)0.0f);
                    GL11.glRotatef((float)Minecraft.getMinecraft().getRenderManager().playerViewX, (float)1.0f, (float)0.0f, (float)0.0f);
                    GL11.glScalef((float)0.015f, (float)0.015f, (float)1.0f);
                    IndependentGUITexture.TEXTURES.bindTexture(this.mc);
                    GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)0.25f);
                    if (!entity.isSneaking()) {
                        this.renderIcon();
                    }
                    GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
                    GL11.glEnable((int)2929);
                    GL11.glDepthMask((boolean)true);
                    this.renderIcon();
                    IndependentGUITexture.bindPlayer(this.mc, (Entity)entity);
                    GL11.glPushMatrix();
                    GL11.glTranslatef((float)20.0f, (float)30.0f, (float)0.0f);
                    GL11.glScalef((float)-1.0f, (float)-1.0f, (float)-1.0f);
                    GL11.glScalef((float)2.0f, (float)2.0f, (float)0.0f);
                    Gui.drawScaledCustomSizeModalRect((int)0, (int)0, (float)8.0f, (float)8.0f, (int)8, (int)8, (int)8, (int)8, (float)64.0f, (float)64.0f);
                    if (this.mc.thePlayer != null && this.mc.thePlayer.func_175148_a(EnumPlayerModelParts.HAT)) {
                        Gui.drawScaledCustomSizeModalRect((int)0, (int)0, (float)40.0f, (float)8.0f, (int)8, (int)8, (int)8, (int)8, (float)64.0f, (float)64.0f);
                    }
                    GL11.glPopMatrix();
                    this.disableEntityLighting();
                    GL11.glPopMatrix();
                }
                ++i;
            }
            GL11.glDisable((int)3042);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        }
    }

    private void renderIcon() {
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
    }

    public void translateEntity(Entity entity, float tick) {
        GL11.glTranslated((double)(entity.prevPosX + (entity.posX - entity.prevPosX) * (double)tick), (double)(entity.prevPosY + (entity.posY - entity.prevPosY) * (double)tick), (double)(entity.prevPosZ + (entity.posZ - entity.prevPosZ) * (double)tick));
    }

    public void translateWorld(Minecraft mc, float tick) {
        GL11.glTranslated((double)(- mc.thePlayer.prevPosX + (mc.thePlayer.posX - mc.thePlayer.prevPosX) * (double)tick), (double)(- mc.thePlayer.prevPosY + (mc.thePlayer.posY - mc.thePlayer.prevPosY) * (double)tick), (double)(- mc.thePlayer.prevPosZ + (mc.thePlayer.posZ - mc.thePlayer.prevPosZ) * (double)tick));
    }
}

