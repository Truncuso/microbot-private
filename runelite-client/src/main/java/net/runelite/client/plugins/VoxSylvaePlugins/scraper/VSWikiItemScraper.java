package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.CombatStats;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.ItemSources;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.WikiItemResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.util.StringUtil;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VSWikiItemScraper extends VSWikiScraper<WikiItemResult> {
    private static final Logger logger = LoggerFactory.getLogger(VSWikiItemScraper.class);
    private final Path itemDatabaseFile;
    private Map<String, WikiItemResult> itemDatabase;

    public VSWikiItemScraper(Map<String, Object> databaseDict, Path destination, String databaseName, Path imageFolder, boolean resetDatabase) {
        super(destination, databaseName, imageFolder, resetDatabase);

        this.itemDatabaseFile = getDefaultDatabaseJson().resolveSibling("itemsDB.json");
        this.itemDatabase = loadDatabase(itemDatabaseFile, resetDatabase, WikiItemResult.class);
        databaseDict.put("items", this.itemDatabase);
    }

    @Override
    public void saveDatabases() {
        saveDatabase(itemDatabase, itemDatabaseFile);
    }

    public Map<String, WikiItemResult> getItemsInfo(String itemNamesSearchString, boolean forceReload, boolean downloadImage, ImageType imageType, String imagePath, boolean withTradeInfo) {
        List<String> itemNames = StringUtil.formatArgs(itemNamesSearchString);
        Map<String, WikiItemResult> itemInfo = new HashMap<>();

        for (String itemName : itemNames) {
            logger.info("Attempting to retrieve info for item: {}", itemName);
            
            String normalizedItemName = StringUtil.normalizeSearchName(itemName);
            
            if (!forceReload && itemDatabase.containsKey(normalizedItemName)) {
                WikiItemResult cachedItem = itemDatabase.get(normalizedItemName);
                logger.info("Retrieved item id {} version from cache for: {}", cachedItem.getId(), itemName);
                itemInfo.put(normalizedItemName, cachedItem);
                continue;
            }

            WikipediaPage page = getWikiPage(itemName);
            if (page == null) {
                logger.error("Failed to retrieve wiki page for item: {}", itemName);
                continue;
            }
          
            logger.info("Successfully retrieved wiki page for item: {}", itemName);
            List<WikiItemResult> itemInfoSWiki = getItemInfoFromWikiText(page, downloadImage, imageType, imagePath);
            
            if (itemInfoSWiki.isEmpty()) {
                logger.error("Failed to parse item info from wiki text for item: {}", itemName);
                continue;
            }
            logger.info("Successfully parsed {} item versions for: {}", itemInfoSWiki.size(), itemName);
            boolean oneItemMustBeTheSearch =false;
            for (WikiItemResult itemInfoWikiResult : itemInfoSWiki) {
                logger.info("Adding item: {} to item database", itemInfoWikiResult.getName());
               
                String normalizedItemName_ii = StringUtil.normalizeSearchName( itemInfoWikiResult.getName());
                //not in set
                if (normalizedItemName_ii.equals(normalizedItemName) ){
                    oneItemMustBeTheSearch = true;
                }
                assert(!itemInfo.containsKey(normalizedItemName_ii));
                assert(!itemDatabase.containsKey(normalizedItemName_ii));
                                
                itemInfo.put(normalizedItemName_ii, itemInfoWikiResult);
                itemDatabase.put(normalizedItemName_ii, itemInfoWikiResult);
    
            }
            assert(oneItemMustBeTheSearch);
            

            if (withTradeInfo) {
                for (WikiItemResult item : itemInfoSWiki) {
                    updateTradeInfo(item);
                }
            }
        }

        return itemInfo;
    }

    private List<WikiItemResult> getItemInfoFromWikiText(WikipediaPage page, boolean downloadImage, ImageType imageType, String imagePath) {
        
        List<WikiItemResult> items = new ArrayList<>();
        Map<String, List<String>> infoboxData = parseInfobox(page.getContent(), "Infobox Item");
        Map<String, List<String>> combatStatsData = parseInfobox(page.getContent(), "Infobox Bonuses");

        int versionCount = getVersionCount(infoboxData);
        
        for (int i = 1; i <= versionCount; i++) {
            WikiItemResult item = new WikiItemResult();
            
            item.setName(getVersionedValue(infoboxData, "name", i));
            item.setId(parseIntOrDefault(getVersionedValue(infoboxData, "id", i), -1));
            item.setNoteable(parseYesNoValue(infoboxData.get("noteable")));
            item.setOptions(parseOptions(infoboxData.get("options")));
            item.setStackable(parseYesNoValue(infoboxData.get("stackable")));
            item.setTradeable(parseYesNoValue(infoboxData.get("tradeable")));
            item.setEquipable(parseYesNoValue(infoboxData.get("equipable")));
            item.setMembers(parseYesNoValue(getVersionedValue(infoboxData, "members", i)));
            item.setExamine(getVersionedValue(infoboxData, "examine", i));
            item.setValue( parseIntOrDefault(getVersionedValue(infoboxData, "value", i), 0));
            item.setHighAlchValue(parseIntOrDefault(getVersionedValue(infoboxData, "highalch", i), 0));
            item.setWeight(parseDoubleOrDefault(infoboxData.get("weight"), 0.0));
            
            if (downloadImage) {
                String imagePathNew = downloadImageFromTemplate(item.getName(), getVersionedValue(infoboxData, "image", i), imageType, imagePath);
                item.setImagePath(imagePathNew);
            }
            if (item.isEquipable() && !combatStatsData.isEmpty()) {
                item.setCombatStats(parseCombatStats(combatStatsData));
            }
             ItemSources sources = new ItemSources();
            
            sources.setSpawnLocations(parseSpawnLocations(page.getContent()));
            sources.setShopSources(parseShopSources(page.getContent(),item.getName()));
            sources.setDropSources(parseDropSources(page.getContent(), item.getName()));
            item.setItemSources(sources);
            items.add(item);
        }
        
        return items;
    }

    private CombatStats parseCombatStats(Map<String, List<String>> combatStatsData) {
        CombatStats stats = new CombatStats();
        
        stats.setAstab(parseIntOrDefault(combatStatsData.get("astab"), 0));
        stats.setAslash(parseIntOrDefault(combatStatsData.get("aslash"), 0));
        stats.setAcrush(parseIntOrDefault(combatStatsData.get("acrush"), 0));
        stats.setAmagic(parseIntOrDefault(combatStatsData.get("amagic"), 0));
        stats.setArange(parseIntOrDefault(combatStatsData.get("arange"), 0));
        stats.setDstab(parseIntOrDefault(combatStatsData.get("dstab"), 0));
        stats.setDslash(parseIntOrDefault(combatStatsData.get("dslash"), 0));
        stats.setDcrush(parseIntOrDefault(combatStatsData.get("dcrush"), 0));
        stats.setDmagic(parseIntOrDefault(combatStatsData.get("dmagic"), 0));
        stats.setDrange(parseIntOrDefault(combatStatsData.get("drange"), 0));
        stats.setStr(parseIntOrDefault(combatStatsData.get("str"), 0));
        stats.setRstr(parseIntOrDefault(combatStatsData.get("rstr"), 0));
        stats.setMdmg(parseIntOrDefault(combatStatsData.get("mdmg"), 0));
        stats.setPrayer(parseIntOrDefault(combatStatsData.get("prayer"), 0));
        stats.setSlot(getFirstOrDefault(combatStatsData.get("slot"), ""));
        stats.setSpeed(parseIntOrDefault(combatStatsData.get("speed"), 0));
        stats.setAttackRange(parseIntOrDefault(combatStatsData.get("attackrange"), 0));
        stats.setCombatStyle(getFirstOrDefault(combatStatsData.get("combatstyle"), ""));
        stats.setImage(getFirstOrDefault(combatStatsData.get("image"), ""));
        stats.setAltImage(getFirstOrDefault(combatStatsData.get("altimage"), ""));
        
        return stats;
    }

    private int parseIntOrDefault(List<String> values, int defaultValue) {
        if (values == null || values.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(values.get(0).replaceAll("[^\\d-]", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String getFirstOrDefault(List<String> values, String defaultValue) {
        return (values != null && !values.isEmpty()) ? values.get(0) : defaultValue;
    }

    private boolean parseYesNoValue(List<String> values) {
        if (values == null || values.isEmpty()) {
            return false;
        }
        return values.get(0).trim().equalsIgnoreCase("Yes");
    }

   

    

    private double parseDoubleOrDefault(List<String> values, double defaultValue) {
        if (values == null || values.isEmpty()) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(values.get(0));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    

    private void updateTradeInfo(WikiItemResult item) {
        // Implement trade info update logic here
    }

    @Override
    protected String downloadAndSaveImage(String name, String imageUrl, ImageType imageType, String destination) {
        // Implement image downloading logic here
        return destination + "/" + name + "_" + imageType.toString().toLowerCase() + ".png";
    }
}