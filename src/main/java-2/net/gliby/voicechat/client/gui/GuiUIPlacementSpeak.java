/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.entity.player.EnumPlayerModelParts
 *  net.minecraft.util.ResourceLocation
 *  org.lwjgl.opengl.GL11
 */
package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.client.gui.GuiPlaceableInterface;
import net.gliby.voicechat.client.gui.UIPosition;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiUIPlacementSpeak
extends GuiPlaceableInterface {
    public GuiUIPlacementSpeak(UIPosition position, int width, int height) {
        super(position, width, height);
        this.width = 56;
        this.height = 52;
    }

    @Override
    public void draw(Minecraft mc, GuiScreen gui, int x, int y, float tick) {
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glScalef((float)this.scale, (float)this.scale, (float)1.0f);
        GL11.glTranslatef((float)1.0f, (float)3.0f, (float)0.0f);
        IndependentGUITexture.TEXTURES.bindTexture(mc);
        gui.drawTexturedModalRect(0, 0, 0, 0, 54, 46);
        switch ((int)((float)(Minecraft.getSystemTime() % 1000) / 350.0f)) {
            case 0: {
                gui.drawTexturedModalRect(12, -3, 0, 47, 22, 49);
                break;
            }
            case 1: {
                gui.drawTexturedModalRect(31, -3, 23, 47, 14, 49);
                break;
            }
            case 2: {
                gui.drawTexturedModalRect(40, -3, 38, 47, 16, 49);
            }
        }
        mc.getTextureManager().bindTexture(mc.thePlayer.getLocationSkin());
        GL11.glTranslatef((float)0.0f, (float)14.0f, (float)0.0f);
        GL11.glScalef((float)2.4f, (float)2.4f, (float)0.0f);
        Gui.drawScaledCustomSizeModalRect((int)0, (int)0, (float)8.0f, (float)8.0f, (int)8, (int)8, (int)8, (int)8, (float)64.0f, (float)64.0f);
        if (mc.thePlayer != null && mc.thePlayer.func_175148_a(EnumPlayerModelParts.HAT)) {
            Gui.drawScaledCustomSizeModalRect((int)0, (int)0, (float)40.0f, (float)8.0f, (int)8, (int)8, (int)8, (int)8, (float)64.0f, (float)64.0f);
        }
    }
}

