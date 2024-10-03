package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikiNPCResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikiItemResult;
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
            if (!forceReload && npcDatabase.containsKey(npcName)) {
                npcInfo.put(npcName, npcDatabase.get(npcName));
                continue;
            }

            WikipediaPage page = getWikiPage(npcName);
            if (page == null) {
                throw new RuntimeException("<RuneMatio> " + npcName + ": Wiki Page doesn't exist and is not in DB");
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
        if (infoboxData.isEmpty()) {
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
            npcResult.setAttributes(parseAttributes(getVersionedValue(infoboxData, "attributes", i)));
            npcResult.setLocations(parseLocations(page.getContent()));
            npcResult.setOptions(parseOptions(infoboxData.get("options")));
            if (downloadImage) {
                String imagePathResult = downloadImageFromTemplate(npcResult.getName(), getVersionedValue(infoboxData, "image", i), imageType, imagePath);
                npcResult.setImagePath(imagePathResult);
            }

            npcResult.setDrops(parseDrops(page.getContent(), downloadImage, imageType, imagePath));

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

            npcResults.put(npcResult.getVersion(), npcResult);
        }

        return npcResults;
    }
    protected List<Drop> parseDrops(String wikiText, boolean downloadImage, ImageType imageType, String imagePath) {
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
    protected Drop getDropFromLine(Map<String, String> dropData, boolean downloadImage, ImageType imageType, String imagePath) {
        String itemName = dropData.get("name");
        WikiItemResult item = itemScraper.getItemsInfo(itemName, false, downloadImage, imageType, imagePath, true).get(itemName);

        List<Integer> quantity = parseQuantity(dropData.getOrDefault("quantity", "1"));
        double rarity = parseRarity(dropData.get("rarity"));

        return new Drop(item, quantity, rarity);
    }
   
  
    


   

   


    @Override
    protected String downloadAndSaveImage(String name, String imageUrl, ImageType imageType, String destination) {
        // Implement the actual image downloading and saving logic here
        // For now, we'll just return a placeholder string
        return destination + "/" + name + "_" + imageType.toString().toLowerCase() + ".png";
    }
}