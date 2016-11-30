package net.gliby.gman;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import net.gliby.gman.ModInfo;
import org.apache.logging.log4j.Logger;

public class GMan {

   public static void launchMod(Logger logger, ModInfo modInfo, String minecraftVersion, String modVersion) {
      String url = "https://raw.githubusercontent.com/Gliby/Mod-Information-Storage/master/" + modInfo.modId + ".json";
      Gson gson = new Gson();
      InputStreamReader reader = null;

      try {
         reader = new InputStreamReader((new URL(url)).openStream());
      } catch (MalformedURLException var8) {
         var8.printStackTrace();
         return;
      } catch (IOException var9) {
         logger.info("Failed to retrieve mod info, either mod doesn\'t exist or host(" + url + ") is down?");
         return;
      }

      ModInfo externalInfo = (ModInfo)gson.fromJson(reader, ModInfo.class);
      modInfo.donateURL = externalInfo.donateURL;
      modInfo.updateURL = externalInfo.updateURL;
      modInfo.versions = externalInfo.versions;
      modInfo.determineUpdate(modVersion, minecraftVersion);
      logger.info(modInfo.isUpdated()?"Mod is up-to-date.":"Mod is outdated, download latest at " + modInfo.updateURL);
   }
}
