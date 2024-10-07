package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.*;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.DropSource;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.ShopSource;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.SpawnLocation;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiNPCInfo.WikiNPCResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.VSWikiScraper.PageData;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.api.ItemSourceParser;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.api.WikipediaApi;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public abstract class VSWikiScraper<T extends ScraperResult> {
    private static final Logger logger = LoggerFactory.getLogger(VSWikiScraper.class);
    protected final WikipediaApi wikipediaApi;
    protected final String baseUrl = "https://oldschool.runescape.wiki/";
    protected final Map<String, CompletableFuture<T>> ongoingSearches;
    protected final Map<String, T> cachedResults;
    protected final Path DEFAULT_DESTINATION;
    protected final Path DEFAULT_DATABASE_JSON;
    protected final Path imageFolder;
    protected final Gson gson;

    public enum ScraperState {
        IDLE, SEARCHING, PROCESSING, COMPLETED, ERROR
    }

    protected ScraperState state;

    public VSWikiScraper(Path destination, String databaseName, Path imageFolder, boolean resetDatabase) {

        if (destination == null || destination.toString().isEmpty()) {
            destination = Paths.get("../data/wikidata");
        }
        this.wikipediaApi = new WikipediaApi();
        this.ongoingSearches = new ConcurrentHashMap<>();
        this.cachedResults = new ConcurrentHashMap<>();
        this.state = ScraperState.IDLE;
        this.DEFAULT_DESTINATION = destination.resolve(databaseName);
        this.DEFAULT_DATABASE_JSON = this.DEFAULT_DESTINATION.resolve(databaseName + ".json");
        this.imageFolder = imageFolder;
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        if (resetDatabase && DEFAULT_DATABASE_JSON.toFile().exists()) {
            DEFAULT_DATABASE_JSON.toFile().delete();
        }
    }

    protected <V extends ScraperResult> Map<String, V> loadDatabase(Path databaseFile, boolean reset, Class<V> valueType) {
        if (!reset && databaseFile.toFile().exists()) {
            try (Reader reader = new FileReader(databaseFile.toFile())) {
                Type mapType = TypeToken.getParameterized(Map.class, String.class, List.class, valueType).getType();
                return gson.fromJson(reader, mapType);
            } catch (IOException e) {
                logger.error("Error loading database: ", e);
            }
        }
        return new HashMap<>();
    }

    protected <V extends ScraperResult> void saveDatabase(Map<String, V> data, Path filePath) {
        try (Writer writer = new FileWriter(filePath.toFile())) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            logger.error("Error saving database: ", e);
        }
    }
    protected <V extends ScraperResult> Map<String, Map<String, V>> loadDatabaseNested(Path databaseFile, boolean reset, Class<V> valueType) {
        if (!reset && databaseFile.toFile().exists()) {
            try (Reader reader = new FileReader(databaseFile.toFile())) {
                Type mapType = TypeToken.getParameterized(Map.class, String.class, Map.class, String.class, valueType).getType();
                return gson.fromJson(reader, mapType);
            } catch (IOException e) {
                logger.error("Error loading database: ", e);
            }
        }
        return new HashMap<>();
    }

    protected <V extends ScraperResult> void saveDatabaseNested(Map<String, Map<String, V>> data, Path filePath) {
        try (Writer writer = new FileWriter(filePath.toFile())) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            logger.error("Error saving database: ", e);
        }
    }

    protected void checkDictKeys(Map<String, Map<String, WikiNPCResult>> dict, List<String> keys) {
        for (Map.Entry<String, Map<String, WikiNPCResult>> entry : dict.entrySet()) {
            String npcName = entry.getKey();
            Map<String, WikiNPCResult> versionMap = entry.getValue();
            
            for (Map.Entry<String, WikiNPCResult> versionEntry : versionMap.entrySet()) {
                String version = versionEntry.getKey();
                WikiNPCResult npc = versionEntry.getValue();
                
                for (String key : keys) {
                    if (!hasProperty(npc, key)) {
                        logger.warn("Warning: Missing key '{}' in NPC result for {}, version {}", key, npcName, version);
                    }
                }
            }
        }
    }

    protected boolean hasProperty(Object obj, String propertyName) {
        try {
            obj.getClass().getDeclaredField(propertyName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    protected WikipediaPage getWikiPage(String pageName) {
        try {
            return wikipediaApi.getPageContent(pageName).get();
        } catch (Exception e) {
            logger.error("Error retrieving wiki page: ", e);
            return null;
        }
    }

    public abstract void saveDatabases() throws IOException;

    protected Path getDefaultDestination() {
        return DEFAULT_DESTINATION;
    }

    protected Path getDefaultDatabaseJson() {
        return DEFAULT_DATABASE_JSON;
    }

    protected Path getImageFolder() {
        return imageFolder;
    }

    public ScraperState getState() {
        return state;
    }

    public boolean isDataAvailable(String searchString) {
        return cachedResults.containsKey(searchString);
    }

    public boolean isSearchCompleted(String searchString) {
        return !ongoingSearches.containsKey(searchString) && cachedResults.containsKey(searchString);
    }

    protected Map<String, List<String>> parseInfobox(String wikiText, String infoboxType) {
        Map<String, List<String>> infoboxData = new HashMap<>();
        Pattern pattern = Pattern.compile("\\{\\{" + infoboxType + "\\s*\n([\\s\\S]*?)\\}\\}", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(wikiText);
    
        if (matcher.find()) {
            String infobox = matcher.group(1);
            Pattern keyValuePattern = Pattern.compile("^\\|\\s*([\\w\\d]+)\\s*=\\s*(.+)$", Pattern.MULTILINE);
            Matcher keyValueMatcher = keyValuePattern.matcher(infobox);
    
            while (keyValueMatcher.find()) {
                String key = keyValueMatcher.group(1).trim();
                String value = keyValueMatcher.group(2).trim();
                value = value.replaceAll("\\[\\[|\\]\\]|\\{\\{|\\}\\}", "");
                infoboxData.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            }
        } else {
            logger.warn("No infobox found of type: {}", infoboxType);
        }
    
        logger.debug("Parsed infobox data: {}", infoboxData);
        return infoboxData;
    }
    protected int getVersionCount(Map<String, List<String>> infoboxData) {
        return infoboxData.entrySet().stream()
                .filter(entry -> entry.getKey().matches("name\\d+"))
                .map(entry -> Integer.parseInt(entry.getKey().substring(4)))
                .max(Integer::compareTo)
                .orElse(1);
    }

    protected String getVersionedValue(Map<String, List<String>> infoboxData, String key, int version) {
        if (!infoboxData.containsKey(key) && infoboxData.containsKey(key + version)) {
            String versionedKey = key + version;
            List<String> values = infoboxData.get(versionedKey);
            return values != null && !values.isEmpty() ? values.get(0) : null;
        } else {
            assert(!infoboxData.containsKey(key + version));
            List<String> values = infoboxData.get(key);
            return values != null && !values.isEmpty() ? values.get(0) : null;
        }

    }

    protected int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    protected boolean parseYesNoValue(String value) {
        return value != null && value.trim().equalsIgnoreCase("Yes");
    }

    protected List<String> parseAttributesNPCS(String attributesString) {
        if (attributesString == null) return null;
        return Arrays.asList(attributesString.split(",\\s*"));
    }

    protected List<String> parseLocationsTable(String wikiText) {
        List<String> locations = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{Location\\|([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(wikiText);
        while (matcher.find()) {
            locations.add(matcher.group(1).split("\\|")[0].trim());
        }
        return locations.isEmpty() ? null : locations;
    }

    protected WikiMapResult parseMapInfo(String mapString) {
        WikiMapResult mapResult = new WikiMapResult();
        Pattern pattern = Pattern.compile("\\{\\{Map\\|([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(mapString);

        if (matcher.find()) {
            String[] params = matcher.group(1).split("\\|");
            for (String param : params) {
                String[] keyValue = param.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    switch (key) {
                        case "name":
                            mapResult.setName(value);
                            break;
                        case "x":
                            mapResult.setX(Integer.parseInt(value));
                            break;
                        case "y":
                            mapResult.setY(Integer.parseInt(value));
                            break;
                        case "plane":
                            mapResult.setPlane(Integer.parseInt(value));
                            break;
                        case "mapID":
                            mapResult.setMapID(Integer.parseInt(value));
                            break;
                        case "mtype":
                            mapResult.setMtype(value);
                            break;
                        case "r":
                            mapResult.setR(Integer.parseInt(value));
                            break;
                        case "squareX":
                            mapResult.setSquareX(Integer.parseInt(value));
                            break;
                        case "squareY":
                            mapResult.setSquareY(Integer.parseInt(value));
                            break;
                        case "ptype":
                            mapResult.setPtype(value);
                            break;
                    }
                }
            }
        }

        return mapResult;
    }

    protected List<WikiMapResult> parseAllMapInfo(String wikiText) {
        List<WikiMapResult> maps = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{Map\\|([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(wikiText);

        while (matcher.find()) {
            WikiMapResult map = parseMapInfo(matcher.group());
            maps.add(map);
        }

        return maps;
    }
   

    protected Map<String, String> parseDropLine(String dropLine) {
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
    protected List<Integer> parseIds(String idString) {
        if (idString == null || idString.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> ids = new ArrayList<>();
        String[] idParts = idString.split(",");
        for (String idPart : idParts) {
            try {
                ids.add(Integer.parseInt(idPart.trim()));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse id: {}", idPart);
            }
        }
        return ids;
    }
    

    protected List<Integer> parseQuantity(String quantityStr) {
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

    protected double parseRarity(String rarityStr) {
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
    protected List<String> parseOptions(List<String> options) {
        if (options == null || options.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(options.get(0).split(","));
    }

   
    protected List<SpawnLocation> parseSpawnLocations(String wikiText) {
        List<SpawnLocation> spawnLocations = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{ItemSpawnLine\\|((?:[^{}]|\\{(?!\\{)|\\}(?!\\})|\\{\\{(?:[^{}]|\\{(?!\\{)|\\}(?!\\}))*\\}\\})*?)\\}\\}");
        Matcher matcher = pattern.matcher(wikiText);
        
        while (matcher.find()) {
            String spawnData = matcher.group(1);
            SpawnLocation location = parseSpawnLocation(spawnData);
            if (location != null) {
                spawnLocations.add(location);
            }
        }
        
        return spawnLocations;
    }  
    private SpawnLocation parseSpawnLocation(String spawnData) {
        SpawnLocation location = new SpawnLocation();
        Map<String, String> attributes = parseAttributes(spawnData);
        //setdefu
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            
            switch (key) {
                case "name":
                    // Ignore the name attribute as it's not part of the SpawnLocation
                    break;
                case "location":
                    parseLocationAndSublocation(location, value);
                    break;
                case "members":
                    location.setMembers(parseYesNoValue(value));
                    break;
                case "mapID":
                    location.setMapID(parseIntOrDefault(value, -1));
                    break;
                case "plane":
                    location.setPlane(parseIntOrDefault(value, 0));
                    break;
                default:
                    if (key.matches("\\d+,\\d+")) {
                        parseCoordinates(location, key);
                    }
                    break;
            }
        }
        
        return location.getLocation() != null ? location : null;
    }

    private Map<String, String> parseAttributes(String spawnData) {
        Map<String, String> attributes = new LinkedHashMap<>();
        StringBuilder currentValue = new StringBuilder();
        String currentKey = null;
        int nestedBrackets = 0;

        for (String part : spawnData.split("\\|")) {
            part = part.trim();
            
            if (part.contains("=") && nestedBrackets == 0) {
                if (currentKey != null) {
                    attributes.put(currentKey, currentValue.toString().trim());
                    currentValue.setLength(0);
                }
                String[] keyValue = part.split("=", 2);
                currentKey = keyValue[0].trim();
                currentValue.append(keyValue[1].trim());
            } else if (part.matches("\\d+,\\d+") && nestedBrackets == 0) {
                // Handle coordinate pairs
                attributes.put(part, "");
            } else {
                // Append to the current value, handling nested templates
                if (currentValue.length() > 0) {
                    currentValue.append("|");
                }
                currentValue.append(part);
            }

            // Count nested brackets
            nestedBrackets += countOccurrences(part, "{{") - countOccurrences(part, "}}");
        }

        // Add the last attribute
        if (currentKey != null) {
            attributes.put(currentKey, currentValue.toString().trim());
        }

        return attributes;
    }

    private int countOccurrences(String str, String subStr) {
        return (str.length() - str.replace(subStr, "").length()) / subStr.length();
    }
    private void parseLocationAndSublocation(SpawnLocation location, String value) {
        // Remove FloorNumber template for parsing, but keep it for sublocation
        String floorNumber = extractFloorNumber(value);
        String cleanValue = removeFloorNumber(value);

        Pattern pattern = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");
        Matcher matcher = pattern.matcher(cleanValue);

        List<String> locations = new ArrayList<>();
        while (matcher.find()) {
            locations.add(matcher.group(1));
        }

        if (!locations.isEmpty()) {
            location.setLocation(locations.get(0));
            if (locations.size() > 1) {
                location.setSublocation(locations.get(1));
            } else {
                String remaining = cleanValue.replaceAll("\\[\\[[^\\]]+\\]\\]", "").trim();
                if (!remaining.isEmpty()) {
                    location.setSublocation(remaining);
                }
            }
        } else {
            location.setLocation(cleanValue);
        }

        // Add FloorNumber to sublocation if present
        if (!floorNumber.isEmpty()) {
           location.setSublocation((location.getSublocation() != null && location.getSublocation().equals("()")) ? null : location.getSublocation());
        }

        // Clean up location and sublocation
        if (location.getLocation() != null) {
            location.setLocation(cleanUpLocationString(location.getLocation()));
        }
        if (location.getSublocation() != null) {
            location.setSublocation(cleanUpLocationString(location.getSublocation()));
        }
    }

    private String extractFloorNumber(String value) {
        Pattern floorPattern = Pattern.compile("\\{\\{FloorNumber\\|[^}]+\\}\\}");
        Matcher floorMatcher = floorPattern.matcher(value);
        return floorMatcher.find() ? floorMatcher.group() : "";
    }

    private String removeFloorNumber(String value) {
        return value.replaceAll("\\{\\{FloorNumber\\|[^}]+\\}\\}", "").trim();
    }

    private String cleanUpLocationString(String locationString) {
        return locationString.replaceAll("^[-\\s]+|[-\\s]+$", "").replaceAll("\\s+", " ");
    }

    private void parseCoordinates(SpawnLocation location, String value) {
        String[] coords = value.split(",");
        if (coords.length == 2) {
            try {
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                location.getCoordinates().add(new SpawnLocation.Coordinate(x, y));
            } catch (NumberFormatException e) {
                logger.warn("Failed to parse coordinates: {}", value);
            }
        }
    }

 

    

    protected List<ShopSource> parseShopSources(String wikiText, String itemName) {
        List<ShopSource> shopSources = new ArrayList<>();
        if (wikiText.contains("{{Store locations list|" + itemName + "}}") ||
            wikiText.contains("{{Store locations list}}")) {
            try {
                shopSources = getStoreLocations(itemName).get();

                //for (String location : storeLocations) {
                //    ShopSource shopSource = parseShopSourceLine(location);
                //    if (shopSource != null) {
                //        shopSources.add(shopSource);
                //    }
                //}
            } catch (Exception e) {
                logger.error("Error fetching store locations for {}: {}", itemName, e.getMessage());
            }
        }
        return shopSources;
    }


    private ShopSource parseShopSourceLine(String line) {
        ShopSource shopSource = new ShopSource();
        String[] parts = line.split("\\t");
        if (parts.length < 8) {
            logger.warn("Invalid shop source line: {} length: {}", line, parts.length);
            return null;
        }

        shopSource.setShopName(parts[0]);
        shopSource.setLocation(parts[1]);
        shopSource.setNumberInStock(parseIntOrDefault(parts[2], 0));
        shopSource.setRestockTime(parts[3]);
        shopSource.setPriceSoldAt(parseIntOrDefault(parts[4], 0));
        shopSource.setPriceBoughtAt(parseIntOrDefault(parts[5], 0));
        shopSource.setChangePercent(parseDoubleOrDefault(parts[6].replace("%", ""), 0.0));
        shopSource.setMembers(parts[7].equalsIgnoreCase("Yes"));

        if (parts.length > 8) {
            shopSource.setNotes(parts[8]);
        }

        return shopSource;
    }
    private double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    protected String downloadImageFromTemplate(String name, String imageData, ImageType imageType, String destination) {
        
            
        String imageUrl = extractImageUrl(imageData);
        if (imageUrl != null) {
            String imagePath = downloadAndSaveImage(name, imageUrl, imageType, destination);                
            return imagePath;
        }
        
        
        return null;
    }
    protected List<String> downloadImagesFromTemplate(List<String> names, String imageData, ImageType imageType, String destination) {
        List<String> imagePaths = new ArrayList<>();
        
        for (String name : names) {
            String imagePath =  downloadImageFromTemplate(name, imageData, imageType, destination);            
            if (imagePath != null) {                
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

    protected abstract String downloadAndSaveImage(String name, String imageUrl, ImageType imageType, String destination);

    public Map<String, PageData> extractPageTitlesFromCategory(String categoryTitle, boolean verbose) {
        Map<String, PageData> pagesData = new HashMap<>();
        Set<String> processedCategories = new HashSet<>();
        extractPageTitlesFromCategoryRecursive(categoryTitle, pagesData, processedCategories, verbose);
        return pagesData;
    }

    private void extractPageTitlesFromCategoryRecursive(String categoryTitle, Map<String, PageData> pagesData, Set<String> processedCategories, boolean verbose) {
        if (processedCategories.contains(categoryTitle)) {
            return;
        }
        processedCategories.add(categoryTitle);

        WikipediaPage categoryPage = getWikiPage(categoryTitle);
        if (categoryPage == null) {
            if (verbose) {
                System.out.println("Failed to retrieve category page: " + categoryTitle);
            }
            return;
        }

        List<String> pages = categoryPage.getLinks().stream()
                .filter(link -> !link.startsWith("Category:"))
                .collect(Collectors.toList());

        List<String> subcategories = categoryPage.getLinks().stream()
                .filter(link -> link.startsWith("Category:"))
                .collect(Collectors.toList());

        String cleanCategoryTitle = extractCategoryTitle(categoryTitle);

        for (String pageName : pages) {
            if (!pagesData.containsKey(pageName)) {
                pagesData.put(pageName, new PageData(pageName, cleanCategoryTitle));
            } else {
                pagesData.get(pageName).setCategory(cleanCategoryTitle);
            }
        }

        for (String subcategory : subcategories) {
            extractPageTitlesFromCategoryRecursive(subcategory, pagesData, processedCategories, verbose);
        }
    }

    private static String extractCategoryTitle(String categoryTitle) {
        return categoryTitle.startsWith("Category:") ? categoryTitle.substring(9) : categoryTitle;
    }

    // Inner class to represent page data
    public static class PageData {
        private String name;
        private String category;

        public PageData(String name, String category) {
            this.name = name;
            this.category = category;
        }

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }
    protected List<DropSource> parseDropSources(String wikiText, String itemName) {
        List<DropSource> dropSources = new ArrayList<>();
        if (wikiText.contains("{{Drop sources|" + itemName + "}}") ||
            wikiText.contains("{{Drop sources}}")) {
            try {
                List<String> sourcesData = getDropSources(itemName).get();
                for (String sourceData : sourcesData) {
                    DropSource dropSource = parseDropSourceLine(sourceData);
                    if (dropSource != null) {
                        dropSources.add(dropSource);
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching drop sources for {}: {}", itemName, e.getMessage());
            }
        }
        return dropSources;
    }

    private DropSource parseDropSourceLine(String line) {
        DropSource dropSource = new DropSource();
        String[] parts = line.split("\t");
        if (parts.length < 4) {
            logger.warn("Invalid drop source line: {}", line);
            return null;
        }

        dropSource.setSourceName(parts[0]);
        dropSource.setSourceLevel(parseLevel(parts[1]));
        parseQuantity(parts[2], dropSource);
        dropSource.setDropRate(parseDropRate(parts[3]));

        if (parts.length > 4) {
            dropSource.setNotes(parts[4]);
        }

        return dropSource;
    }

    private int parseLevel(String levelStr) {
        if (levelStr.equalsIgnoreCase("N/A")) {
            return -1;
        }
        try {
            return Integer.parseInt(levelStr);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private void parseQuantity(String quantityStr, DropSource dropSource) {
        if (quantityStr.contains("–")) {
            String[] quantities = quantityStr.split("–");
            dropSource.setMinQuantity(Integer.parseInt(quantities[0]));
            dropSource.setMaxQuantity(Integer.parseInt(quantities[1]));
        } else {
            int quantity = Integer.parseInt(quantityStr);
            dropSource.setMinQuantity(quantity);
            dropSource.setMaxQuantity(quantity);
        }
    }

    private double parseDropRate(String dropRateStr) {
        if (dropRateStr.contains("/")) {
            String[] fractionParts = dropRateStr.split("/");
            return Double.parseDouble(fractionParts[0]) / Double.parseDouble(fractionParts[1]);
        } else {
            return Double.parseDouble(dropRateStr);
        }
    }

      public CompletableFuture<List<ShopSource>> getStoreLocations(String itemName) {
        return CompletableFuture.supplyAsync(() -> {          
            try {            
                String html = wikipediaApi.getHTMLPage(itemName).get();
                Map<String, List<ShopSource>> shopSources = ItemSourceParser.parseStoreLocationsFromHtml(html, itemName);
                List<ShopSource> shopSourcesList = new ArrayList<>();
                for (Map.Entry<String, List<ShopSource>> entry : shopSources.entrySet()) {
                    logger.info("Version: " + entry.getKey());
                    
                    for (ShopSource source : entry.getValue()) {
                        logger.info(""+source);
                    }
                    if (entry.getKey().equals("Default")) {
                        assert(shopSourcesList.isEmpty());
                        assert(shopSources.size()  ==1);
                        shopSourcesList = entry.getValue();
                        
                        
                    }else{
                        
                    }
                }
                if (shopSourcesList.isEmpty()) {
                    logger.info("No shop sources found for item: " + itemName);
                    return new ArrayList<>();
                }
               
                return shopSources.containsKey(itemName) ? shopSources.get(itemName) : shopSources.get("Default");
               // List<String> storeLocations = ItemSourceParser.parseStoreLocationsFromHtmlOld(html, itemName);
                //TODO impentation of filtering only item sources from the given item
                
            } catch (Exception e) {
                logger.error("Error fetching store locations for {}: {}", itemName, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch store locations for " + itemName, e);
            }
        });
    }

    
    public CompletableFuture<List<String>> getDropSources(String itemName) {
        return CompletableFuture.supplyAsync(() -> {
            long FetchStart = System.currentTimeMillis();
            logger.info("Start Fetching drop sources for item: " + itemName);          
            try {            
                String html = wikipediaApi.getHTMLPage(itemName).get();
                logger.info("\n\nstart parsing drop sources for item: " + itemName);
                long FetchEnd = System.currentTimeMillis();
                long FetchTime = FetchEnd - FetchStart;
                long FetchTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(FetchTime);
                long FetchTimeMillis = FetchTime - TimeUnit.SECONDS.toMillis(FetchTimeSeconds);
                logger.info("End Fetching drop sources for item: " + itemName + " in " + (FetchEnd - FetchStart) + " ms");
                
                Map<String, List<DropSource>> dropSources = ItemSourceParser.parseDropSourcesFromHtml(html, itemName);
                // Now you can access drop sources and shop sources for each version
                for (Map.Entry<String, List<DropSource>> entry : dropSources.entrySet()) {
                    System.out.println("Version: " + entry.getKey());
                    for (DropSource source : entry.getValue()) {
                        System.out.println(source);
                    }
                }
                return ItemSourceParser.parseDropSourcesFromHtmlOld(html, itemName);
            } catch (Exception e) {
                logger.error("Error fetching drop sources for {}: {}", itemName, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch drop sources for " + itemName, e);
            }
           
        });
    }
}