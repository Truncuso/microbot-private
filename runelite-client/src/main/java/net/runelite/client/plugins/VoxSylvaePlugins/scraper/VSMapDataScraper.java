package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ScraperResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ScraperResult.ItemQuantity;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikiLocationResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikiMapResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.util.StringUtil;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class VSMapDataScraper extends VSWikiScraper<WikiMapResult> {

    private final Path mapDatabaseFile;
    private final List<String> mapKeys = Arrays.asList("name", "x", "y", "plane", "mapID", "mtype", "r", "squareX", "squareY", "ptype", "caption", "text", "align", "width", "height", "zoom", "group", "showPins", "title", "description", "nopreprocess", "smw");
    private Map<String, WikiMapResult> mapDatabase;
    private Map<String, WikiLocationResult> locationDatabase;
    private Map<String, ScraperResult.TeleportationSpellResult> teleportationSpellDatabase;
    private Map<String, ScraperResult.TeleportationItemResult> teleportationItemDatabase;
    private Map<String, ScraperResult.FairyRingResult> fairyRingDatabase;
    public VSMapDataScraper(Map<String, Object> databaseDict, Path destination, String databaseName, Path imageFolder, boolean resetDatabase) {
        super(destination, databaseName, imageFolder, resetDatabase);
        this.mapDatabaseFile = getDefaultDatabaseJson().resolveSibling("mapDB.json");
        this.mapDatabase = loadDatabase(mapDatabaseFile, resetDatabase, WikiMapResult.class);
        databaseDict.put("maps", this.mapDatabase);
    }

    @Override
    public void saveDatabases() throws IOException {
        saveDatabase(mapDatabase, mapDatabaseFile);
        saveDatabase(locationDatabase, getDefaultDatabaseJson().resolveSibling("locationDB.json"));
        saveDatabase(teleportationSpellDatabase, getDefaultDatabaseJson().resolveSibling("teleportationSpellDB.json"));
        saveDatabase(teleportationItemDatabase, getDefaultDatabaseJson().resolveSibling("teleportationItemDB.json"));
        saveDatabase(fairyRingDatabase, getDefaultDatabaseJson().resolveSibling("fairyRingDB.json"));
    }


    public Map<String, WikiMapResult> getMapInfo(String mapNamesSearchString, boolean forceReload, boolean downloadImage, ImageType imageType, String imagePath) {
        List<String> mapNames = StringUtil.formatArgs(mapNamesSearchString);
        Map<String, WikiMapResult> mapInfo = new HashMap<>();

        for (String mapName : mapNames) {
            if ((mapDatabase.containsKey(mapName) || mapDatabase.containsKey(StringUtil.capitalizeEachWord(mapName)))
                    && !forceReload && !downloadImage) {
                mapInfo.put(mapName, mapDatabase.get(mapName));
                continue;
            }

            WikipediaPage page = getWikiPage(mapName);
            if (page == null) {
                throw new RuntimeException("<RuneMatio> " + mapName + ": Wiki Page doesn't exist and is not in DB");
            }

            WikiMapResult mapInfoWiki = getMapInfoFromWikiText(page, downloadImage, imageType, imagePath);
            mapInfo.put(mapName, mapInfoWiki);
            mapDatabase.put(mapName, mapInfoWiki);
        }

        //checkDictKeys(mapInfo, mapKeys);
        return mapInfo;
    }

    private WikiMapResult getMapInfoFromWikiText(WikipediaPage page, boolean downloadImage, ImageType imageType, String imagePath) {
        WikiMapResult mapResult = new WikiMapResult();
        Map<String, List<String>> infoboxData = parseInfobox(page.getContent(), "Map");
        int versionCount = getVersionCount(infoboxData);
        assert versionCount == 1;
        mapResult.setName(infoboxData.get("name").get(0));
        //mapResult.setX(Integer.parseInt(infoboxData.getOrDefault("x", "0")));
        //mapResult.setY(Integer.parseInt(infoboxData.getOrDefault("y", "0")));
        //mapResult.setPlane(Integer.parseInt(infoboxData.getOrDefault("plane", "0")));
        //mapResult.setMapID(Integer.parseInt(infoboxData.getOrDefault("mapID", "-1")));
        mapResult.setMtype(infoboxData.get("mtype").get(0));
        //mapResult.setR(Integer.parseInt(infoboxData.getOrDefault("r", "0")));
        //mapResult.setSquareX(Integer.parseInt(infoboxData.getOrDefault("squareX", "0")));
        //mapResult.setSquareY(Integer.parseInt(infoboxData.getOrDefault("squareY", "0")));
        //mapResult.setPtype(infoboxData.get("ptype"));

        if (downloadImage) {
            //List<String> imagePaths = downloadImagesFromTemplate(Collections.singletonList(mapResult.getName()), infoboxData.get("image"), imageType, imagePath);
            //mapResult.setImagePaths(imagePaths);
        }

        return mapResult;
    }

 


    public void scrapeMapFromWebCanvas(Path mapTileDir, Path mapImageDir) {
        // Implement web scraping logic here
        // This would involve using Selenium or a similar tool to interact with the web page
        // and capture map tiles
    }

    private void mergeWebScrapedMap(Path mapTileDir, Path mapImageDir) {
        // Implement logic to merge scraped map tiles into a single image
        // This would involve image processing techniques, possibly using libraries like OpenCV
    }

    @Override
    protected String downloadAndSaveImage(String name, String imageUrl, ImageType imageType, String destination) {
        // Implement the actual image downloading and saving logic here
        // For now, we'll just return a placeholder string
        return destination + "/" + name + "_" + imageType.toString().toLowerCase() + ".png";
    }

    public WikiLocationResult getLocationInfoboxData(String pageTitle) {
        WikipediaPage page = getWikiPage(pageTitle);
        if (page == null) {
            return null;
        }

        WikiLocationResult result = new WikiLocationResult();
        Map<String, List<String>> infoboxData = parseInfobox(page.getContent(), "Infobox Location");
        int versionCount = getVersionCount(infoboxData);
        assert versionCount == 1;
        
        result.setName(infoboxData.get("name").get(0));
        result.setMembers(Boolean.parseBoolean(infoboxData.get("members").get(0)));
        result.setType(infoboxData.get("type").get(0));
        result.setLocation(parseLocation(infoboxData.get("location").get(0)));
        result.setMap(parseMapInfo(infoboxData.get("map").get(0)));
        result.setImage(parseImageInfo(infoboxData.get("image").get(0)));
        result.setCategory(infoboxData.get("category").get(0));

        return result;
    }

    public Map<String, WikiLocationResult> getAllLocationsData() {
        Map<String, PageData> pagesData = extractPageTitlesFromCategory("Locations", true);
        Map<String, WikiLocationResult> allLocations = new HashMap<>();

        for (Map.Entry<String, PageData> entry : pagesData.entrySet()) {
            String pageName = entry.getValue().getName();
            String category = entry.getValue().getCategory();
            WikiLocationResult locationData = getLocationsData(pageName, category);
            allLocations.put(pageName, locationData);
        }

        return allLocations;
    }

    public ScraperResult.TeleportationItemResult getTeleportationItemData(String itemName) {
        if (teleportationItemDatabase.containsKey(itemName)) {
            return teleportationItemDatabase.get(itemName);
        }

        ScraperResult.TeleportationItemResult result = new ScraperResult.TeleportationItemResult();
        // ... implement fetching teleportation item data ...

        teleportationItemDatabase.put(itemName, result);
        return result;
    }

    public WikiLocationResult getLocationsData(String locationName, String category) {
        if (locationDatabase.containsKey(locationName)) {
            return locationDatabase.get(locationName);
        }

        WikiLocationResult result = getLocationInfoboxData(locationName);
        result.setCategory(category);
        locationDatabase.put(locationName, result);
        return result;
    }

    public void fetchAllTeleportations() {
        getAllTeleportationSpellsData();
        getAllTeleportationItemsData();
        //getAllFairyRingData();
        try {
            saveDatabases();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
    }

    public Map<String, ScraperResult.TeleportationSpellResult> getAllTeleportationSpellsData() {
        Map<String, PageData> pagesData = extractPageTitlesFromCategory("Teleportation_spells", true);
        Map<String, ScraperResult.TeleportationSpellResult> allTeleportations = new HashMap<>();

        for (String pageName : pagesData.keySet()) {
            ScraperResult.TeleportationSpellResult teleportationData = getTeleportationSpellData(pageName);
            allTeleportations.put(pageName, teleportationData);
        }

        return allTeleportations;
    }

    public Map<String, ScraperResult.TeleportationItemResult> getAllTeleportationItemsData() {
        Map<String, PageData> pagesData = extractPageTitlesFromCategory("Teleportation_items", true);
        Map<String, ScraperResult.TeleportationItemResult> allItemTeleportations = new HashMap<>();

        for (String pageName : pagesData.keySet()) {
            ScraperResult.TeleportationItemResult teleportationData = getTeleportationItemData(pageName);
            allItemTeleportations.put(pageName, teleportationData);
        }

        return allItemTeleportations;
    }

    public ScraperResult.FairyRingResult getFairyRingData(String fairyRingCode) {
        if (fairyRingDatabase.containsKey(fairyRingCode)) {
            return fairyRingDatabase.get(fairyRingCode);
        }

        WikipediaPage page = getWikiPage("Fairy ring");
        if (page == null) {
            return null;
        }

        ScraperResult.FairyRingResult result = new ScraperResult.FairyRingResult();
        result.setCode(fairyRingCode);

        String wikiText = page.getContent();
        Pattern pattern = Pattern.compile(fairyRingCode + "\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^|]+)");
        Matcher matcher = pattern.matcher(wikiText);

        if (matcher.find()) {
            result.setDestination(matcher.group(1).trim());
            result.setLocation(matcher.group(2).trim());
        }

        // Extract name if available
        pattern = Pattern.compile(fairyRingCode + "\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^|]+)\\s*\\|\\s*([^|]+)");
        matcher = pattern.matcher(wikiText);
        if (matcher.find()) {
            result.setName(matcher.group(3).trim());
        }

        fairyRingDatabase.put(fairyRingCode, result);
        return result;
    }

    public ScraperResult.TeleportationSpellResult getTeleportationSpellData(String spellName) {
        if (teleportationSpellDatabase.containsKey(spellName)) {
            return teleportationSpellDatabase.get(spellName);
        }

        WikipediaPage page = getWikiPage(spellName);
        if (page == null) {
            return null;
        }

        ScraperResult.TeleportationSpellResult result = new ScraperResult.TeleportationSpellResult();
        result.setName(spellName);

        Map<String, List<String>> infoboxData = parseInfobox(page.getContent(), "Infobox Spell");

        result.setDestination(infoboxData.get("destination").get(0));
        result.setCost(parseItemQuantities(infoboxData.get("cost").get(0)));
        result.setSpellbook(infoboxData.get("spellbook").get(0));
        result.setType(infoboxData.get("type").get(0));
        result.setLevel(Integer.parseInt(infoboxData.get("level").get(0)));
        result.setImage(infoboxData.get("image").get(0));

        teleportationSpellDatabase.put(spellName, result);
        return result;
    }

    /**
     * Remove wiki markup and extract the plain text location from the given string.
     * Given a string like "[[Varrock]]" or "[[Varrock|Varrock Palace]]", this method will return "Varrock" or "Varrock Palace" respectively.
     * If the string does not contain wiki markup, the original string is returned.
     * @param locationString the string to extract the location from
     * @return the plain text location
     */
    private String parseLocation(String locationString) {
        // Remove wiki markup and extract the plain text location
        Pattern pattern = Pattern.compile("\\[\\[([^|\\]]+)(?:\\|([^\\]]+))?\\]\\]");
        Matcher matcher = pattern.matcher(locationString);
        if (matcher.find()) {
            return matcher.group(2) != null ? matcher.group(2) : matcher.group(1);
        }
        return locationString;
    }

    private String parseImageInfo(String imageString) {
        // Extract image file name from wiki markup
        Pattern pattern = Pattern.compile("\\[\\[File:([^|\\]]+)");
        Matcher matcher = pattern.matcher(imageString);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return imageString;
    }

    

    private List<ItemQuantity> parseItemQuantities(String costString) {
        List<ItemQuantity> costs = new ArrayList<>();
        if (costString == null || costString.isEmpty()) {
            return costs;
        }

        String[] items = costString.split(",");
        for (String item : items) {
            String[] parts = item.trim().split(" ", 2);
            if (parts.length == 2) {
                int quantity = Integer.parseInt(parts[0]);
                String itemName = parts[1];
                costs.add(new ItemQuantity(itemName, quantity));
            }
        }

        return costs;
    }

  
}