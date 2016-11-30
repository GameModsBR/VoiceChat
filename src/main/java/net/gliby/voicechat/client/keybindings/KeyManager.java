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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@SideOnly(Side.CLIENT)
public class KeyManager {

   private final VoiceChatClient voiceChat;
   @SideOnly(Side.CLIENT)
   private final List keyEvents = new ArrayList();
   protected boolean[] keyDown;


   public KeyManager(VoiceChatClient voiceChat) {
      this.voiceChat = voiceChat;
   }

   @SideOnly(Side.CLIENT)
   public List getKeyEvents() {
      return this.keyEvents;
   }

   public String getKeyName(EnumBinding binding) {
      for(int i = 0; i < this.keyEvents.size(); ++i) {
         KeyEvent event = (KeyEvent)this.keyEvents.get(i);
         if(event.keyBind == binding) {
            return Keyboard.getKeyName(event.keyID);
         }
      }

      return null;
   }

   public void init() {
      this.keyEvents.add(new KeySpeakEvent(this.voiceChat, EnumBinding.SPEAK, 47, false));
      this.keyEvents.add(new KeyGuiOptionsEvent(this.voiceChat, EnumBinding.OPEN_GUI_OPTIONS, 52, false));
      this.registerKeyBindings();
      FMLCommonHandler.instance().bus().register(this);
   }

   @SubscribeEvent
   public void keyEvent(KeyInputEvent event) {
      for(int i = 0; i < this.keyEvents.size(); ++i) {
         KeyEvent keyEvent = (KeyEvent)this.keyEvents.get(i);
         KeyBinding keyBinding = ((KeyEvent)this.keyEvents.get(i)).forgeKeyBinding;
         int keyCode = keyBinding.getKeyCode();
         boolean state = keyCode < 0?Mouse.isButtonDown(keyCode + 100):Keyboard.isKeyDown(keyCode);
         boolean tickEnd = true;
         if(state != this.keyDown[i] || state && keyEvent.repeating) {
            if(state) {
               keyEvent.keyDown(keyBinding, true, state != this.keyDown[i]);
            } else {
               keyEvent.keyUp(keyBinding, true);
            }

            this.keyDown[i] = state;
         }
      }

   }

   private KeyBinding[] registerKeyBindings() {
      KeyBinding[] keyBinding = new KeyBinding[this.keyEvents.size()];

      for(int i = 0; i < keyBinding.length; ++i) {
         KeyEvent keyEvent = (KeyEvent)this.keyEvents.get(i);
         keyBinding[i] = new KeyBinding(keyEvent.keyBind.name, keyEvent.keyID, "key.categories.multiplayer");
         this.keyDown = new boolean[keyBinding.length];
         keyEvent.forgeKeyBinding = keyBinding[i];
         ClientRegistry.registerKeyBinding(keyBinding[i]);
      }

      return keyBinding;
   }
}
