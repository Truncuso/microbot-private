package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DropSource {
    private String sourceName;
    private int sourceLevel;
    private int minQuantity;
    private int maxQuantity;
    private double dropRate;
    private String notes;

    @Override
    public String toString() {
        return "DropSource{" +
            "sourceName='" + sourceName + '\'' +
            ", sourceLevel=" + sourceLevel +
            ", minQuantity=" + minQuantity +
            ", maxQuantity=" + maxQuantity +
            ", dropRate=" + dropRate +
            ", notes='" + notes + '\'' +
            '}';
    }
}