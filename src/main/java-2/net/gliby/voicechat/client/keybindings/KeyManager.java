/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.settings.KeyBinding
 *  net.minecraftforge.fml.client.registry.ClientRegistry
 *  net.minecraftforge.fml.common.FMLCommonHandler
 *  net.minecraftforge.fml.common.eventhandler.EventBus
 *  net.minecraftforge.fml.common.eventhandler.SubscribeEvent
 *  net.minecraftforge.fml.common.gameevent.InputEvent
 *  net.minecraftforge.fml.common.gameevent.InputEvent$KeyInputEvent
 *  net.minecraftforge.fml.relauncher.Side
 *  net.minecraftforge.fml.relauncher.SideOnly
 *  org.lwjgl.input.Keyboard
 *  org.lwjgl.input.Mouse
 */
package net.gliby.voicechat.client.keybindings;

import java.util.ArrayList;
import java.util.List;
import net.gliby.voicechat.client.VoiceChatClient;
import net.gliby.voicechat.client.keybindings.EnumBinding;
import net.gliby.voicechat.client.keybindings.KeyEvent;
import net.gliby.voicechat.client.keybindings.KeyGuiOptionsEvent;
import net.gliby.voicechat.client.keybindings.KeySpeakEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@SideOnly(value=Side.CLIENT)
public class KeyManager {
    private final VoiceChatClient voiceChat;
    @SideOnly(value=Side.CLIENT)
    private final List<KeyEvent> keyEvents = new ArrayList<KeyEvent>();
    protected boolean[] keyDown;

    public KeyManager(VoiceChatClient voiceChat) {
        this.voiceChat = voiceChat;
    }

    @SideOnly(value=Side.CLIENT)
    public List<KeyEvent> getKeyEvents() {
        return this.keyEvents;
    }

    public String getKeyName(EnumBinding binding) {
        for (int i = 0; i < this.keyEvents.size(); ++i) {
            KeyEvent event = this.keyEvents.get(i);
            if (event.keyBind != binding) continue;
            return Keyboard.getKeyName((int)event.keyID);
        }
        return null;
    }

    public void init() {
        this.keyEvents.add(new KeySpeakEvent(this.voiceChat, EnumBinding.SPEAK, 47, false));
        this.keyEvents.add(new KeyGuiOptionsEvent(this.voiceChat, EnumBinding.OPEN_GUI_OPTIONS, 52, false));
        this.registerKeyBindings();
        FMLCommonHandler.instance().bus().register((Object)this);
    }

    @SubscribeEvent
    public void keyEvent(InputEvent.KeyInputEvent event) {
        for (int i = 0; i < this.keyEvents.size(); ++i) {
            KeyEvent keyEvent = this.keyEvents.get(i);
            KeyBinding keyBinding = this.keyEvents.get((int)i).forgeKeyBinding;
            int keyCode = keyBinding.getKeyCode();
            boolean state = keyCode < 0 ? Mouse.isButtonDown((int)(keyCode + 100)) : Keyboard.isKeyDown((int)keyCode);
            boolean tickEnd = true;
            if (state == this.keyDown[i] && (!state || !keyEvent.repeating)) continue;
            if (state) {
                keyEvent.keyDown(keyBinding, true, state != this.keyDown[i]);
            } else {
                keyEvent.keyUp(keyBinding, true);
            }
            this.keyDown[i] = state;
        }
    }

    private KeyBinding[] registerKeyBindings() {
        KeyBinding[] keyBinding = new KeyBinding[this.keyEvents.size()];
        for (int i = 0; i < keyBinding.length; ++i) {
            KeyEvent keyEvent = this.keyEvents.get(i);
            keyBinding[i] = new KeyBinding(keyEvent.keyBind.name, keyEvent.keyID, "key.categories.multiplayer");
            this.keyDown = new boolean[keyBinding.length];
            keyEvent.forgeKeyBinding = keyBinding[i];
            ClientRegistry.registerKeyBinding((KeyBinding)keyBinding[i]);
        }
        return keyBinding;
    }
}

