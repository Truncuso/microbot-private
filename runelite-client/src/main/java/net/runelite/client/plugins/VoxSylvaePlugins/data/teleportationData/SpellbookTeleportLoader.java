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

public class SpellbookTeleportLoader {

    private static final String SPELLBOOK_JSON_PATH = "../data/teleportationData/";

    public void loadSpellbookTeleports(Map<TeleportType, List<Teleport>> teleports, String filename) {
        try (FileReader reader = new FileReader(SPELLBOOK_JSON_PATH + filename)) {
            Gson gson = new Gson();
            Type spellbookTeleportListType = new TypeToken<ArrayList<JsonSpellbookTeleport>>(){}.getType();
            
            List<JsonSpellbookTeleport> jsonSpellbookTeleports = gson.fromJson(reader, spellbookTeleportListType);
            
            List<SpellbookTeleport> spellbookTeleports = jsonSpellbookTeleports.stream()
                .map(this::convertJsonToSpellbookTeleport)
                .collect(Collectors.toList());
            
            for (SpellbookTeleport teleport : spellbookTeleports) {
                teleports.computeIfAbsent(teleport.getType(), k -> new ArrayList<>()).add(teleport);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private SpellbookTeleport convertJsonToSpellbookTeleport(JsonSpellbookTeleport json) {
        return new SpellbookTeleport(
            json.name,
            TeleportType.valueOf(json.type),
            json.requiredLevel,
            json.requiredItems,
            json.requiredQuests,
            json.destinations,
            json.additionalInfo.location,
            json.additionalInfo.membersOnly
        );
    }

    // Helper class to match the JSON structure
    private static class JsonSpellbookTeleport {
        String name;
        String type;
        int requiredLevel;
        List<ItemQuantity> requiredItems;
        List<String> requiredQuests;
        List<WorldPoint> destinations;
        AdditionalInfo additionalInfo;

        // Additional info matches the JSON structure for location and membersOnly
        static class AdditionalInfo {
            String location;
            boolean membersOnly;
        }
    }
}