package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;

import static net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter.AIOHunterConfig.GROUP;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.plugins.microbot.util.inventory.DropOrder;

@ConfigGroup(GROUP)
public interface AIOHunterConfig extends Config {

    String GROUP = "HuntersRumours";

    @ConfigSection(
            name = "General",
            description = "General settings",
            position = 0
    )
    String generalSection = "general";
    String antiBanSection = "AntiBan";

    
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

    @ConfigItem(
        keyName = "toggleKeybind",
        name = "Toggle Hunter Plugin",
        description = "Keybind to start/stop the hunter plugin",        
        position = 1, // Adjust position as needed
        section = generalSection
    )
    default Keybind toggleKeybind() {
        return Keybind.NOT_SET;
    }


    @ConfigItem(
        keyName = "devDebug",
        name = "Enable developer debug",
        description = "Enable developer debug",
    )
    default boolean devDebug() {
        return true;
    }
    // drop order
    @ConfigItem(
            keyName = "dropOrder",
            name = "Drop Order",
            description = "The order in which to drop items",
            position = 2,
            section = generalSection
    )
    default DropOrder dropOrder() {
        return DropOrder.STANDARD;
    }


    @ConfigItem(
        keyName = "huntingMode",
        name = "Hunting Mode",
        description = "Choose between Hunter Rumours or Classic Hunting",
        position = 1,
        section = generalSection
    )
    default HuntingMode huntingMode() {
        return HuntingMode.HUNTER_RUMOURS;
    }

    @ConfigItem(
        keyName = "classicHuntingCreature",
        name = "Classic Hunting Creature",
        description = "Select the creature to hunt in classic mode",
        position = 2,
        section = generalSection
    )
    default HuntingCreature classicHuntingCreature() {
        return HuntingCreature.RED_CHINCHOMPA;
    }



    @ConfigItem(
        keyName = "trapLimit",
        name = "Trap Limit",
        description = "The maximum number of traps to set"
    )
    default int trapLimit() {
        return 5;
    }
//######################### antiban settings
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