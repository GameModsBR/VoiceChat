/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.FontRenderer
 *  net.minecraft.client.gui.GuiButton
 *  net.minecraft.client.gui.GuiScreen
 *  net.minecraft.client.renderer.Tessellator
 *  net.minecraft.client.renderer.WorldRenderer
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.texture.TextureManager
 *  net.minecraft.client.resources.I18n
 *  net.minecraft.client.resources.IResourcePack
 *  net.minecraft.util.ResourceLocation
 *  net.minecraftforge.fml.client.FMLClientHandler
 *  net.minecraftforge.fml.common.ModMetadata
 *  org.lwjgl.opengl.GL11
 */
package net.gliby.voicechat.client.gui;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import javax.imageio.ImageIO;
import net.gliby.gman.ModInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ModMetadata;
import org.lwjgl.opengl.GL11;

public class GuiScreenDonate
extends GuiScreen {
    private final GuiScreen parent;
    private final ModInfo info;
    private final ModMetadata modMetadata;
    private ResourceLocation cachedLogo;
    private Dimension cachedLogoDimensions;

    public GuiScreenDonate(ModInfo info, ModMetadata modMetadata, GuiScreen parent) {
        this.parent = parent;
        this.info = info;
        this.modMetadata = modMetadata;
    }

    public void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: {
                this.mc.displayGuiScreen(this.parent);
                break;
            }
            case 1: {
                this.openURL(this.info.donateURL);
            }
        }
    }

    public void drawScreen(int x, int y, float tick) {
        this.renderModLogo(this.info.modId, this.modMetadata, false);
        this.drawBackground(0);
        GL11.glPushMatrix();
        GL11.glTranslatef((float)(this.width / 2 - this.cachedLogoDimensions.width / 2), (float)(this.height / 2 - (this.cachedLogoDimensions.height + 60)), (float)0.0f);
        this.renderModLogo(this.info.modId, this.modMetadata, true);
        GL11.glPopMatrix();
        String s = I18n.format((String)"menu.gman.supportGliby.description", (Object[])new Object[0]);
        this.fontRendererObj.drawSplitString(s, this.width / 2 - 150, this.height / 2 - 50, 300, -1);
        String s1 = I18n.format((String)"menu.gman.supportGliby.contact", (Object[])new Object[0]);
        this.fontRendererObj.drawSplitString(s1, this.width / 2 - 150, this.height / 2 + 35, 300, -1);
        super.drawScreen(x, y, tick);
    }

    public void initGui() {
        this.buttonList.add(new GuiButton(0, this.width / 2 - 50, this.height - 34, 100, 20, I18n.format((String)"gui.back", (Object[])new Object[0])));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 100, this.height / 2, I18n.format((String)"menu.gman.donate", (Object[])new Object[0])));
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

    private void renderModLogo(String modId, ModMetadata modMetadata, boolean draw) {
        String logoFile = modMetadata.logoFile;
        if (!logoFile.isEmpty()) {
            GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            TextureManager tm = this.mc.getTextureManager();
            IResourcePack pack = FMLClientHandler.instance().getResourcePackFor(modId);
            try {
                if (this.cachedLogo == null) {
                    BufferedImage logo = null;
                    if (pack != null) {
                        logo = pack.getPackImage();
                    } else {
                        InputStream logoResource = this.getClass().getResourceAsStream(logoFile);
                        if (logoResource != null) {
                            logo = ImageIO.read(logoResource);
                        }
                    }
                    if (logo != null) {
                        this.cachedLogo = tm.getDynamicTextureLocation("modlogo", new DynamicTexture(logo));
                        this.cachedLogoDimensions = new Dimension(logo.getWidth(), logo.getHeight());
                    }
                }
                if (this.cachedLogo != null && draw) {
                    this.mc.renderEngine.bindTexture(this.cachedLogo);
                    double scaleX = (double)this.cachedLogoDimensions.width / 200.0;
                    double scaleY = (double)this.cachedLogoDimensions.height / 65.0;
                    double scale = 1.0;
                    if (scaleX > 1.0 || scaleY > 1.0) {
                        scale = 1.0 / Math.max(scaleX, scaleY);
                    }
                    this.cachedLogoDimensions.width = (int)((double)this.cachedLogoDimensions.width * scale);
                    this.cachedLogoDimensions.height = (int)((double)this.cachedLogoDimensions.height * scale);
                    Tessellator tessellator = Tessellator.getInstance();
                    WorldRenderer renderer = tessellator.getWorldRenderer();
                    renderer.startDrawingQuads();
                    renderer.addVertexWithUV(0.0, (double)this.cachedLogoDimensions.height, (double)this.zLevel, 0.0, 1.0);
                    renderer.addVertexWithUV((double)(0 + this.cachedLogoDimensions.width), (double)(0 + this.cachedLogoDimensions.height), (double)this.zLevel, 1.0, 1.0);
                    renderer.addVertexWithUV((double)(0 + this.cachedLogoDimensions.width), 0.0, (double)this.zLevel, 1.0, 0.0);
                    renderer.addVertexWithUV(0.0, 0.0, (double)this.zLevel, 0.0, 0.0);
                    tessellator.draw();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

