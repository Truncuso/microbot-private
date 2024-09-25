package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;

import net.runelite.api.coords.WorldPoint;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.runelite.client.plugins.worldmap.*;
public class HunterAreaUtils {
    HunterAreaLocation HunterAreaLocation;
    /**
     * Get all hunter areas.
     * @return List of all HunterAreaLocation enum values.
     */
    public static List<HunterAreaLocation> getAllAreas() {
        return Arrays.asList(HunterAreaLocation.values());
    }

    /**
     * Search for areas containing a specific creature.
     * @param creatureName The name of the creature to search for.
     * @return List of HunterAreaLocation where the creature can be found.
     */
    public static List<HunterAreaLocation> getAreasForCreature(String creatureName) {
        return Arrays.stream(HunterAreaLocation.values())
                .filter(area -> area.getTooltip().toLowerCase().contains(creatureName.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Get all WorldPoints for areas containing a specific creature.
     * @param creatureName The name of the creature to search for.
     * @return List of WorldPoints where the creature can be found.
     */
    public static List<WorldPoint> getLocationsForCreature(String creatureName) {
        return getAreasForCreature(creatureName).stream()
                .map(HunterAreaLocation::getLocation)
                .collect(Collectors.toList());
    }

    /**
     * Get all creatures available in the game.
     * @return List of all unique creature names.
     */
    public static List<String> getAllCreatures() {
        return Arrays.stream(HunterAreaLocation.values())
                .flatMap(area -> Arrays.stream(area.getTooltip().split("<br>")))
                .map(creature -> creature.substring(0, creature.lastIndexOf('(')).trim())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Get the required level for a specific creature.
     * @param creatureName The name of the creature.
     * @return The required level, or -1 if the creature is not found.
     */
    public static int getRequiredLevelForCreature(String creatureName) {
        return Arrays.stream(HunterAreaLocation.values())
                .flatMap(area -> Arrays.stream(area.getTooltip().split("<br>")))
                .filter(creature -> creature.toLowerCase().startsWith(creatureName.toLowerCase()))
                .findFirst()
                .map(creature -> {
                    int start = creature.lastIndexOf('(') + 1;
                    int end = creature.lastIndexOf(')');
                    return Integer.parseInt(creature.substring(start, end));
                })
                .orElse(-1);
    }
}