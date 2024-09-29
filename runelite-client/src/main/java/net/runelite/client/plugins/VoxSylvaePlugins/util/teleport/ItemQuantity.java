package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

public class ItemQuantity {
    private String name;
    private int quantity;

    public ItemQuantity(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    @Override
    public String toString() {
        return quantity + " x " + name;
    }
}