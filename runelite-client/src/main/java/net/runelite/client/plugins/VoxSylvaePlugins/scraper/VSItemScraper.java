package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ItemResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ScraperResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ShopInfo;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.util.StringUtil;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class VSItemScraper extends VSWikiScraper<ItemResult> {
    private static final Logger logger = LoggerFactory.getLogger(VSItemScraper.class);
    private final Path itemDatabaseFile;
    private final List<String> itemKeys = Arrays.asList("names", "ids", "imagePaths", "noteable", "options", "stackable", "tradeable", "equipable", "examine", "highAlchValue", "gePrice", "shops", "locations", "droppedBy");
    private Map<String, ItemResult> itemDatabase;

    public VSItemScraper(Map<String, Object> databaseDict, Path destination, String databaseName, Path imageFolder, boolean resetDatabase) {
        super(destination, databaseName, imageFolder, resetDatabase);

        this.itemDatabaseFile = getDefaultDatabaseJson().resolveSibling("itemsDB.json");
        this.itemDatabase = loadDatabase(itemDatabaseFile, resetDatabase, ItemResult.class);
        databaseDict.put("items", this.itemDatabase);
    }

    @Override
    public void saveDatabases() {
        saveDatabase(itemDatabase, itemDatabaseFile);
    }

    public List<String> getItemKey(String itemNamesSearchString) {
        return StringUtil.formatArgs(itemNamesSearchString);
    }

    
    public Map<String, ItemResult> getItemsInfo(String itemNamesSearchString, boolean forceReload, boolean downloadImage, ImageType imageType, String imagePath, boolean withTradeInfo) {
        List<String> itemNames = getItemKey(itemNamesSearchString);
        Map<String, ItemResult> itemInfo = new HashMap<>();

        for (String itemName : itemNames) {
            logger.info("Attempting to retrieve info for item: {}", itemName);
            
            boolean downloadImageLoc = shouldDownloadImage(itemName, downloadImage);

            if ((itemDatabase.containsKey(itemName) || itemDatabase.containsKey(StringUtil.capitalizeEachWord(itemName)))
                    && !forceReload && !downloadImageLoc) {
                ItemResult cachedItem = itemDatabase.get(itemName);
                logger.info("Retrieved item from cache: {}", cachedItem);
                itemInfo.put(itemName, cachedItem);
                continue;
            }

            WikipediaPage page = getWikiPage(itemName);
            if (page == null) {
                logger.error("Failed to retrieve wiki page for item: {}", itemName);
                continue;
            }
            System.out.println("Page title: " + page.getTitle());
            System.out.println("Page content length: " + page.getContent().length());
            System.out.println("First 100 characters of content: " + page.getContent());
            logger.info("Successfully retrieved wiki page for item: {}", itemName);
            logger.debug("Page title: {}", page.getTitle());
            logger.debug("Page revision ID: {}", page.getRevisionId());
            logger.debug("Page content length: {}", page.getContent() != null ? page.getContent().length() : "null");
            if (page.getContent() != null && page.getContent().length() > 0) {
                logger.debug("First 100 characters of xx ---  page content: {}", page.getContent());
            } else {
                logger.warn("Page content is empty or null for item: {}", itemName);
            }
            ItemResult itemInfoWiki = getItemInfoFromWikiText(page, downloadImageLoc, imageType, imagePath);
            
            if (itemInfoWiki == null) {
                logger.error("Failed to parse item info from wiki text for item: {}", itemName);
                continue;
            }

            logger.info("Successfully parsed item info: {}", itemInfoWiki);
            itemInfo.put(itemName, itemInfoWiki);
            itemDatabase.put(itemName, itemInfoWiki);

            if (withTradeInfo) {
                updateTradeInfo(itemInfoWiki);
            }
        }

        return itemInfo;
    }

    private boolean shouldDownloadImage(String itemName, boolean downloadImage) {
        if (!downloadImage) return false;
        if (!itemDatabase.containsKey(itemName)) return true;
        
        ItemResult item = itemDatabase.get(itemName);
        return item.getImagePaths() == null || item.getImagePaths().stream().noneMatch(path -> Path.of(path).toFile().exists());
    }

    private ItemResult getItemInfoFromWikiText(WikipediaPage page, boolean downloadImage, ImageType imageType, String imagePath) {
        logger.info("Starting to parse item info from wiki text");
        ItemResult itemResult = new ItemResult();
        logger.info("Page title: {}", page.getTitle());
        logger.info("Page revision ID: {}", page.getRevisionId());
        logger.info("Page content length: {}", page.getContent() != null ? page.getContent().length() : "null");
        if (page.getContent() != null && page.getContent().length() > 0) {
            logger.info("First 100 characters of page content: {}", page.getContent().substring(0, Math.min(500, page.getContent().length())));
        } else {
            logger.warn("Page content is empty or null for item: {}", page.getTitle());
        }
        Map<String, String> infoboxData = parseInfobox(page.getContent(), "Infobox Item");
        logger.info("Parsed infobox data: {}", infoboxData);
        Map<String, String> DropSoruces = parseInfobox(page.getContent(), "Drop sources");
        logger.info("Parsed DropSoruces data: {}", DropSoruces);
        if (infoboxData.isEmpty()) {
            logger.error("Failed to parse infobox data from wiki text");
            return null;
        }
      
        itemResult.setShops(parseShops(page.getContent()));
        itemResult.setLocations(parseLocations(page.getContent()));
        itemResult.setDroppedBy(parseDroppedBy(page.getContent()));

        itemResult.setNames(Collections.singletonList(infoboxData.get("name")));
        itemResult.setIds(Collections.singletonList(parseIds(infoboxData.get("id"))));
        itemResult.setNoteable(Collections.singletonList(parseYesNoValue(infoboxData.get("noteable"))));
        itemResult.setOptions(Collections.singletonList(parseOptions(infoboxData.get("options"))));
        itemResult.setStackable(Collections.singletonList(parseYesNoValue(infoboxData.get("stackable"))));
        
        logger.info("Stackable: {}", infoboxData.get("stackable"));
        logger.info("Parsed Stackable: {}", itemResult.getStackable().get(0));
        
        itemResult.setTradeable(Collections.singletonList(parseYesNoValue(infoboxData.get("tradeable"))));
        
        logger.info("Tradeable: {}", infoboxData.get("tradeable"));
        logger.info("Parsed Tradeable: {}", itemResult.getTradeable().get(0));
        
        itemResult.setEquipable(Collections.singletonList(parseYesNoValue(infoboxData.get("equipable"))));
        itemResult.setExamine(Collections.singletonList(infoboxData.get("examine")));
        itemResult.setHighAlchValue(Collections.singletonList(parseIntOrDefault(infoboxData.get("highalch"), 0)));
    
        if (downloadImage) {
            List<String> imagePaths = downloadImagesFromTemplate(itemResult.getNames(), infoboxData.get("image"), imageType, imagePath);
            itemResult.setImagePaths(imagePaths);
        }
        
        logger.info("Finished parsing item info: {}", itemResult.toString());
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
    private boolean parseYesNoValue(String value) {
        if (value == null) {
            return false;
        }
        return value.trim().equalsIgnoreCase("Yes");
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

    private List<ShopInfo> parseShops(String wikiText) {
        List<ShopInfo> shops = new ArrayList<>();
        Pattern shopPattern = Pattern.compile("\\{\\{Store\\s*\\|([^}]+)\\}\\}");
        Matcher shopMatcher = shopPattern.matcher(wikiText);

        while (shopMatcher.find()) {
            String shopData = shopMatcher.group(1);
            Map<String, String> shopInfo = new HashMap<>();
            for (String pair : shopData.split("\\|")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    shopInfo.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            shops.add(new ShopInfo(
                shopInfo.get("name"),
                parseIntOrDefault(shopInfo.get("quantity"), 1),
                parseIntOrDefault(shopInfo.get("price"), 0)
            ));
        }
        return shops;
    }

    private List<String> parseLocations(String wikiText) {
        List<String> locations = new ArrayList<>();
        Pattern locationPattern = Pattern.compile("\\{\\{Location\\s*\\|([^}]+)\\}\\}");
        Matcher locationMatcher = locationPattern.matcher(wikiText);

        while (locationMatcher.find()) {
            String locationData = locationMatcher.group(1);
            String[] parts = locationData.split("\\|");
            if (parts.length > 0) {
                locations.add(parts[0].trim());
            }
        }
        return locations;
    }

    private List<String> parseDroppedBy(String wikiText) {
        List<String> droppedBy = new ArrayList<>();
        Pattern dropPattern = Pattern.compile("\\{\\{DropsLine\\s*\\|([^}]+)\\}\\}");
        Matcher dropMatcher = dropPattern.matcher(wikiText);

        while (dropMatcher.find()) {
            String dropData = dropMatcher.group(1);
            Map<String, String> dropInfo = new HashMap<>();
            for (String pair : dropData.split("\\|")) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    dropInfo.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }
            if (dropInfo.containsKey("name")) {
                droppedBy.add(dropInfo.get("name"));
            }
        }
        return droppedBy;
    }

    private void updateTradeInfo(ItemResult item) {
        // This method would fetch the latest GE price and trade volume
        // You'd need to implement the API call to the OSRS GE API here
        // For now, we'll just set a placeholder value
        //item.setGePrice(1000); // Placeholder value
    }

    private void checkDictKeys(Map<String, ItemResult> dict, List<String> keys) {
        for (ItemResult item : dict.values()) {
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

    private int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    protected String downloadAndSaveImage(String name, String imageUrl, ImageType imageType, String destination) {
        // Implement the actual image downloading and saving logic here
        // For now, we'll just return a placeholder string
        return destination + "/" + name + "_" + imageType.toString().toLowerCase() + ".png";
    }
}