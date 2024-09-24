package net.runelite.client.plugins.VoxSylvaePlugins.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.VoxSylvaePlugins.data.locationData.JewelryTeleport;
import net.runelite.client.plugins.VoxSylvaePlugins.util.teleport.*;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeleportationManager {
    private Map<TeleportType, List<Teleport>> teleports;

    public TeleportationManager() {
        teleports = new HashMap<>();
        loadTeleportData();
    }

    private void loadTeleportData() {
        loadStandardSpellbookTeleports();
        loadAncientMagicksTeleports();
        loadArceuusTeleports();
        loadLunarMagicTeleports();
        loadJewelryTeleports();
        loadFairyRingTeleports();
        loadPOHTeleports();
    }

    private void loadStandardSpellbookTeleports() {
        List<Teleport> standardTeleports = loadTeleportsFromFile("teleportationSpells.json");
        teleports.put(TeleportType.STANDARD_SPELLBOOK, standardTeleports);
    }

    private void loadAncientMagicksTeleports() {
        List<Teleport> ancientTeleports = loadTeleportsFromFile("ancientMagicksTeleports.json");
        teleports.put(TeleportType.ANCIENT_MAGICKS, ancientTeleports);
    }

    private void loadArceuusTeleports() {
        List<Teleport> arceuusTeleports = loadTeleportsFromFile("arceuusTeleports.json");
        teleports.put(TeleportType.ARCEUUS_SPELLBOOK, arceuusTeleports);
    }

    private void loadLunarMagicTeleports() {
        List<Teleport> lunarTeleports = loadTeleportsFromFile("lunarMagicTeleports.json");
        teleports.put(TeleportType.LUNAR_SPELLBOOK, lunarTeleports);
    }

    private void loadJewelryTeleports() {
        try (FileReader reader = new FileReader("../data/locationData/jewelryTeleports.json")) {
            Gson gson = new Gson();
            Type jewelryTeleportListType = new TypeToken<ArrayList<JewelryTeleport>>(){}.getType();
            List<JewelryTeleport> jewelryTeleports = gson.fromJson(reader, jewelryTeleportListType);
            teleports.put(TeleportType.JEWELRY, new ArrayList<>(jewelryTeleports));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFairyRingTeleports() {
        try (FileReader reader = new FileReader("../data/locationData/fairyRingTeleports.json")) {
            Gson gson = new Gson();
            Type fairyRingTeleportListType = new TypeToken<ArrayList<FairyRingTeleport>>(){}.getType();
            List<FairyRingTeleport> fairyRingTeleports = gson.fromJson(reader, fairyRingTeleportListType);
            teleports.put(TeleportType.FAIRY_RING, new ArrayList<>(fairyRingTeleports));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPOHTeleports() {
        try (FileReader reader = new FileReader("../data/locationData/POHTeleportOptions.json")) {
            Gson gson = new Gson();
            Type pohTeleportListType = new TypeToken<ArrayList<POHTeleport>>(){}.getType();
            List<POHTeleport> pohTeleports = gson.fromJson(reader, pohTeleportListType);
            teleports.put(TeleportType.POH, new ArrayList<>(pohTeleports));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Teleport findNearestTeleport(WorldPoint destination) {
        Teleport nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (List<Teleport> teleportList : teleports.values()) {
            for (Teleport teleport : teleportList) {
                double distance = teleport.getDistanceTo(destination);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = teleport;
                }
            }
        }

        return nearest;
    }

    public Teleport findNearestTeleport(WorldPoint destination, TeleportType type) {
        List<Teleport> typeSpecificTeleports = teleports.get(type);
        if (typeSpecificTeleports == null) {
            return null;
        }

        return findNearestTeleportInList(destination, typeSpecificTeleports);
    }

    public Teleport findNearestTeleport(WorldPoint destination, List<String> completedQuests) {
        Teleport nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (List<Teleport> teleportList : teleports.values()) {
            for (Teleport teleport : teleportList) {
                if (meetsQuestRequirements(teleport, completedQuests)) {
                    double distance = teleport.getDistanceTo(destination);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = teleport;
                    }
                }
            }
        }

        return nearest;
    }

    private Teleport findNearestTeleportInList(WorldPoint destination, List<Teleport> teleportList) {
        Teleport nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Teleport teleport : teleportList) {
            double distance = teleport.getDistanceTo(destination);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = teleport;
            }
        }

        return nearest;
    }

    private boolean meetsQuestRequirements(Teleport teleport, List<String> completedQuests) {
        if (teleport.getRequiredQuests() == null || teleport.getRequiredQuests().isEmpty()) {
            return true;
        }
        return completedQuests.containsAll(teleport.getRequiredQuests());
    }

    // Add more methods as needed, such as filtering by requirements, etc.
}