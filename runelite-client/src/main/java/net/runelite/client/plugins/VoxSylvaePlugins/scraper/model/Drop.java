package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
@Getter
@Setter
@ToString
public class Drop {
    private WikiItemResult item;
    private List<Integer> quantity;
    private double rarity;
    private boolean lootStatus;

    public Drop(WikiItemResult item, List<Integer> quantity, double rarity) {
        this.item = item;
        this.quantity = quantity;
        this.rarity = rarity;
        this.lootStatus = false;
    }
}