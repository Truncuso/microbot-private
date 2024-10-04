package net.runelite.client.plugins.VoxSylvaePlugins.scraper.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikipediaApi {
    private static final Logger logger = LoggerFactory.getLogger(WikipediaApi.class);
    private final OkHttpClient client;
    private final Gson gson;
    private final String baseUrl = "https://oldschool.runescape.wiki/api.php";

    public WikipediaApi() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        this.gson = new Gson();
    }

    public CompletableFuture<WikipediaPage> getPageContent(String pageName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "parse")
                .addQueryParameter("page", pageName)
                .addQueryParameter("format", "json")
                .addQueryParameter("prop", "wikitext|revid|categories|links|templates")
                .build();

            Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "RuneLite/MicrobotScraper")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseBody = response.body().string();
                //logger.debug("API Response for {}: {}", pageName, responseBody);

                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                if (jsonObject.has("error")) {
                    throw new IOException("API error: " + jsonObject.get("error").getAsJsonObject().get("info").getAsString());
                }

                if (!jsonObject.has("parse")) {
                    throw new IOException("Unexpected response structure. 'parse' object not found.");
                }

                JsonObject parse = jsonObject.getAsJsonObject("parse");
                
                if (!parse.has("title") || !parse.has("wikitext") || !parse.has("revid")) {
                    throw new IOException("Missing required fields in the parse object.");
                }
                String title = parse.get("title").getAsString();
                String wikitext = parse.getAsJsonObject("wikitext").get("*").getAsString();
                JsonArray templates = parse.getAsJsonArray("templates");
                List<String> templateNames = new ArrayList<>();
                for (JsonElement tamplet : templates) {
                    System.out.println(tamplet.getAsJsonObject().get("*"));
                    templateNames.add(tamplet.getAsJsonObject().get("*").getAsString());
                }

                int revisionId = parse.get("revid").getAsInt();
                System.out.println("Title: " + title);
                System.out.println("Wikitext length: " + wikitext.length());
                System.out.println("Revision ID: " + revisionId);
                System.out.println("First 100 characters of wikitext: " + wikitext.substring(0, Math.min(100, wikitext.length())));
                logger.debug("Title: {}", title);
                logger.debug("Wikitext length: {}", wikitext.length());
                logger.debug("Revision ID: {}", revisionId);
                logger.trace("Full wikitext content: {}", wikitext);
                return new WikipediaPage(title, wikitext, revisionId);
            } catch (Exception e) {
                logger.error("Error fetching page content for {}: {}", pageName, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch page content for " + pageName, e);
            }
        });
    }

    public CompletableFuture<Map<String, String>> getPageCategories(String pageName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "query")
                .addQueryParameter("titles", pageName)
                .addQueryParameter("prop", "categories")
                .addQueryParameter("format", "json")
                .build();

            Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "RuneLite/MicrobotScraper")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseBody = response.body().string();
                logger.debug("API Response for categories of {}: {}", pageName, responseBody);

                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                Map<String, String> categories = new HashMap<>();
                JsonObject pages = jsonObject.getAsJsonObject("query").getAsJsonObject("pages");
                String pageId = pages.keySet().iterator().next();
                JsonObject page = pages.getAsJsonObject(pageId);

                if (page.has("categories")) {
                    page.getAsJsonArray("categories").forEach(category -> {
                        String categoryTitle = category.getAsJsonObject().get("title").getAsString();
                        categories.put(categoryTitle, categoryTitle.replace("Category:", ""));
                    });
                }

                logger.info("Retrieved {} categories for {}", categories.size(), pageName);
                return categories;
            } catch (Exception e) {
                logger.error("Error fetching categories for {}: {}", pageName, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch categories for " + pageName, e);
            }
        });
    }

    public CompletableFuture<byte[]> getImage(String fileName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "query")
                .addQueryParameter("titles", "File:" + fileName)
                .addQueryParameter("prop", "imageinfo")
                .addQueryParameter("iiprop", "url")
                .addQueryParameter("format", "json")
                .build();

            Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "RuneLite/MicrobotScraper")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseBody = response.body().string();
                logger.debug("API Response for image info of {}: {}", fileName, responseBody);

                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                JsonObject pages = jsonObject.getAsJsonObject("query").getAsJsonObject("pages");
                String pageId = pages.keySet().iterator().next();
                JsonObject page = pages.getAsJsonObject(pageId);

                if (page.has("imageinfo")) {
                    String imageUrl = page.getAsJsonArray("imageinfo").get(0).getAsJsonObject().get("url").getAsString();

                    logger.info("Fetching image from URL: {}", imageUrl);

                    Request imageRequest = new Request.Builder()
                        .url(imageUrl)
                        .header("User-Agent", "RuneLite/MicrobotScraper")
                        .build();

                    try (Response imageResponse = client.newCall(imageRequest).execute()) {
                        if (!imageResponse.isSuccessful()) throw new IOException("Unexpected code " + imageResponse);

                        byte[] imageBytes = imageResponse.body().bytes();
                        logger.info("Successfully fetched image for {}. Size: {} bytes", fileName, imageBytes.length);
                        return imageBytes;
                    }
                } else {
                    logger.warn("Image not found: {}", fileName);
                    throw new IOException("Image not found: " + fileName);
                }
            } catch (Exception e) {
                logger.error("Error fetching image {}: {}", fileName, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch image " + fileName, e);
            }
        });
    }

    public CompletableFuture<List<String>> searchPages(String query, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "opensearch")
                .addQueryParameter("search", query)
                .addQueryParameter("limit", String.valueOf(limit))
                .addQueryParameter("namespace", "0")
                .addQueryParameter("format", "json")
                .build();

            Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "RuneLite/MicrobotScraper")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseBody = response.body().string();
                logger.debug("API Response for search query '{}': {}", query, responseBody);

                JsonArray jsonArray = gson.fromJson(responseBody, JsonArray.class);
                JsonArray resultArray = jsonArray.get(1).getAsJsonArray();

                List<String> results = new ArrayList<>();
                resultArray.forEach(element -> results.add(element.getAsString()));
                
                logger.info("Found {} results for search query '{}'", results.size(), query);
                return results;
            } catch (Exception e) {
                logger.error("Error searching pages for query '{}': {}", query, e.getMessage(), e);
                throw new RuntimeException("Failed to search pages for query: " + query, e);
            }
        });
    }

    public CompletableFuture<Map<String, String>> getPageLinks(String pageName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "query")
                .addQueryParameter("titles", pageName)
                .addQueryParameter("prop", "links")
                .addQueryParameter("pllimit", "max")
                .addQueryParameter("format", "json")
                .build();

            Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "RuneLite/MicrobotScraper")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseBody = response.body().string();
                logger.debug("API Response for links of {}: {}", pageName, responseBody);

                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                Map<String, String> links = new HashMap<>();
                JsonObject pages = jsonObject.getAsJsonObject("query").getAsJsonObject("pages");
                String pageId = pages.keySet().iterator().next();
                JsonObject page = pages.getAsJsonObject(pageId);

                if (page.has("links")) {
                    page.getAsJsonArray("links").forEach(link -> {
                        JsonObject linkObj = link.getAsJsonObject();
                        links.put(linkObj.get("title").getAsString(), linkObj.get("ns").getAsString());
                    });
                }

                logger.info("Retrieved {} links for {}", links.size(), pageName);
                return links;
            } catch (Exception e) {
                logger.error("Error fetching links for {}: {}", pageName, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch links for " + pageName, e);
            }
        });
    }

    public CompletableFuture<List<String>> getCategoryMembers(String categoryName, int limit) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "query")
                .addQueryParameter("list", "categorymembers")
                .addQueryParameter("cmtitle", "Category:" + categoryName)
                .addQueryParameter("cmlimit", String.valueOf(limit))
                .addQueryParameter("format", "json")
                .build();

            Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "RuneLite/MicrobotScraper")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseBody = response.body().string();
                logger.debug("API Response for members of category {}: {}", categoryName, responseBody);

                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                List<String> members = new ArrayList<>();
                JsonArray categoryMembers = jsonObject.getAsJsonObject("query")
                    .getAsJsonArray("categorymembers");

                categoryMembers.forEach(member -> {
                    members.add(member.getAsJsonObject().get("title").getAsString());
                });

                logger.info("Retrieved {} members for category {}", members.size(), categoryName);
                return members;
            } catch (Exception e) {
                logger.error("Error fetching category members for {}: {}", categoryName, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch category members for " + categoryName, e);
            }
        });
    }

    public CompletableFuture<Map<String, String>> getPageInfo(String pageName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "query")
                .addQueryParameter("titles", pageName)
                .addQueryParameter("prop", "info")
                .addQueryParameter("inprop", "url|displaytitle")
                .addQueryParameter("format", "json")
                .build();

            Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "RuneLite/MicrobotScraper")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseBody = response.body().string();
                logger.debug("API Response for page info of {}: {}", pageName, responseBody);

                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                Map<String, String> pageInfo = new HashMap<>();
                JsonObject pages = jsonObject.getAsJsonObject("query").getAsJsonObject("pages");
                String pageId = pages.keySet().iterator().next();
                JsonObject page = pages.getAsJsonObject(pageId);

                pageInfo.put("pageId", pageId);
                pageInfo.put("title", page.get("title").getAsString());
                pageInfo.put("fullUrl", page.get("fullurl").getAsString());
                pageInfo.put("displayTitle", page.get("displaytitle").getAsString());

                logger.info("Retrieved page info for {}. Page ID: {}", pageName, pageId);
                return pageInfo;
            } catch (Exception e) {
                logger.error("Error fetching page info for {}: {}", pageName, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch page info for " + pageName, e);
            }
        });
    }
    public CompletableFuture<List<String>> getStoreLocations(String itemName) {
        return CompletableFuture.supplyAsync(() -> {
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "parse")
                .addQueryParameter("page", itemName)
                .addQueryParameter("format", "json")
                .addQueryParameter("prop", "text")
                .build();

            Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "RuneLite/MicrobotScraper")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                if (jsonObject.has("error")) {
                    throw new IOException("API error: " + jsonObject.get("error").getAsJsonObject().get("info").getAsString());
                }

                String html = jsonObject.getAsJsonObject("parse")
                    .getAsJsonObject("text")
                    .get("*").getAsString();

                return parseStoreLocationsFromHtml(html);
            } catch (Exception e) {
                logger.error("Error fetching store locations for {}: {}", itemName, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch store locations for " + itemName, e);
            }
        });
    }

    private List<String> parseStoreLocationsFromHtml(String html) {
        List<String> storeLocations = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements tables = doc.select("table.wikitable");
        
        for (Element table : tables) {
            Elements rows = table.select("tr");
            for (int i = 1; i < rows.size(); i++) { // Skip header row
                Elements cells = rows.get(i).select("td");
                if (cells.size() >= 8) {
                    //log type of element 7
                    logger.info("type of element 7: {} size: {}, raw: {}", cells.get(7).text(), cells.get(7).text().length(), cells.get(7));
                    String location = String.join("\t", 
                        cells.get(0).text(), // Shop Name
                        cells.get(1).text(), // Location
                        cells.get(2).text(), // Number in stock
                        cells.get(3).text(), // Restock time
                        cells.get(4).text(), // Price sold at
                        cells.get(5).text(), // Price bought at
                        cells.get(6).text(), // Change %
                        String.valueOf(cells.get(7)).contains("Members")?"Yes":"No"  // Members
                    );
                    logger.info("shop data: {}", location);
                    if (cells.size() > 8) {
                        location += "\t" + cells.get(8).text(); // Notes
                    }
                    storeLocations.add(location);
                }
            }
        }

        return storeLocations;
    }
    public CompletableFuture<List<String>> getDropSources(String itemName) {
        return CompletableFuture.supplyAsync(() -> {
            logger.info("Fetching drop sources for item: " + itemName);
            HttpUrl url = HttpUrl.parse(baseUrl).newBuilder()
                .addQueryParameter("action", "parse")
                .addQueryParameter("page", itemName)
                .addQueryParameter("format", "json")
                .addQueryParameter("prop", "text")
                .build();

            Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", "RuneLite/MicrobotScraper")
                .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                String responseBody = response.body().string();
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                if (jsonObject.has("error")) {
                    throw new IOException("API error: " + jsonObject.get("error").getAsJsonObject().get("info").getAsString());
                }

                String html = jsonObject.getAsJsonObject("parse")
                    .getAsJsonObject("text")
                    .get("*").getAsString();
                logger.info("\n\nstart parsing drop sources for item: " + itemName);
                return parseDropSourcesFromHtml(html, itemName);
            } catch (Exception e) {
                logger.error("Error fetching drop sources for {}: {}", itemName, e.getMessage(), e);
                throw new RuntimeException("Failed to fetch drop sources for " + itemName, e);
            }
        });
    }

    private List<String> parseDropSourcesFromHtml(String html, String itemName) {
        List<String> dropSources = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements tables = doc.select("table.wikitable");
          // Find the table that follows the item-specific Drop sources template
       
        for (Element table : tables) {
            
            Elements rows = table.select("tr");
            // print hearder row
            
            Elements HeaderCells = rows.get(0).select("td");
            if (rows.get(0).text().contains("Source") && rows.get(0).text().contains("Level") && rows.get(0).text().contains("Quantity") && rows.get(0).text().contains("Rarity")) {
                logger.info("Header row contains Source, Level, Quantity, Rarity");
                logger.info(rows.get(0).text());
                logger.info("Number of rows: " + rows.size());
                logger.info("Number of header cells: " + table.text().contains(itemName));
            }else{
                continue;
            }
           
            for (int i = 1; i < rows.size(); i++) { // Skip header row
                Elements cells = rows.get(i).select("td");
                if (cells.size() >= 4) {
                    // Source Name, Level, Quantity, Rarity
                    //log number of cells
                    logger.info("Number of cells: " + cells.size());
                    //log cell headers
                    
                    logger.info("{} {} {} {}", cells.get(0).text(), cells.get(1).text(), cells.get(2).text(), cells.get(3).text());
                    String source = String.join("\t", 
                        cells.get(0).text(), // Source Name
                        cells.get(1).text(), // Level
                        cells.get(2).text(), // Quantity
                        cells.get(3).text()  // Rarity
                    );
                    logger.info("source: " + source);
                    if (cells.size() > 4) {
                        source += "\t" + cells.get(4).text(); // Notes
                    }
                    dropSources.add(source);
                }
            }
        }

        return dropSources;
    }
}