package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiNPCInfo;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ScraperResult;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class WikiNPCResult extends ScraperResult {
    private String name;
    private String version;  // New field for version
    private List<Integer> ids;
    private String imagePath;
    private String examine;
    private int combatLevel;
    private int hitpoints;
    private boolean aggressive;
    private boolean poisonous;
    private List<String> attributes;
    private List<WikiNPCLocation> locations;
    private List<String> options;
    private Map<String,List<WikiMonsterDrop>> drops;

    // Additional fields
    private int attackLevel;
    private int strengthLevel;
    private int defenceLevel;
    private int magicLevel;
    private int rangedLevel;
    private int attackSpeed;
    private String attackStyle;
    private int maxHit;
    private boolean isMembers;
    private String size;
    private String race;
    private String releaseDate;
    private String updateName;

    // Fields specific to monsters
    private int xpBonus;
    private int attackBonus;
    private int strengthBonus;
    private int magicAttackBonus;
    private int magicStrengthBonus;
    private int rangedAttackBonus;
    private int rangedStrengthBonus;
    private int stabDefence;
    private int slashDefence;
    private int crushDefence;
    private int magicDefence;
    private int rangedDefence;
    private boolean immunePoison;
    private boolean immuneVenom;
    private boolean immuneCannon;
    private boolean immuneThrall;
    private int respawnTime;
    private int slayerLevel;
    private int slayerXp;
    @Override
    public String toString() {
        return "WikiNPCResult{" +
            "name='" + name + '\'' +
            "version='" + version + '\'' +
            ", ids=" + ids +
            ", combatLevel=" + combatLevel +
            ", hitpoints=" + hitpoints +
            ", aggressive=" + aggressive +
            ", poisonous=" + poisonous +
            ", attributes=" + attributes +
            ", locations=" + locations +
            ", version='" + version + '\'' +
            // ... (add other fields as needed)
            '}';
    }
}