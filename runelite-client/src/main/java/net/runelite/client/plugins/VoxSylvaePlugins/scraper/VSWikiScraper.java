package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ScraperResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.api.WikipediaApi;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
public abstract class VSWikiScraper<T extends ScraperResult> {
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
                Type mapType = TypeToken.getParameterized(Map.class, String.class, valueType).getType();
                return gson.fromJson(reader, mapType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new HashMap<>();
    }

    protected <V extends ScraperResult> void saveDatabase(Map<String, V> data, Path filePath) {
        try (Writer writer = new FileWriter(filePath.toFile())) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected WikipediaPage getWikiPage(String pageName) {
        try {
            return wikipediaApi.getPageContent(pageName).get();
        } catch (Exception e) {
            e.printStackTrace();
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
    protected Map<String, String> parseInfobox(String wikiText, String infoboxType) {
        Map<String, String> infoboxData = new HashMap<>();
        Pattern pattern = Pattern.compile("\\{\\{" + infoboxType + "[\\s\\S]*?\\}\\}");
        Matcher matcher = pattern.matcher(wikiText);
        
        if (matcher.find()) {
            String infobox = matcher.group();
            Pattern keyValuePattern = Pattern.compile("\\|\\s*(\\w+)\\s*=\\s*([^|\\}]+)");
            Matcher keyValueMatcher = keyValuePattern.matcher(infobox);
            
            while (keyValueMatcher.find()) {
                String key = keyValueMatcher.group(1).trim();
                String value = keyValueMatcher.group(2).trim();
                infoboxData.put(key, value);
            }
        }
        
        return infoboxData;
    }

    protected List<String> downloadImagesFromTemplate(List<String> names, String imageData, ImageType imageType, String destination) {
        List<String> imagePaths = new ArrayList<>();
        
        for (String name : names) {
            String imageUrl = extractImageUrl(imageData);
            if (imageUrl != null) {
                String imagePath = downloadAndSaveImage(name, imageUrl, imageType, destination);
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
    protected String downloadAndSaveImage(String name, String imageUrl, ImageType imageType, String destination) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() == 200) {
                String fileName = name.replaceAll("[^a-zA-Z0-9.-]", "_") + "_" + imageType.toString().toLowerCase() + ".png";
                Path outputPath = Paths.get(destination, fileName);
                Files.createDirectories(outputPath.getParent());
                Files.write(outputPath, response.body());
                return outputPath.toString();
            } else {
                System.err.println("Failed to download image: " + imageUrl + ", Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error downloading image: " + imageUrl);
            e.printStackTrace();
        }
        return null;
    }

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