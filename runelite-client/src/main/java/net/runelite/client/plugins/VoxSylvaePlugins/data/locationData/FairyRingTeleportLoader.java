package net.runelite.client.plugins.VoxSylvaePlugins.data.locationData;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.VoxSylvaePlugins.util.teleport.*;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FairyRingTeleportLoader {

    private static final String FAIRY_RING_JSON_PATH = "../data/locationData/";

    // Method to load the fairy ring teleports from the JSON file
    public void loadFairyRingTeleports(Map<TeleportType, List<Teleport>> teleports, String fileName) {
        try (FileReader reader = new FileReader(FAIRY_RING_JSON_PATH + fileName)) {
            Gson gson = new Gson();
            Type fairyRingTeleportListType = new TypeToken<ArrayList<JsonFairyRingTeleport>>(){}.getType();
            
            List<JsonFairyRingTeleport> jsonFairyRingTeleports = gson.fromJson(reader, fairyRingTeleportListType);
            
            // Convert JSON to FairyRingTeleport objects
            List<FairyRingTeleport> fairyRingTeleports = jsonFairyRingTeleports.stream()
                .map(this::convertJsonToFairyRingTeleport)
                .collect(Collectors.toList());
            
            // Add them to the teleport map
            teleports.put(TeleportType.FAIRY_RING, new ArrayList<>(fairyRingTeleports));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Helper method to convert the JSON structure to FairyRingTeleport objects
    private FairyRingTeleport convertJsonToFairyRingTeleport(JsonFairyRingTeleport json) {
        return new FairyRingTeleport(
            json.name,
            json.requiredLevel,
            json.requiredItems,
            json.requiredQuests,
            json.destinations,
            json.additionalInfo.code,
            json.additionalInfo.location,
            json.additionalInfo.notes,
            json.additionalInfo.points_of_interest
        );
    }

    // Helper class to match the JSON structure
    private static class JsonFairyRingTeleport {
        String name;
        int requiredLevel;
        List<ItemQuantity> requiredItems;
        List<String> requiredQuests;
        List<WorldPoint> destinations;
        AdditionalInfo additionalInfo;

        // Inner class to match additional information in the JSON
        static class AdditionalInfo {
            String code;
            String location;
            String notes;
            List<String> points_of_interest;
        }
    }
}