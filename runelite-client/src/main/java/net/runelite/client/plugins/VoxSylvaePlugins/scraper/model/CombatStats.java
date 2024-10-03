package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CombatStats {
    private int astab;
    private int aslash;
    private int acrush;
    private int amagic;
    private int arange;
    private int dstab;
    private int dslash;
    private int dcrush;
    private int dmagic;
    private int drange;
    private int str;
    private int rstr;
    private int mdmg;
    private int prayer;
    private String slot;
    private int speed;
    private int attackRange;
    private String combatStyle;
    private String image;
    private String altImage;

    @Override
    public String toString() {
        return "CombatStats{" +
            "astab=" + astab +
            ", aslash=" + aslash +
            ", acrush=" + acrush +
            ", amagic=" + amagic +
            ", arange=" + arange +
            ", dstab=" + dstab +
            ", dslash=" + dslash +
            ", dcrush=" + dcrush +
            ", dmagic=" + dmagic +
            ", drange=" + drange +
            ", str=" + str +
            ", rstr=" + rstr +
            ", mdmg=" + mdmg +
            ", prayer=" + prayer +
            ", slot='" + slot + '\'' +
            ", speed=" + speed +
            ", attackRange=" + attackRange +
            ", combatStyle='" + combatStyle + '\'' +
            ", image='" + image + '\'' +
            ", altImage='" + altImage + '\'' +
            '}';
    }
}