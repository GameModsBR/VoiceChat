/*
 * Decompiled with CFR 0_118.
 * 
 * Could not load the following classes:
 *  com.google.gson.annotations.SerializedName
 */
package net.gliby.gman;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ModInfo {
    @SerializedName(value="DonateURL")
    public String donateURL;
    @SerializedName(value="UpdateURL")
    public String updateURL;
    @SerializedName(value="Versions")
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
        for (String s : this.versions) {
            if (!s.startsWith(currentMinecraftVersion)) continue;
            this.updated = s.split(":")[1].trim().equals(currentModVersion);
            break;
        }
    }

    public final String getUpdateSite() {
        return this.updateURL;
    }

    public final boolean isUpdated() {
        return this.updated;
    }

    public String toString() {
        return "[" + this.modId + "]" + "; Up to date? " + (this.isUpdated() ? "Yes" : "No");
    }

    public final boolean updateNeeded() {
        return !this.updated;
    }
}

