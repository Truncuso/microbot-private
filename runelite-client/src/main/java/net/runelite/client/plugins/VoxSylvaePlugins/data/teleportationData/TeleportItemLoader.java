package net.runelite.client.plugins.VoxSylvaePlugins.data.teleportationData;

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

public class TeleportItemLoader {

    private static final String ITEM_TELEPORT_JSON_PATH = "../data/teleportationData/";

    public void loadTeleportItemLoaderTeleports(Map<TeleportType, List<Teleport>> teleports, String fileName) {
        try (FileReader reader = new FileReader(ITEM_TELEPORT_JSON_PATH+fileName)) {
            Gson gson = new Gson();
            Type jewelryTeleportListType = new TypeToken<ArrayList<JsonTeleportItemLoaderTeleport>>() {}.getType();
            List<JsonTeleportItemLoaderTeleport> jsonJewelryTeleports = gson.fromJson(reader, jewelryTeleportListType);

            List<TeleportItem> jewelryTeleports = jsonJewelryTeleports.stream()
                .map(this::convertJsonToTeleportItem)
                .collect(Collectors.toList());

            for (TeleportItem teleport : jewelryTeleports) {
                teleports.computeIfAbsent(teleport.getType(), k -> new ArrayList<>()).add(teleport);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TeleportItem convertJsonToTeleportItem(JsonTeleportItemLoaderTeleport json) {
        List<WorldPoint> destinations = json.destinations.stream()
            .map(point -> new WorldPoint(point.x, point.y, point.z))
            .collect(Collectors.toList());

        return new TeleportItem(
            json.name,
            TeleportType.valueOf(json.type),
            -1,  // Assuming itemId is not provided in the JSON; use -1 as default
            json.requiredLevel,
            json.requiredItems,
            json.requiredQuests,
            destinations,
            json.isEquippable,
            json.charges,
            json.additionalInfo.locations,
            json.additionalInfo.membersOnly,
            json.additionalInfo.rechargeable,
            json.additionalInfo.notes
        );
    }

    // Helper class to match the JSON structure
    private static class JsonTeleportItemLoaderTeleport {
        String name;
        String type;
        int requiredLevel;
        List<ItemQuantity> requiredItems;
        List<String> requiredQuests;
        List<JsonWorldPoint> destinations;
        boolean isEquippable;
        int charges;
        AdditionalInfo additionalInfo;

        // Inner class for the additionalInfo field
        static class AdditionalInfo {
            List<String> locations;
            boolean membersOnly;
            boolean rechargeable;
            String notes;
        }

        // Inner class for the destination points
        static class JsonWorldPoint {
            int x, y, z;
        }
    }
}