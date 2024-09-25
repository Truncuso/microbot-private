package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;

import static net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter.AIOHunterConfig.GROUP;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Keybind;
import net.runelite.client.plugins.microbot.util.inventory.DropOrder;
import net.runelite.api.coords.WorldPoint;
import java.util.List;
@ConfigGroup(GROUP)
public interface AIOHunterConfig extends Config {

    String GROUP = "HuntersRumours";

    @ConfigSection(
            name = "General",
            description = "General settings",
            position = 0
    )
    String generalSection = "general";
    String huntingSection = "hunting";
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
        position = 2, // Adjust position as needed
        section = generalSection
    )
    default boolean devDebug() {
        return true;
    }
    // drop order
    @ConfigItem(
            keyName = "dropOrder",
            name = "Drop Order",
            description = "The order in which to drop items",
            position = 3,
            section = generalSection
    )
    default DropOrder dropOrder() {
        return DropOrder.STANDARD;
    }


    @ConfigItem(
        keyName = "huntingMode",
        name = "Hunting Mode",
        description = "Choose between Hunter Rumours or Classic Hunting",
        position = 0,
        section = huntingSection
    )
    default HuntingMode huntingMode() {
        return HuntingMode.CLASSIC_HUNTING;
    }

    @ConfigItem(
        keyName = "preferredHuntingCreature",
        name = "preferred Hunting Creature",
        description = "Select the creature to hunt in classic mode",
        position = 1,
        section = huntingSection
    )
    default HunterCreatureTarget preferredHuntingCreature() {
        CreatureLocation location = new CreatureLocation("Feldip Hills", new WorldPoint(2536, 2910, 0));
        List<CreatureLocation> locations = List.of(location);
        HunterCreatureTarget tmp = new HunterCreatureTarget ("Red Chinchompa" , 63, "Box Traps", locations);
        return tmp;
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
        description = "Enable antiban measures like random camera movements",
        section = antiBanSection
    )
    default boolean useAntiban() {
        return true;
    }

    @ConfigItem(
        keyName = "breakDuration",
        name = "Break Duration",
        description = "Duration of breaks in milliseconds",
        section = antiBanSection
    )
    default int breakDuration() {
        return 30000; // 30 seconds
    }
    enum HuntingMode {
        HUNTER_RUMOURS,
        CLASSIC_HUNTING
    }
    

}