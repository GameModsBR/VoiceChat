/*
 * Decompiled with CFR 0_118.
 */
package net.gliby.voicechat.client.gui;

import net.gliby.voicechat.client.gui.EnumUIPlacement;

public class UIPosition {
    public int type;
    public EnumUIPlacement info;
    public float scale;
    public float x;
    public float y;

    public UIPosition(EnumUIPlacement info, float x, float y, int type, float scale) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.info = info;
        this.scale = scale;
    }
}

