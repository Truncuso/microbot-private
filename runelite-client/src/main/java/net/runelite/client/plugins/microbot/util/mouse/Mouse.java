package net.runelite.client.plugins.microbot.util.mouse;

import net.runelite.api.Point;
import net.runelite.client.plugins.microbot.Microbot;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static net.runelite.client.plugins.microbot.util.math.Random.random;

public abstract class Mouse {

    public Mouse() {
    }

    public Canvas getCanvas() {
        return Microbot.getClient().getCanvas();
    }

    public int randomizeClick() {
        return random(-10, 10);
    }

    public abstract Mouse click(int x, int y);

    public abstract Mouse click(double x, double y);

    public abstract Mouse click(Rectangle rectangle);

    public abstract Mouse click(int x, int y, boolean rightClick);

    public abstract Mouse click(Point point);
    public abstract Mouse click(Point point, boolean rightClick);

    public abstract Mouse click();

    public abstract Mouse move(Point point);
    public abstract Mouse move(Rectangle rect);

    public abstract Mouse move(int x, int y);
    public abstract Mouse move(double x, double y);
    public abstract Mouse move(Polygon polygon);
    public abstract Mouse scrollDown(Point point);
    public abstract Mouse scrollUp(Point point);
    public abstract java.awt.Point getMousePosition();

}
