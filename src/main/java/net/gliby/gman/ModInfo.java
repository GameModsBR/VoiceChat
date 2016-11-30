package net.gliby.gman;

import com.google.gson.annotations.SerializedName;
import java.util.Iterator;
import java.util.List;

public class ModInfo {

   @SerializedName("DonateURL")
   public String donateURL;
   @SerializedName("UpdateURL")
   public String updateURL;
   @SerializedName("Versions")
   public List<String> versions;
   boolean updated = true;
   public final String modId;


   public ModInfo() {
      this.modId = "NULL";
   }

   public ModInfo(String modId, String updateURL) {
      this.updateURL = updateURL;
      this.donateURL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=PBXHJ67N62ZRW";
      this.modId = modId;
   }

   public void determineUpdate(String currentModVersion, String currentMinecraftVersion) {
      Iterator<String> i$ = this.versions.iterator();

      while(i$.hasNext()) {
         String s = (String)i$.next();
         if(s.startsWith(currentMinecraftVersion)) {
            this.updated = s.split(":")[1].trim().equals(currentModVersion);
            break;
         }
      }

   }

   public final String getUpdateSite() {
      return this.updateURL;
   }

   public final boolean isUpdated() {
      return this.updated;
   }

   public String toString() {
      return "[" + this.modId + "]" + "; Up to date? " + (this.isUpdated()?"Yes":"No");
   }

   public final boolean updateNeeded() {
      return !this.updated;
   }
}
