package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ScraperResult;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcStats.Drop;

import java.util.List;

@Getter
@Setter
public class WikiItemResult extends ScraperResult {
    private String name;
    private int id;
    private String imagePath;
    private boolean noteable;
    private List<String> options;
    private boolean stackable;
    private boolean tradeable;
    private boolean equipable;
    private boolean isMembers;
    private String examine;
    private int highAlchValue;
    private int value;
    private double weight;    
    private CombatStats combatStats;
    private ItemSources itemSources;
    @Override
    public String toString() {
        return "WikiItemResult{" +
            "name='" + name + '\'' +
            ", id=" + id +
            ", noteable=" + noteable +
            ", stackable=" + stackable +
            ", tradeable=" + tradeable +
            ", equipable=" + equipable +
            ", examine='" + examine + '\'' +
            ", highAlchValue=" + highAlchValue +
            ", weight=" + weight +
            ", gePrice=" + getgePrice() +
            ", combatStats=" + combatStats +
            '}';
    }
    public int getgePrice(){
        //fetch current ge price from wiki
        return 0;
    }
    public boolean hasItemSources(){
        return !itemSources.hasItemSources();

    }
    public List<DropSource> getDropSources(){
        return itemSources.getDropSources();
    }
    
}