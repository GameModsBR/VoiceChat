/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiScreen
 */
package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.client.gui.EnumUIPlacement;
import net.gliby.voicechat.client.gui.UIPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public abstract class GuiPlaceableInterface {
    public int screenWidth;
    public int screenHeight;
    public UIPosition positionUI;
    float x;
    float y;
    int positionType = 0;
    int width;
    int height;
    float scale = 1.0f;
    EnumUIPlacement info;

    public GuiPlaceableInterface(UIPosition position, int width, int height) {
        this.positionUI = position;
        this.info = position.info;
        this.x = position.type == 0 ? position.x * (float)width : position.x;
        this.y = position.type == 0 ? position.y * (float)height : position.y;
        this.positionType = position.type;
        this.screenWidth = width;
        this.screenHeight = height;
        this.scale = position.scale;
    }

    public abstract void draw(Minecraft var1, GuiScreen var2, int var3, int var4, float var5);

    public void update(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.screenWidth = width;
        this.screenHeight = height;
    }
}

