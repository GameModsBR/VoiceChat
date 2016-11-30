/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.settings.KeyBinding
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 */
package net.gliby.voicechat.client.keybindings;

import net.gliby.voicechat.client.keybindings.EnumBinding;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(value=Side.CLIENT)
public abstract class KeyEvent {
    public KeyBinding forgeKeyBinding;
    public EnumBinding keyBind;
    public int keyID = -1;
    public boolean repeating;

    public KeyEvent(EnumBinding keyBind, int keyID, boolean repeating) {
        this.keyBind = keyBind;
        this.keyID = keyID;
        this.repeating = repeating;
    }

    public abstract void keyDown(KeyBinding var1, boolean var2, boolean var3);

    public abstract void keyUp(KeyBinding var1, boolean var2);
}

