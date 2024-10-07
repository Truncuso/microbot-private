package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.WikiItemResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiNPCInfo.WikiMonsterDrop;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiNPCInfo.WikiNPCLocation;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiNPCInfo.WikiNPCResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.util.StringUtil;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VSNPCScraper extends VSWikiScraper<WikiNPCResult> {
    private static final Logger logger = LoggerFactory.getLogger(VSNPCScraper.class);
    private final Path npcDatabaseFile;
    private final List<String> npcKeys = Arrays.asList("name", "version", "ids", "imagePath", "examine", "combatLevel", "hitpoints", "aggressive", "poisonous", "attributes", "locations", "drops");
    private Map<String, Map<String, WikiNPCResult>> npcDatabase;
    private VSWikiItemScraper itemScraper;

    public VSNPCScraper(Map<String, Object> databaseDict, Path destination, String databaseName, Path imageFolder, boolean resetDatabase) {
        super(destination, databaseName, imageFolder, resetDatabase);
        this.npcDatabaseFile = getDefaultDatabaseJson().resolveSibling("npcDB.json");
        this.npcDatabase = loadDatabaseNested(npcDatabaseFile, resetDatabase, WikiNPCResult.class);
        databaseDict.put("npcs", this.npcDatabase);
        this.itemScraper = new VSWikiItemScraper(databaseDict, destination, databaseName, imageFolder, resetDatabase);
    }

    @Override
    public void saveDatabases() throws IOException {
        saveDatabaseNested(npcDatabase, npcDatabaseFile);
        itemScraper.saveDatabases();
    }
    public Map<String, Map<String, WikiNPCResult>> getNPCInfo(String npcNamesSearchString, boolean forceReload, boolean downloadImage, boolean saveDatabase, ImageType imageType, String imagePath) {
        List<String> npcNames = StringUtil.formatArgs(npcNamesSearchString);
        Map<String, Map<String, WikiNPCResult>> npcInfo = new HashMap<>();

        for (String npcName : npcNames) {
            logger.info("Attempting to retrieve info for npc: {}", npcName);
            
            String normalizedNPCName = StringUtil.normalizeSearchName(npcName);
            if (!forceReload && npcDatabase.containsKey(npcName)) {
                Map<String, WikiNPCResult> cachedNPC = npcDatabase.get(npcName);
                for (Map.Entry<String, WikiNPCResult> entry : cachedNPC.entrySet()) {
                    logger.info("Retrieved npc id {} version from cache for: {}", entry.getValue().getIds(), entry.getKey());
                }                
                npcInfo.put(npcName, npcDatabase.get(npcName));
                continue;
            }

            WikipediaPage page = getWikiPage(npcName);
            if (page == null) {
                throw new RuntimeException("<RuneMatio> " + npcName + ": Wiki Page doesn't exist and is not in DB");
            }
            if (page == null) {
                logger.error("Failed to retrieve wiki page for item: {}", npcName);
                continue;
            }
            Map<String, WikiNPCResult> npcInfoWiki = getNPCInfoFromWikiText(page, downloadImage, imageType, imagePath);
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
      private Map<String, WikiNPCResult> getNPCInfoFromWikiText(WikipediaPage page, boolean downloadImage, ImageType imageType, String imagePath) {
        Map<String, WikiNPCResult> npcResults = new HashMap<>();
        Map<String, List<String>> infoboxData = parseInfobox(page.getContent(), "Infobox Monster");
        boolean isMonster = !infoboxData.isEmpty();
        
        if (!isMonster) {
            infoboxData = parseInfobox(page.getContent(), "Infobox NPC");
        }

        if (infoboxData.isEmpty()) {
            logger.error("Failed to parse infobox data for NPC/Monster: {}", page.getTitle());
            return npcResults;
        }

        int versionCount = getVersionCount(infoboxData);
        String baseName = infoboxData.get("name").get(0);

        for (int i = 1; i <= versionCount; i++) {
            WikiNPCResult npcResult = new WikiNPCResult();
            String version = getVersionedValue(infoboxData, "version", i);
            
            npcResult.setName(baseName);
            npcResult.setVersion(version == null ? "Standard" : version);
            npcResult.setIds(parseIds(getVersionedValue(infoboxData, "id", i)));
            npcResult.setExamine(getVersionedValue(infoboxData, "examine", i));
            npcResult.setCombatLevel(parseIntOrDefault(getVersionedValue(infoboxData, "combat", i), 0));
            npcResult.setHitpoints(parseIntOrDefault(getVersionedValue(infoboxData, "hitpoints", i), 0));
            npcResult.setAggressive(parseYesNoValue(getVersionedValue(infoboxData, "aggressive", i)));
            npcResult.setPoisonous(parseYesNoValue(getVersionedValue(infoboxData, "poisonous", i)));
            npcResult.setAttributes(parseAttributesNPCS(getVersionedValue(infoboxData, "attributes", i)));
            npcResult.setLocations(parseLocations(page.getContent(), isMonster));
            npcResult.setOptions(parseOptions(infoboxData.get("options")));
            if (downloadImage) {
                String imagePathResult = downloadImageFromTemplate(npcResult.getName(), getVersionedValue(infoboxData, "image", i), imageType, imagePath);
                npcResult.setImagePath(imagePathResult);
            }

            if (isMonster) {
                npcResult.setDrops(parseDrops(page.getContent(), downloadImage, imageType, imagePath));
            }

            // Parse additional fields
            npcResult.setAttackLevel(parseIntOrDefault(getVersionedValue(infoboxData, "att", i), 0));
            npcResult.setStrengthLevel(parseIntOrDefault(getVersionedValue(infoboxData, "str", i), 0));
            npcResult.setDefenceLevel(parseIntOrDefault(getVersionedValue(infoboxData, "def", i), 0));
            npcResult.setMagicLevel(parseIntOrDefault(getVersionedValue(infoboxData, "mage", i), 0));
            npcResult.setRangedLevel(parseIntOrDefault(getVersionedValue(infoboxData, "range", i), 0));
            npcResult.setAttackSpeed(parseIntOrDefault(getVersionedValue(infoboxData, "attack speed", i), 0));
            npcResult.setAttackStyle(getVersionedValue(infoboxData, "attack style", i));
            npcResult.setMaxHit(parseIntOrDefault(getVersionedValue(infoboxData, "max hit", i), 0));
            npcResult.setMembers(parseYesNoValue(getVersionedValue(infoboxData, "members", i)));
            npcResult.setSize(getVersionedValue(infoboxData, "size", i));
            npcResult.setRace(getVersionedValue(infoboxData, "race", i));
            npcResult.setReleaseDate(getVersionedValue(infoboxData, "release", i));
            npcResult.setUpdateName(getVersionedValue(infoboxData, "update", i));

            // Monster-specific fields
            if (isMonster) {
                npcResult.setXpBonus(parseIntOrDefault(getVersionedValue(infoboxData, "xpbonus", i), 0));
                npcResult.setAttackBonus(parseIntOrDefault(getVersionedValue(infoboxData, "attbns", i), 0));
                npcResult.setStrengthBonus(parseIntOrDefault(getVersionedValue(infoboxData, "strbns", i), 0));
                npcResult.setMagicAttackBonus(parseIntOrDefault(getVersionedValue(infoboxData, "amagic", i), 0));
                npcResult.setMagicStrengthBonus(parseIntOrDefault(getVersionedValue(infoboxData, "mbns", i), 0));
                npcResult.setRangedAttackBonus(parseIntOrDefault(getVersionedValue(infoboxData, "arange", i), 0));
                npcResult.setRangedStrengthBonus(parseIntOrDefault(getVersionedValue(infoboxData, "rngbns", i), 0));
                npcResult.setStabDefence(parseIntOrDefault(getVersionedValue(infoboxData, "dstab", i), 0));
                npcResult.setSlashDefence(parseIntOrDefault(getVersionedValue(infoboxData, "dslash", i), 0));
                npcResult.setCrushDefence(parseIntOrDefault(getVersionedValue(infoboxData, "dcrush", i), 0));
                npcResult.setMagicDefence(parseIntOrDefault(getVersionedValue(infoboxData, "dmagic", i), 0));
                npcResult.setRangedDefence(parseIntOrDefault(getVersionedValue(infoboxData, "drange", i), 0));
                npcResult.setImmunePoison(parseYesNoValue(getVersionedValue(infoboxData, "immunepoison", i)));
                npcResult.setImmuneVenom(parseYesNoValue(getVersionedValue(infoboxData, "immunevenom", i)));
                npcResult.setImmuneCannon(parseYesNoValue(getVersionedValue(infoboxData, "immunecannon", i)));
                npcResult.setImmuneThrall(parseYesNoValue(getVersionedValue(infoboxData, "immunethrall", i)));
                npcResult.setRespawnTime(parseIntOrDefault(getVersionedValue(infoboxData, "respawn", i), 0));
                npcResult.setSlayerLevel(parseIntOrDefault(getVersionedValue(infoboxData, "slaylvl", i), 0));
                npcResult.setSlayerXp(parseIntOrDefault(getVersionedValue(infoboxData, "slayxp", i), 0));
            }

            npcResults.put(npcResult.getVersion(), npcResult);
        }

        return npcResults;
    }

    private List<WikiNPCLocation> parseLocations(String wikiText, boolean isMonster) {
        List<WikiNPCLocation> locations = new ArrayList<>();
        Pattern pattern = isMonster ? 
            Pattern.compile("\\{\\{LocLine([^}]+)\\}\\}", Pattern.MULTILINE) :
            Pattern.compile("\\{\\{Map([^}]+)\\}\\}", Pattern.MULTILINE);
        
        Matcher matcher = pattern.matcher(wikiText);
        
        while (matcher.find()) {
            WikiNPCLocation location = new WikiNPCLocation();
            String locData = matcher.group(1);
            
            if (isMonster) {
                parseMonsterLocation(location, locData);
            } else {
                parseNPCLocation(location, locData);
            }
            
            locations.add(location);
        }
        
        return locations.isEmpty() ? null : locations;
    }

    private void parseMonsterLocation(WikiNPCLocation location, String locData) {
        Map<String, String> locMap = parseKeyValuePairs(locData);
        
        location.setName(locMap.get("name"));
        location.setLocation(locMap.get("location"));
        location.setLevels(parseIntList(locMap.get("levels")));
        location.setMembers(parseYesNoValue(locMap.get("members")));
        location.setMapID(parseIntOrDefault(locMap.get("mapID"), -1));
        location.setPlane(parseIntOrDefault(locMap.get("plane"), 0));
        location.setCoordinates(parseCoordinates(locMap));
        location.setMtype(locMap.get("mtype"));
    }
    private void parseNPCLocation(WikiNPCLocation location, String locData) {
        Map<String, String> locMap = parseKeyValuePairs(locData);
        
        location.setName(locMap.get("name"));
        location.setMapID(parseIntOrDefault(locMap.get("mapID"), -1));
        location.setPlane(parseIntOrDefault(locMap.get("plane"), 0));
        location.setCoordinates(parseCoordinates(locMap));
        location.setMtype(locMap.get("mtype"));
    }

    private Map<String, String> parseKeyValuePairs(String data) {
        Map<String, String> result = new HashMap<>();
        Pattern pattern = Pattern.compile("(\\w+)\\s*=\\s*([^|]+)");
        Matcher matcher = pattern.matcher(data);
        while (matcher.find()) {
            result.put(matcher.group(1).trim(), matcher.group(2).trim());
        }
        return result;
    }

    private List<WikiNPCLocation.Coordinate> parseCoordinates(Map<String, String> locMap) {
        List<WikiNPCLocation.Coordinate> coordinates = new ArrayList<>();
        for (Map.Entry<String, String> entry : locMap.entrySet()) {
            if (entry.getKey().startsWith("x")) {
                String[] parts = entry.getValue().split(",");
                if (parts.length == 2) {
                    int x = Integer.parseInt(parts[0].trim());
                    int y = Integer.parseInt(parts[1].split(":")[1].trim());
                    coordinates.add(new WikiNPCLocation.Coordinate(x, y));
                }
            }
        }
        return coordinates;
    }

    private List<Integer> parseIntList(String value) {
        if (value == null || value.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> result = new ArrayList<>();
        for (String part : value.split(",")) {
            try {
                result.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse integer: {}", part);
            }
        }
        return result;
    }
    protected Map<String, List<WikiMonsterDrop>> parseDrops(String wikiText, boolean downloadImage, ImageType imageType, String imagePath) {
        Map<String, List<WikiMonsterDrop>> dropCategories = new HashMap<>();
        
        // Parse average drop value (if present)
        parseAverageDropValue(wikiText, dropCategories);

        // Parse all drop categories dynamically
        Pattern categoryPattern = Pattern.compile("===(.*?)===\\s*\\{\\{DropsTableHead.*?\\}\\}(.*?)\\{\\{DropsTableBottom\\}\\}", Pattern.DOTALL);
        Matcher categoryMatcher = categoryPattern.matcher(wikiText);

        while (categoryMatcher.find()) {
            String category = categoryMatcher.group(1).trim();
            String categoryContent = categoryMatcher.group(2);
            
            List<WikiMonsterDrop> drops = parseCategoryDrops(categoryContent, category, downloadImage, imageType, imagePath);
            dropCategories.put(category, drops);
        }

        // Handle special cases like HerbDropTable
        parseSpecialDropTables(wikiText, dropCategories, downloadImage, imageType, imagePath);

        return dropCategories;
    }

    private void parseAverageDropValue(String wikiText, Map<String, List<WikiMonsterDrop>> dropCategories) {
        Pattern pattern = Pattern.compile("\\{\\{Average drop value.*?\\}\\}");
        Matcher matcher = pattern.matcher(wikiText);
        if (matcher.find()) {
            // For now, we'll just add a placeholder. You can implement actual parsing logic later if needed.
            dropCategories.put("Average Drop Value", new ArrayList<>());
            logger.info("Average drop value found but not parsed");
        }
    }

    private List<WikiMonsterDrop> parseCategoryDrops(String categoryContent, String category, boolean downloadImage, ImageType imageType, String imagePath) {
        List<WikiMonsterDrop> drops = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{DropsLine\\|([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(categoryContent);

        while (matcher.find()) {
            Map<String, String> dropData = parseDropLine(matcher.group(1));
            WikiMonsterDrop drop = getDropFromLine(dropData, category, downloadImage, imageType, imagePath);
            drops.add(drop);
        }

        return drops;
    }

    private void parseSpecialDropTables(String wikiText, Map<String, List<WikiMonsterDrop>> dropCategories, boolean downloadImage, ImageType imageType, String imagePath) {
        parseHerbDropTable(wikiText, dropCategories, downloadImage, imageType, imagePath);
        parseRareDropTable(wikiText, dropCategories, downloadImage, imageType, imagePath);
        // Add more special drop table parsing methods here as needed
    }

    private void parseHerbDropTable(String wikiText, Map<String, List<WikiMonsterDrop>> dropCategories, boolean downloadImage, ImageType imageType, String imagePath) {
        Pattern herbPattern = Pattern.compile("\\{\\{HerbDropTable\\|([^}]+)\\}\\}");
        Matcher herbMatcher = herbPattern.matcher(wikiText);
        if (herbMatcher.find()) {
            String herbRarity = herbMatcher.group(1);
            List<WikiMonsterDrop> herbDrops = parseHerbDrops(herbRarity, downloadImage, imageType, imagePath);
            dropCategories.put("Herbs", herbDrops);
            logger.info("Parsed HerbDropTable with rarity: {}", herbRarity);
        }
    }

    private List<WikiMonsterDrop> parseHerbDrops(String herbRarity, boolean downloadImage, ImageType imageType, String imagePath) {
        List<WikiMonsterDrop> herbDrops = new ArrayList<>();
        String[] herbs = {"Guam leaf", "Marrentill", "Tarromin", "Harralander", "Ranarr weed", "Toadflax", "Irit leaf", "Avantoe", "Kwuarm", "Snapdragon", "Cadantine", "Lantadyme", "Dwarf weed", "Torstol"};
        double baseRarity = parseRarity(herbRarity);

        for (String herb : herbs) {
            WikiItemResult item = itemScraper.getItemsInfo(herb, false, downloadImage, imageType, imagePath, true).get(herb);
            WikiMonsterDrop herbDrop = new WikiMonsterDrop(item, Arrays.asList(1), baseRarity / herbs.length);
            herbDrop.setMembersOnly(true);
            herbDrop.setCategory("Herbs");
            herbDrops.add(herbDrop);
        }

        return herbDrops;
    }

    private void parseRareDropTable(String wikiText, Map<String, List<WikiMonsterDrop>> dropCategories, boolean downloadImage, ImageType imageType, String imagePath) {
        Pattern rareDropPattern = Pattern.compile("\\{\\{RareDropTable\\|([^}]+)\\}\\}");
        Matcher rareDropMatcher = rareDropPattern.matcher(wikiText);
        if (rareDropMatcher.find()) {
            String rareDropParams = rareDropMatcher.group(1);
            // Implement parsing logic for RareDropTable here
            logger.info("RareDropTable found with params: {}", rareDropParams);
        }
    }

    protected WikiMonsterDrop getDropFromLine(Map<String, String> dropData, String category, boolean downloadImage, ImageType imageType, String imagePath) {
        String itemName = dropData.get("name");
        WikiItemResult item = itemScraper.getItemsInfo(itemName, false, downloadImage, imageType, imagePath, true).get(itemName);

        List<Integer> quantity = parseQuantity(dropData.getOrDefault("quantity", "1"));
        double rarity = parseRarity(dropData.get("rarity"));

        WikiMonsterDrop drop = new WikiMonsterDrop(item, quantity, rarity);
        drop.setCategory(category);
        drop.setMembersOnly(dropData.containsKey("namenotes") && dropData.get("namenotes").contains("(m)"));
        drop.setFreeToPlay(dropData.containsKey("namenotes") && dropData.get("namenotes").contains("(f)"));
        drop.setNotes(dropData.get("raritynotes"));
        drop.setAltRarity(dropData.get("altrarity"));

        logger.debug("Parsed drop: {} in category: {}", itemName, category);
        return drop;
    }
   
  
    


   

   


    @Override
    protected String downloadAndSaveImage(String name, String imageUrl, ImageType imageType, String destination) {
        // Implement the actual image downloading and saving logic here
        // For now, we'll just return a placeholder string
        return destination + "/" + name + "_" + imageType.toString().toLowerCase() + ".png";
    }
}