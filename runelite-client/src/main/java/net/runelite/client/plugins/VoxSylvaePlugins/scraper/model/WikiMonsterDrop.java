package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class WikiMonsterDrop {
    private WikiItemResult item;
    private List<Integer> quantity;
    private double rarity;
    private boolean membersOnly;
    private boolean freeToPlay;
    private String notes;
    private String altRarity;
    private String category;
    private String raritynotes;
    private boolean gemwExcluded;

    public WikiMonsterDrop(WikiItemResult item, List<Integer> quantity, double rarity) {
        this.item = item;
        this.quantity = quantity;
        this.rarity = rarity;
    }
}