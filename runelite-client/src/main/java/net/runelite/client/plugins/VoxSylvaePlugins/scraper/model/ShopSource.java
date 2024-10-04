package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopSource {
    private String shopName;
    private String location;
    private int numberInStock;
    private String restockTime;
    private int priceSoldAt;
    private int priceBoughtAt;
    private double changePercent;
    private boolean members;
    private String notes;

    @Override
    public String toString() {
        return "ShopSource{" +
            "shopName='" + shopName + '\'' +
            ", location='" + location + '\'' +
            ", numberInStock=" + numberInStock +
            ", restockTime='" + restockTime + '\'' +
            ", priceSoldAt=" + priceSoldAt +
            ", priceBoughtAt=" + priceBoughtAt +
            ", changePercent=" + changePercent +
            ", members=" + members +
            ", notes='" + notes + '\'' +
            '}';
    }
}