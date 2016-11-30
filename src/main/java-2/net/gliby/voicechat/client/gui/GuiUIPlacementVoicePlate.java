/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.Gui
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.entity.player.EnumPlayerModelParts
 *  net.minecraft.util.ResourceLocation
 *  org.lwjgl.opengl.GL11
 */
package net.gliby.voicechat.client.gui;

import java.util.Random;
import net.gliby.voicechat.client.gui.EnumUIPlacement;
import net.gliby.voicechat.client.gui.GuiPlaceableInterface;
import net.gliby.voicechat.client.gui.UIPosition;
import net.gliby.voicechat.client.textures.IndependentGUITexture;
import net.gliby.voicechat.common.MathUtility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiUIPlacementVoicePlate
extends GuiPlaceableInterface {
    private static final ResourceLocation field_110826_a = new ResourceLocation("textures/entity/steve.png");
    String[] players;

    public GuiUIPlacementVoicePlate(UIPosition position, int width, int height) {
        super(position, width, height);
        this.width = 70;
        this.height = 55;
        this.players = new String[]{"krisis78", "theGliby", "3kliksphilip"};
        this.shuffleArray(this.players);
    }

    @Override
    public void draw(Minecraft mc, GuiScreen gui, int x, int y, float tick) {
        for (int i = 0; i < this.players.length; ++i) {
            String stream = this.players[i];
            int length = mc.fontRendererObj.getStringWidth(stream);
            float scale = 0.75f * this.scale;
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(this.positionUI.x + (float)this.positionUI.info.offsetX), (float)(this.positionUI.y + (float)this.positionUI.info.offsetY + (float)(i * 23) * scale), (float)0.0f);
            GL11.glScalef((float)scale, (float)scale, (float)0.0f);
            GL11.glEnable((int)3042);
            GL11.glBlendFunc((int)770, (int)771);
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            GL11.glTranslatef((float)0.0f, (float)0.0f, (float)0.0f);
            IndependentGUITexture.TEXTURES.bindTexture(mc);
            gui.drawTexturedModalRect(0, 0, 56, 0, 109, 22);
            GL11.glPushMatrix();
            scale = MathUtility.clamp(50.5f / (float)length, 0.0f, 1.25f);
            GL11.glTranslatef((float)(25.0f + scale / 2.0f), (float)(11.0f - (float)(mc.fontRendererObj.FONT_HEIGHT - 1) * scale / 2.0f), (float)0.0f);
            GL11.glScalef((float)scale, (float)scale, (float)0.0f);
            gui.drawString(mc.fontRendererObj, stream, 0, 0, -1);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            mc.getTextureManager().bindTexture(field_110826_a);
            GL11.glTranslatef((float)3.25f, (float)3.25f, (float)0.0f);
            GL11.glScalef((float)2.0f, (float)2.0f, (float)0.0f);
            Gui.drawScaledCustomSizeModalRect((int)0, (int)0, (float)8.0f, (float)8.0f, (int)8, (int)8, (int)8, (int)8, (float)64.0f, (float)64.0f);
            if (mc.thePlayer != null && mc.thePlayer.func_175148_a(EnumPlayerModelParts.HAT)) {
                Gui.drawScaledCustomSizeModalRect((int)0, (int)0, (float)40.0f, (float)8.0f, (int)8, (int)8, (int)8, (int)8, (float)64.0f, (float)64.0f);
            }
            GL11.glPopMatrix();
            GL11.glDisable((int)3042);
            GL11.glPopMatrix();
        }
    }

    void shuffleArray(String[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; --i) {
            int index = rnd.nextInt(i + 1);
            String a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }
}

