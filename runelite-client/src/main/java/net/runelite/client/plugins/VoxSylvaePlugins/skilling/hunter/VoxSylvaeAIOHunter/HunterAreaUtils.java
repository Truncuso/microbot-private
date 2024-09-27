package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;

import net.runelite.api.coords.WorldPoint;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class HunterAreaUtils {

    private static final String HUNTER_AREA_LOCATION_CLASS = "net.runelite.client.plugins.worldmap.HunterAreaLocation";

    public static List<String> getAllAreaNames() {
        try {
            Class<?> enumClass = Class.forName(HUNTER_AREA_LOCATION_CLASS);
            return Arrays.stream(enumClass.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<String> getAreaNamesForCreature(String creatureName) {
        try {
            Class<?> enumClass = Class.forName(HUNTER_AREA_LOCATION_CLASS);
            Method getTooltip = enumClass.getMethod("getTooltip");
            return Arrays.stream(enumClass.getEnumConstants())
                    .filter(area -> {
                        try {
                            return ((String) getTooltip.invoke(area)).toLowerCase().contains(creatureName.toLowerCase());
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<WorldPoint> getLocationsForCreature(String creatureName) {
        try {
            Class<?> enumClass = Class.forName(HUNTER_AREA_LOCATION_CLASS);
            Method getTooltip = enumClass.getMethod("getTooltip");
            Method getLocation = enumClass.getMethod("getLocation");
            return Arrays.stream(enumClass.getEnumConstants())
                    .filter(area -> {
                        try {
                            return ((String) getTooltip.invoke(area)).toLowerCase().contains(creatureName.toLowerCase());
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .map(area -> {
                        try {
                            return (WorldPoint) getLocation.invoke(area);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(wp -> wp != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static List<String> getAllCreatures() {
        try {
            Class<?> enumClass = Class.forName(HUNTER_AREA_LOCATION_CLASS);
            Method getTooltip = enumClass.getMethod("getTooltip");
            return Arrays.stream(enumClass.getEnumConstants())
                    .flatMap(area -> {
                        try {
                            return Arrays.stream(((String) getTooltip.invoke(area)).split("<br>"));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(creature -> creature != null)
                    .map(creature -> creature.substring(0, creature.lastIndexOf('(')).trim())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public static int getRequiredLevelForCreature(String creatureName) {
        try {
            Class<?> enumClass = Class.forName(HUNTER_AREA_LOCATION_CLASS);
            Method getTooltip = enumClass.getMethod("getTooltip");
            return Arrays.stream(enumClass.getEnumConstants())
                    .flatMap(area -> {
                        try {
                            return Arrays.stream(((String) getTooltip.invoke(area)).split("<br>"));
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(creature -> creature != null && creature.toLowerCase().startsWith(creatureName.toLowerCase()))
                    .findFirst()
                    .map(creature -> {
                        int start = creature.lastIndexOf('(') + 1;
                        int end = creature.lastIndexOf(')');
                        return Integer.parseInt(creature.substring(start, end));
                    })
                    .orElse(-1);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}