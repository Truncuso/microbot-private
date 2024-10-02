package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.NPCResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ItemResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.util.StringUtil;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.Drop;



import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VSNPCScraper extends VSWikiScraper<NPCResult> {
    private static final Logger logger = LoggerFactory.getLogger(VSNPCScraper.class);
    private final Path npcDatabaseFile;
    private final List<String> npcKeys = Arrays.asList("names", "ids", "imagePaths", "examine", "combatLevel", "hitpoints", "aggressive", "poisonous", "attributes", "locations", "drops");
    private Map<String, NPCResult> npcDatabase;
    private VSItemScraper itemScraper;

    public VSNPCScraper(Map<String, Object> databaseDict, Path destination, String databaseName, Path imageFolder, boolean resetDatabase) {
        super(destination, databaseName, imageFolder, resetDatabase);
        this.npcDatabaseFile = getDefaultDatabaseJson().resolveSibling("npcDB.json");
        this.npcDatabase = loadDatabase(npcDatabaseFile, resetDatabase, NPCResult.class);
        databaseDict.put("npcs", this.npcDatabase);
        this.itemScraper = new VSItemScraper(databaseDict, destination, databaseName, imageFolder, resetDatabase);
    }

    @Override
    public void saveDatabases() throws IOException {
        saveDatabase(npcDatabase, npcDatabaseFile);
        itemScraper.saveDatabases();
    }

    public Map<String, NPCResult> getNPCInfo(String npcNamesSearchString, boolean forceReload, boolean downloadImage, boolean saveDatabase, ImageType imageType, String imagePath) {
        List<String> npcNames = StringUtil.formatArgs(npcNamesSearchString);
        Map<String, NPCResult> npcInfo = new HashMap<>();

        for (String npcName : npcNames) {
            if (!forceReload && npcDatabase.containsKey(npcName)) {
                npcInfo.put(npcName, npcDatabase.get(npcName));
                continue;
            }

            WikipediaPage page = getWikiPage(npcName);
            if (page == null) {
                throw new RuntimeException("<RuneMatio> " + npcName + ": Wiki Page doesn't exist and is not in DB");
            }

            NPCResult npcInfoWiki = getNPCInfoFromWikiText(page, downloadImage, imageType, imagePath);
            npcInfo.put(npcName, npcInfoWiki);
            npcDatabase.put(npcName, npcInfoWiki);
        }

        checkDictKeys(npcInfo, npcKeys);
        if (saveDatabase) {
            try {
                saveDatabases();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return npcInfo;
    }

    private NPCResult getNPCInfoFromWikiText(WikipediaPage page, boolean downloadImage, ImageType imageType, String imagePath) {
        NPCResult npcResult = new NPCResult();
        Map<String, String> infoboxData = parseInfobox(page.getContent(), "NPC");

        if (infoboxData.isEmpty()) {
            logger.error("Failed to parse infobox data for NPC: {}", page.getTitle());
            return null;
        }

        npcResult.setNames(parseMultipleVersions(infoboxData, "name"));
        npcResult.setIds(parseMultipleVersionsInt(infoboxData, "id"));
        npcResult.setExamine(parseMultipleVersions(infoboxData, "examine"));
        npcResult.setCombatLevel(parseMultipleVersionsInt(infoboxData, "combat"));
        npcResult.setHitpoints(parseMultipleVersionsInt(infoboxData, "hitpoints"));
        npcResult.setAggressive(parseMultipleVersionsBool(infoboxData, "aggressive"));
        npcResult.setPoisonous(parseMultipleVersionsBool(infoboxData, "poisonous"));
        npcResult.setAttributes(parseAttributes(infoboxData.get("attributes")));
        npcResult.setLocations(parseLocations(page.getContent()));

        if (downloadImage) {
            List<String> imagePaths = downloadImagesFromTemplate(npcResult.getNames(), infoboxData.get("image"), imageType, imagePath);
            npcResult.setImagePaths(imagePaths);
        }

        npcResult.setDrops(parseDrops(page.getContent(), downloadImage, imageType, imagePath));

        return npcResult;
    }

    private List<String> parseMultipleVersions(Map<String, String> infoboxData, String key) {
        List<String> values = new ArrayList<>();
        if (infoboxData.containsKey(key)) {
            values.add(infoboxData.get(key));
        }
        int version = 1;
        while (infoboxData.containsKey(key + version)) {
            values.add(infoboxData.get(key + version));
            version++;
        }
        return values.isEmpty() ? null : values;
    }

    private List<Integer> parseMultipleVersionsInt(Map<String, String> infoboxData, String key) {
        List<String> stringValues = parseMultipleVersions(infoboxData, key);
        if (stringValues == null) return null;
        return stringValues.stream().map(s -> Integer.parseInt(s.replaceAll("[^0-9]", ""))).collect(java.util.stream.Collectors.toList());
    }

    private List<Boolean> parseMultipleVersionsBool(Map<String, String> infoboxData, String key) {
        List<String> stringValues = parseMultipleVersions(infoboxData, key);
        if (stringValues == null) return null;
        return stringValues.stream().map(s -> s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("true")).collect(java.util.stream.Collectors.toList());
    }

    private List<String> parseAttributes(String attributesString) {
        if (attributesString == null) return null;
        return Arrays.asList(attributesString.split(",\\s*"));
    }

    private List<String> parseLocations(String wikiText) {
        List<String> locations = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{Location\\|([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(wikiText);
        while (matcher.find()) {
            locations.add(matcher.group(1).split("\\|")[0].trim());
        }
        return locations.isEmpty() ? null : locations;
    }

    private List<Drop> parseDrops(String wikiText, boolean downloadImage, ImageType imageType, String imagePath) {
        List<Drop> drops = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{DropsLine\\|([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(wikiText);

        while (matcher.find()) {
            Map<String, String> dropData = parseDropLine(matcher.group(1));
            Drop drop = getDropFromLine(dropData, downloadImage, imageType, imagePath);
            drops.add(drop);
        }

        return drops.isEmpty() ? null : drops;
    }

    private Map<String, String> parseDropLine(String dropLine) {
        Map<String, String> dropData = new HashMap<>();
        String[] parts = dropLine.split("\\|");
        for (String part : parts) {
            String[] keyValue = part.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                // Remove any remaining curly braces or square brackets
                value = value.replaceAll("[{}\\[\\]]", "");
                dropData.put(key, value);
            }
        }
        return dropData;
    }

    private Drop getDropFromLine(Map<String, String> dropData, boolean downloadImage, ImageType imageType, String imagePath) {
        String itemName = dropData.get("name");
        ItemResult item = itemScraper.getItemsInfo(itemName, false, downloadImage, imageType, imagePath, true).get(itemName);

        List<Integer> quantity = parseQuantity(dropData.getOrDefault("quantity", "1"));
        double rarity = parseRarity(dropData.get("rarity"));

        return new Drop(item, quantity, rarity);
    }

    private List<Integer> parseQuantity(String quantityStr) {
        List<Integer> quantity = new ArrayList<>();
        if (quantityStr.contains("-")) {
            String[] parts = quantityStr.split("-");
            quantity.add(Integer.parseInt(parts[0].trim()));
            quantity.add(Integer.parseInt(parts[1].trim()));
        } else {
            int q = Integer.parseInt(quantityStr.trim());
            quantity.add(q);
            quantity.add(q);
        }
        return quantity;
    }

    private double parseRarity(String rarityStr) {
        if (rarityStr == null) return 0.0;
        if ("Always".equalsIgnoreCase(rarityStr)) {
            return 1.0;
        } else if (rarityStr.endsWith("%")) {
            return Double.parseDouble(rarityStr.substring(0, rarityStr.length() - 1)) / 100;
        } else {
            String[] parts = rarityStr.split("/");
            return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
        }
    }

    private void checkDictKeys(Map<String, NPCResult> dict, List<String> keys) {
        for (NPCResult npc : dict.values()) {
            for (String key : keys) {
                if (!hasProperty(npc, key)) {
                    System.out.println("Warning: Missing key in NPC result: " + key);
                }
            }
        }
    }

    private boolean hasProperty(Object obj, String propertyName) {
        try {
            obj.getClass().getDeclaredField(propertyName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    @Override
    protected String downloadAndSaveImage(String name, String imageUrl, ImageType imageType, String destination) {
        // Implement the actual image downloading and saving logic here
        // For now, we'll just return a placeholder string
        return destination + "/" + name + "_" + imageType.toString().toLowerCase() + ".png";
    }
}