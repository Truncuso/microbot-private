package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ScraperResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.util.StringUtil;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VSItemScraper extends VSWikiScraper<ScraperResult.ItemResult> {
    private final Path itemDatabaseFile;
    private final List<String> itemKeys = Arrays.asList("name", "ids", "imagePaths", "noteable", "options", "stackable", "tradeable", "equipable", "examine", "quantity", "rarity", "lootStatus");
    private Map<String, ScraperResult.ItemResult> itemDatabase;

    public VSItemScraper(Map<String, Object> databaseDict, Path destination, String databaseName, Path imageFolder, boolean resetDatabase) {
        super(destination, databaseName, imageFolder, resetDatabase);

        this.itemDatabaseFile = getDefaultDatabaseJson().resolveSibling("itemsDB.json");
        this.itemDatabase = loadDatabase(itemDatabaseFile, resetDatabase, ScraperResult.ItemResult.class);
        databaseDict.put("items", this.itemDatabase);
    }

    @Override
    public void saveDatabases() {
        saveDatabase(itemDatabase, itemDatabaseFile);
    }

    public List<String> getItemKey(String itemNamesSearchString) {
        return StringUtil.formatArgs(itemNamesSearchString);
    }

    public Map<String, ScraperResult.ItemResult> getItemsInfo(String itemNamesSearchString, boolean forceReload, boolean downloadImage, ImageType imageType, String imagePath, boolean withTradeInfo) {
        Map<String, Object> kwargs = new HashMap<>();
        kwargs.put("image_type", imageType);
        kwargs.put("destination", imagePath != null ? imagePath : getImageFolder().toString());

        List<String> itemNames = getItemKey(itemNamesSearchString);
        Map<String, ScraperResult.ItemResult> itemInfo = new HashMap<>();

        for (String itemName : itemNames) {
            boolean downloadImageLoc = shouldDownloadImage(itemName, downloadImage);

            if ((itemDatabase.containsKey(itemName) || itemDatabase.containsKey(StringUtil.capitalizeEachWord(itemName)))
                    && !forceReload && !downloadImageLoc) {
                itemInfo.put(itemName, itemDatabase.get(itemName));
                continue;
            }

            WikipediaPage page = getWikiPage(itemName);
            if (page == null) {
                throw new RuntimeException("<RuneMatio> " + itemName + ": Wiki Page doesn't exist and is not in DB");
            }

            ScraperResult.ItemResult itemInfoWiki = getItemInfoFromWikiText(page, downloadImageLoc, imageType, kwargs);
            itemInfo.put(itemName, itemInfoWiki);
            itemDatabase.put(itemName, itemInfoWiki);
        }

        checkDictKeys(itemInfo, itemKeys);
        return itemInfo;
    }

    private boolean shouldDownloadImage(String itemName, boolean downloadImage) {
        if (!downloadImage) return false;
        if (!itemDatabase.containsKey(itemName)) return true;
        
        ScraperResult.ItemResult item = itemDatabase.get(itemName);
        return item.getImagePaths() == null || item.getImagePaths().stream().noneMatch(path -> Path.of(path).toFile().exists());
    }

    private ScraperResult.ItemResult getItemInfoFromWikiText(WikipediaPage page, boolean downloadImage, ImageType imageType, Map<String, Object> kwargs) {
        ScraperResult.ItemResult itemResult = new ScraperResult.ItemResult();
        
        Map<String, String> infoboxData = parseInfobox(page.getContent(), "Item");
        
        itemResult.setName(infoboxData.get("name"));
        itemResult.setIds(parseIds(infoboxData.get("id")));
        itemResult.setNoteable(Boolean.parseBoolean(infoboxData.getOrDefault("noteable", "false")));
        itemResult.setOptions(parseOptions(infoboxData.get("options")));
        itemResult.setStackable(Boolean.parseBoolean(infoboxData.getOrDefault("stackable", "false")));
        itemResult.setTradeable(Boolean.parseBoolean(infoboxData.getOrDefault("tradeable", "false")));
        itemResult.setEquipable(Boolean.parseBoolean(infoboxData.getOrDefault("equipable", "false")));
        itemResult.setExamine(infoboxData.get("examine"));
        
        if (downloadImage) {
            List<String> imagePaths = downloadImagesFromTemplate(Collections.singletonList(itemResult.getName()), infoboxData.get("image"), imageType, kwargs);
            itemResult.setImagePaths(imagePaths);
        }
        
        return itemResult;
    }

    private List<Integer> parseIds(String idString) {
        List<Integer> ids = new ArrayList<>();
        if (idString != null) {
            for (String id : idString.split(",")) {
                try {
                    ids.add(Integer.parseInt(id.trim()));
                } catch (NumberFormatException e) {
                    System.out.println("Failed to parse ID: " + id);
                }
            }
        }
        return ids;
    }

    private List<String> parseOptions(String optionsString) {
        List<String> options = new ArrayList<>();
        if (optionsString != null) {
            for (String option : optionsString.split(",")) {
                options.add(option.trim());
            }
        }
        return options;
    }

    private void checkDictKeys(Map<String, ScraperResult.ItemResult> dict, List<String> keys) {
        for (ScraperResult.ItemResult item : dict.values()) {
            for (String key : keys) {
                if (!hasProperty(item, key)) {
                    throw new IllegalStateException("Missing key in item result: " + key);
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

    private List<String> downloadImagesFromTemplate(List<String> names, String imageData, ImageType imageType, Map<String, Object> kwargs) {
        List<String> imagePaths = new ArrayList<>();
        for (String name : names) {
            String imageUrl = extractImageUrl(imageData);
            if (imageUrl != null) {
                String imagePath = downloadAndSaveImage(name, imageUrl, imageType, (String) kwargs.get("destination"));
                imagePaths.add(imagePath);
            }
        }
        return imagePaths;
    }

    private String extractImageUrl(String imageData) {
        Pattern pattern = Pattern.compile("\\[\\[File:(.*?)\\]\\]");
        Matcher matcher = pattern.matcher(imageData);
        if (matcher.find()) {
            return baseUrl + "images/" + matcher.group(1);
        }
        return null;
    }
}