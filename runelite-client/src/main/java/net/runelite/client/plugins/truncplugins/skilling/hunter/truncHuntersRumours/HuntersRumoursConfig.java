package net.runelite.client.plugins.truncplugins.skilling.hunter.truncHuntersRumours;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.microbot.util.inventory.DropOrder;

import static net.runelite.client.plugins.truncplugins.skilling.hunter.truncHuntersRumours.HuntersRumoursConfig.GROUP;

@ConfigGroup(GROUP)
public interface HuntersRumoursConfig extends Config {
    String GROUP = "HuntersRumours";

    @ConfigSection(
            name = "General",
            description = "General",
            position = 0,
            closedByDefault = false
    )
    String generalSection = "general";

    @ConfigItem(
            keyName = "GUIDE",
            name = "GUIDE",
            description = "GUIDE",
            position = 0,
            section = generalSection
    )
    default String GUIDE() {
        return "This plugin allows for fully automated barbarian fishing at Otto's Grotto. \n\n" +
                "To use this plugin, simply start the script at Otto's Grotto with a Barbarian rod and feathers in your inventory.";
    }

    // drop order
    @ConfigItem(
            keyName = "dropOrder",
            name = "Drop Order",
            description = "The order in which to drop items",
            position = 1,
            section = generalSection
    )
    default DropOrder dropOrder() {
        return DropOrder.STANDARD;
    }

    @ConfigItem(
        keyName = "trapLimit",
        name = "Trap Limit",
        description = "The maximum number of traps to set"
    )
    default int trapLimit() {
        return 5;
    }

    @ConfigItem(
        keyName = "useAntiban",
        name = "Enable Antiban",
        description = "Enable antiban measures like random camera movements"
    )
    default boolean useAntiban() {
        return true;
    }

    @ConfigItem(
        keyName = "breakDuration",
        name = "Break Duration",
        description = "Duration of breaks in milliseconds"
    )
    default int breakDuration() {
        return 30000; // 30 seconds
    }

}