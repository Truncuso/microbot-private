package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class NPCResult extends ScraperResult {
    private List<String> names;
    private List<Integer> ids;
    private List<String> imagePaths;
    private List<String> examine;
    private List<Integer> combatLevel;
    private List<Integer> hitpoints;
    private List<Boolean> aggressive;
    private List<Boolean> poisonous;
    private List<String> attributes;
    private List<String> locations;
    private List<Drop> drops;

    // Additional fields
    private List<Integer> attackLevel;
    private List<Integer> strengthLevel;
    private List<Integer> defenceLevel;
    private List<Integer> magicLevel;
    private List<Integer> rangedLevel;
    private List<Integer> attackSpeed;
    private List<String> attackStyle;
    private List<Integer> maxHit;
    private Boolean isMembers;
    private String size;
    private String race;

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NPCResult{\n");
        
        for (int i = 0; i < names.size(); i++) {
            sb.append("  Version ").append(i + 1).append(":\n");
            sb.append("    Name: ").append(names.get(i)).append("\n");
            sb.append("    ID: ").append(ids.get(i)).append("\n");
            if (examine != null && i < examine.size()) sb.append("    Examine: ").append(examine.get(i)).append("\n");
            if (combatLevel != null && i < combatLevel.size()) sb.append("    Combat Level: ").append(combatLevel.get(i)).append("\n");
            if (hitpoints != null && i < hitpoints.size()) sb.append("    Hitpoints: ").append(hitpoints.get(i)).append("\n");
            if (aggressive != null && i < aggressive.size()) sb.append("    Aggressive: ").append(aggressive.get(i)).append("\n");
            if (poisonous != null && i < poisonous.size()) sb.append("    Poisonous: ").append(poisonous.get(i)).append("\n");
            if (attackLevel != null && i < attackLevel.size()) sb.append("    Attack Level: ").append(attackLevel.get(i)).append("\n");
            if (strengthLevel != null && i < strengthLevel.size()) sb.append("    Strength Level: ").append(strengthLevel.get(i)).append("\n");
            if (defenceLevel != null && i < defenceLevel.size()) sb.append("    Defence Level: ").append(defenceLevel.get(i)).append("\n");
            if (magicLevel != null && i < magicLevel.size()) sb.append("    Magic Level: ").append(magicLevel.get(i)).append("\n");
            if (rangedLevel != null && i < rangedLevel.size()) sb.append("    Ranged Level: ").append(rangedLevel.get(i)).append("\n");
            if (attackSpeed != null && i < attackSpeed.size()) sb.append("    Attack Speed: ").append(attackSpeed.get(i)).append("\n");
            if (attackStyle != null && i < attackStyle.size()) sb.append("    Attack Style: ").append(attackStyle.get(i)).append("\n");
            if (maxHit != null && i < maxHit.size()) sb.append("    Max Hit: ").append(maxHit.get(i)).append("\n");
        }

        sb.append("  Common Properties:\n");
        if (imagePaths != null) sb.append("    Image Paths: ").append(imagePaths).append("\n");
        if (attributes != null) sb.append("    Attributes: ").append(attributes).append("\n");
        if (locations != null) sb.append("    Locations: ").append(locations).append("\n");
        sb.append("    Is Members: ").append(isMembers).append("\n");
        sb.append("    Size: ").append(size).append("\n");
        sb.append("    Race: ").append(race).append("\n");

        if (drops != null) {
            sb.append("  Drops:\n");
            for (Drop drop : drops) {
                sb.append("    ").append(drop.getItem().getNames().get(0)).append(": ")
                  .append("Quantity ").append(drop.getQuantity())
                  .append(", Rarity ").append(drop.getRarity())
                  .append("\n");
            }
        }

        sb.append("}");
        return sb.toString();
    }
}