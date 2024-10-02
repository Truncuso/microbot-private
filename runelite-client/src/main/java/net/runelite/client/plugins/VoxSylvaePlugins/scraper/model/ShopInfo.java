package net.runelite.client.plugins.VoxSylvaePlugins.scraper.model;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
public class ShopInfo {
    private String name;
    private int quantity;
    private int price;

    @Override
    public String toString() {
        return String.format("ShopInfo{name='%s', quantity=%d, price=%d}", name, quantity, price);
    }
}