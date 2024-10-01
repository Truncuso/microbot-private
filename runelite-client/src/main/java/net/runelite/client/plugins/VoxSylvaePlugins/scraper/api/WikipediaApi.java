package net.runelite.client.plugins.VoxSylvaePlugins.scraper.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WikipediaApi {
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
                .addQueryParameter("prop", "wikitext|revid")
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

                JsonObject parse = jsonObject.getAsJsonObject("parse");
                String title = parse.get("title").getAsString();
                String wikitext = parse.getAsJsonObject("wikitext").get("*").getAsString();
                int revisionId = parse.get("revid").getAsInt();

                return new WikipediaPage(title, wikitext, revisionId);
            } catch (Exception e) {
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

                return categories;
            } catch (Exception e) {
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
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                JsonObject pages = jsonObject.getAsJsonObject("query").getAsJsonObject("pages");
                String pageId = pages.keySet().iterator().next();
                JsonObject page = pages.getAsJsonObject(pageId);

                if (page.has("imageinfo")) {
                    String imageUrl = page.getAsJsonArray("imageinfo").get(0).getAsJsonObject().get("url").getAsString();

                    // Now fetch the actual image
                    Request imageRequest = new Request.Builder()
                        .url(imageUrl)
                        .header("User-Agent", "RuneLite/MicrobotScraper")
                        .build();

                    try (Response imageResponse = client.newCall(imageRequest).execute()) {
                        if (!imageResponse.isSuccessful()) throw new IOException("Unexpected code " + imageResponse);

                        return imageResponse.body().bytes();
                    }
                } else {
                    throw new IOException("Image not found: " + fileName);
                }
            } catch (Exception e) {
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
                JsonArray jsonArray = gson.fromJson(responseBody, JsonArray.class);
                JsonArray resultArray = jsonArray.get(1).getAsJsonArray();

                List<String> results = new ArrayList<>();
                resultArray.forEach(element -> results.add(element.getAsString()));
                return results;
            } catch (Exception e) {
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

                return links;
            } catch (Exception e) {
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
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                List<String> members = new ArrayList<>();
                JsonArray categoryMembers = jsonObject.getAsJsonObject("query")
                    .getAsJsonArray("categorymembers");

                categoryMembers.forEach(member -> {
                    members.add(member.getAsJsonObject().get("title").getAsString());
                });

                return members;
            } catch (Exception e) {
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
                JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);

                Map<String, String> pageInfo = new HashMap<>();
                JsonObject pages = jsonObject.getAsJsonObject("query").getAsJsonObject("pages");
                String pageId = pages.keySet().iterator().next();
                JsonObject page = pages.getAsJsonObject(pageId);

                pageInfo.put("pageId", pageId);
                pageInfo.put("title", page.get("title").getAsString());
                pageInfo.put("fullUrl", page.get("fullurl").getAsString());
                pageInfo.put("displayTitle", page.get("displaytitle").getAsString());

                return pageInfo;
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch page info for " + pageName, e);
            }
        });
    }

    // You can add more methods here as needed, such as:
    // - getPageRevisions: to get revision history of a page
    // - getPageProperties: to get specific properties of a page
    // - getRandomPages: to get a list of random pages
    // - getPageTranscluded: to get pages that transclude a given page
    // - etc.
}