package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ScraperResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.util.StringUtil;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VSNPCScraper extends VSWikiScraper<ScraperResult.NPCResult> {

    private final Path npcDatabaseFile;
    private final List<String> npcKeys = Arrays.asList("name", "id", "imagePaths");
    private final List<String> monsterKeys = Arrays.asList("names", "drops", "ids", "imagePaths");
    private final List<String> dropKeys = Arrays.asList("names", "quantity", "rarity", "ids", "imagePaths", "lootStatus");
    private Map<String, ScraperResult.NPCResult> npcDatabase;

    public VSNPCScraper(Map<String, Object> databaseDict, Path destination, String databaseName, Path imageFolder, boolean resetDatabase) {
        super(destination, databaseName, imageFolder, resetDatabase);
        

        

        this.npcDatabaseFile = getDefaultDatabaseJson().resolveSibling("npcDB.json");
        this.npcDatabase = loadDatabase(npcDatabaseFile, resetDatabase, ScraperResult.NPCResult.class);
        databaseDict.put("npcs", this.npcDatabase);
    }

    @Override
    public void saveDatabases() throws IOException {
        saveDatabase(npcDatabase, npcDatabaseFile);
    }

    public Map<String, ScraperResult.NPCResult> getNPCInfo(String npcNamesSearchString, boolean forceReload, boolean downloadImage, boolean saveDatabase, ImageType imageType, String imagePath) {
        List<String> npcNames = StringUtil.formatArgs(npcNamesSearchString);
        Map<String, ScraperResult.NPCResult> npcInfo = new HashMap<>();
        boolean isMonster = false;

        for (String npcName : npcNames) {
            if ((npcDatabase.containsKey(npcName) || npcDatabase.containsKey(StringUtil.capitalizeEachWord(npcName)))
                    && !forceReload && !downloadImage) {
                ScraperResult.NPCResult result = npcDatabase.get(npcName);
                npcInfo.put(npcName, result);
                if (result.getDrops() != null && !result.getDrops().isEmpty()) {
                    isMonster = true;
                }
                continue;
            }

            WikipediaPage page = getWikiPage(npcName);
            if (page == null) {
                throw new RuntimeException("<RuneMatio> " + npcName + ": Wiki Page doesn't exist and is not in DB");
            }

            ScraperResult.NPCResult npcInfoWiki = getNPCInfoFromWikiText(page, downloadImage, imageType, imagePath);
            npcInfo.put(npcName, npcInfoWiki);
            npcDatabase.put(npcName, npcInfoWiki);

            if (npcInfoWiki.getDrops() != null && !npcInfoWiki.getDrops().isEmpty()) {
                isMonster = true;
            }
        }

        checkDictKeys(npcInfo, isMonster ? monsterKeys : npcKeys);
        try {
            // Save database
            if (saveDatabase) {
                saveDatabases();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return npcInfo;
    }

    private ScraperResult.NPCResult getNPCInfoFromWikiText(WikipediaPage page, boolean downloadImage, ImageType imageType, String imagePath) {
        ScraperResult.NPCResult npcResult = new ScraperResult.NPCResult();
        Map<String, String> infoboxData = parseInfobox(page.getContent(), "NPC");
        Map<String, ScraperResult.ItemResult> npcDrops = new HashMap<>();

        if (infoboxData.containsKey("name")) {
            npcResult.setName(infoboxData.get("name"));
            npcResult.setId(Integer.parseInt(infoboxData.getOrDefault("id", "-1")));
            if (downloadImage) {
                List<String> imagePaths = downloadImagesFromTemplate(Collections.singletonList(npcResult.getName()), infoboxData.get("image"), imageType, imagePath);
                npcResult.setImagePaths(imagePaths);
            }
        } else {
            // Handle monster infobox
            npcResult = getInfoboxMonster(infoboxData, downloadImage, imageType, imagePath);
        }

        // Parse drops
        List<Map<String, String>> dropsData = parseDropsLines(page.getContent());
        for (Map<String, String> dropData : dropsData) {
            ScraperResult.ItemResult drop = getDropFromLine(dropData, downloadImage, imageType, imagePath);
            npcDrops.put(drop.getName(), drop);
        }

        npcResult.setDrops(new ArrayList<>(npcDrops.values()));
        return npcResult;
    }

    private ScraperResult.NPCResult getInfoboxMonster(Map<String, String> infoboxData, boolean downloadImage, ImageType imageType, String imagePath) {
        ScraperResult.NPCResult monsterResult = new ScraperResult.NPCResult();

        List<String> names = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();

        if (infoboxData.containsKey("version1")) {
            int version = 1;
            while (infoboxData.containsKey("version" + version)) {
                names.add(infoboxData.get("name" + version));
                ids.add(Integer.parseInt(infoboxData.getOrDefault("id" + version, "-1")));
                version++;
            }
        } else {
            names.add(infoboxData.get("name"));
            ids.add(Integer.parseInt(infoboxData.getOrDefault("id", "-1")));
        }

        monsterResult.setNames(names);
        monsterResult.setIds(ids);

        if (downloadImage) {
            List<String> imagePaths = downloadImagesFromTemplate(names, infoboxData.get("image"), imageType, imagePath);
            monsterResult.setImagePaths(imagePaths);
        }

        return monsterResult;
    }

    private ScraperResult.ItemResult getDropFromLine(Map<String, String> dropData, boolean downloadImage, ImageType imageType, String imagePath) {
        ScraperResult.ItemResult drop = new ScraperResult.ItemResult();
        drop.setName(dropData.get("name"));

        String quantityStr = dropData.getOrDefault("quantity", "1");
        List<Integer> quantity = parseQuantity(quantityStr);
        drop.setQuantity(quantity);

        String rarityStr = dropData.get("rarity");
        double rarity = parseRarity(rarityStr);
        drop.setRarity(rarity);

        drop.setLootStatus(false);

        // Here you would typically fetch more item info, similar to how the Python version calls get_itemsInfo
        // For simplicity, we'll just set the basic info we have
        drop.setIds(Collections.singletonList(Integer.parseInt(dropData.getOrDefault("id", "-1"))));

        if (downloadImage) {
            List<String> imagePaths = downloadImagesFromTemplate(Collections.singletonList(drop.getName()), dropData.get("image"), imageType, imagePath);
            drop.setImagePaths(imagePaths);
        }

        return drop;
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
        if ("Always".equalsIgnoreCase(rarityStr)) {
            return 1.0;
        } else if (rarityStr.endsWith("%")) {
            return Double.parseDouble(rarityStr.substring(0, rarityStr.length() - 1)) / 100;
        } else {
            String[] parts = rarityStr.split("/");
            return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
        }
    }

    private List<Map<String, String>> parseDropsLines(String wikiText) {
        List<Map<String, String>> drops = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{DropsLine\\|([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(wikiText);

        while (matcher.find()) {
            String dropLine = matcher.group(1);
            Map<String, String> dropData = new HashMap<>();
            String[] parts = dropLine.split("\\|");
            for (String part : parts) {
                String[] keyValue = part.split("=");
                if (keyValue.length == 2) {
                    dropData.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            drops.add(dropData);
        }

        return drops;
    }

    private void checkDictKeys(Map<String, ScraperResult.NPCResult> dict, List<String> keys) {
        for (ScraperResult.NPCResult npc : dict.values()) {
            for (String key : keys) {
                if (!hasProperty(npc, key)) {
                    throw new IllegalStateException("Missing key in NPC result: " + key);
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
     // Implement the abstract method from VSWikiScraper
     @Override
     protected String downloadAndSaveImage(String name, String imageUrl, ImageType imageType, String destination) {
         // Implement the actual image downloading and saving logic here
         // For now, we'll just return a placeholder string
         return destination + "/" + name + "_" + imageType.toString().toLowerCase() + ".png";
     }
}