package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.Drop;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ScraperResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikiItemResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikiNPCResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.api.WikipediaApi;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
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
        String versionedKey = version == 1 ? key : key + version;
        List<String> values = infoboxData.get(versionedKey);
        return values != null && !values.isEmpty() ? values.get(0) : null;
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

    protected List<String> parseAttributes(String attributesString) {
        if (attributesString == null) return null;
        return Arrays.asList(attributesString.split(",\\s*"));
    }

    protected List<String> parseLocations(String wikiText) {
        List<String> locations = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{\\{Location\\|([^}]+)\\}\\}");
        Matcher matcher = pattern.matcher(wikiText);
        while (matcher.find()) {
            locations.add(matcher.group(1).split("\\|")[0].trim());
        }
        return locations.isEmpty() ? null : locations;
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
    protected List<String> parseSpawnLocations(String wikiText) {
        // Implementation to extract spawn locations
        return new ArrayList<>();
    }
    
    protected List<String> parseShopLocations(String wikiText) {
        // Implementation to extract shop locations
        return new ArrayList<>();
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
}