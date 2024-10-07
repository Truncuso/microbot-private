package net.runelite.client.plugins.VoxSylvaePlugins.scraper.api;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.*;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.DropSource;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.ShopSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemSourceParser {
    private static final Logger logger = LoggerFactory.getLogger(ItemSourceParser.class);

    public static Map<String, List<DropSource>> parseDropSourcesFromHtml(String html, String itemName) {
        Document doc = Jsoup.parse(html);
        Map<String, List<DropSource>> dropSourcesByVersion = new HashMap<>();

        Elements tables = doc.select("table");
        for (Element table : tables) {
            Elements rows = table.select("tr");
            if (rows.size() > 0 && isDropSourceTable(rows.first())) {
                String version = findVersionForTable(table, itemName);
                List<DropSource> sources = parseDropSourceTable(rows, itemName);
                if (!sources.isEmpty()) {
                    dropSourcesByVersion.computeIfAbsent(version, k -> new ArrayList<>()).addAll(sources);
                }
            }
        }

        return dropSourcesByVersion;
    }

    public static Map<String, List<ShopSource>> parseStoreLocationsFromHtml(String html, String itemName) {
        Document doc = Jsoup.parse(html);
        Map<String, List<ShopSource>> shopSourcesByVersion = new HashMap<>();

        Elements tables = doc.select("table");
        for (Element table : tables) {
            Elements rows = table.select("tr");
            if (rows.size() > 0 && isShopSourceTable(rows.first())) {
                String version = findVersionForTable(table, itemName);
                List<ShopSource> sources = parseShopSourceTable(rows, itemName);
                if (!sources.isEmpty()) {
                    shopSourcesByVersion.computeIfAbsent(version, k -> new ArrayList<>()).addAll(sources);
                }
            }
        }

        return shopSourcesByVersion;
    }

    private static boolean isDropSourceTable(Element headerRow) {
        String headerText = headerRow.text().toLowerCase();
        return headerText.contains("source") && headerText.contains("level") &&
               headerText.contains("quantity") && headerText.contains("rarity");
    }

    private static boolean isShopSourceTable(Element headerRow) {
        String headerText = headerRow.text().toLowerCase();
        return headerText.contains("seller") && headerText.contains("location") &&
               headerText.contains("number in stock") && headerText.contains("restock time") &&
               headerText.contains("price sold at") && headerText.contains("price bought at") &&
               headerText.contains("change per") && headerText.contains("members");
    }

    private static String findVersionForTable(Element table, String itemName) {
        Element previousHeader = table.previousElementSibling();
        while (previousHeader != null && !previousHeader.tagName().matches("h[1-6]")) {
            previousHeader = previousHeader.previousElementSibling();
        }
        if (previousHeader != null && previousHeader.text().contains(itemName)) {
            String version = previousHeader.text().replace(itemName, "").trim();
            return version.isEmpty() ? "Default" : version;
        }
        return "Default";
    }

    private static List<DropSource> parseDropSourceTable(Elements rows, String itemName) {
        List<DropSource> dropSources = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) { // Skip header row
            Element row = rows.get(i);
            Elements cells = row.select("td");
            if (cells.size() >= 4) {
                DropSource source = new DropSource();
                source.setSourceName(cells.get(0).text());
                source.setSourceLevel(parseIntOrDefault(cells.get(1).text(), -1));
                parseQuantity(cells.get(2).text(), source);
                source.setDropRate(parseDropRate(cells.get(3).text()));
                if (cells.size() > 4) {
                    source.setNotes(cells.get(4).text());
                }
                dropSources.add(source);
            }
        }
        logger.info("Parsed {} drop sources for {}", dropSources.size(), itemName);
        return dropSources;
    }

    private static List<ShopSource> parseShopSourceTable(Elements rows, String itemName) {
        List<ShopSource> shopSources = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) { // Skip header row
            Element row = rows.get(i);
            Elements cells = row.select("td");
            if (cells.size() >= 8) {
                ShopSource source = new ShopSource();
                source.setShopName(cells.get(0).text());
                source.setLocation(cells.get(1).text());
                source.setNumberInStock(parseIntOrDefault(cells.get(2).text(), 0));
                source.setRestockTime(cells.get(3).text());
                source.setPriceSoldAt(parseIntOrDefault(cells.get(4).text(), 0));
                source.setPriceBoughtAt(parseIntOrDefault(cells.get(5).text(), 0));
                source.setChangePercent(parseDoubleOrDefault(cells.get(6).text().replace("%", ""), 0.0));
                source.setMembers( String.valueOf(cells.get(7)).contains("Members"));
                if (cells.size() > 8) {
                    source.setNotes(cells.get(8).text());
                }
                shopSources.add(source);
            }
        }
        logger.info("Parsed {} shop sources for {}", shopSources.size(), itemName);
        return shopSources;
    }

    private static int parseIntOrDefault(String value, int defaultValue) {
        try {
            return Integer.parseInt(value.replaceAll("[^\\d-]", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static double parseDoubleOrDefault(String value, double defaultValue) {
        try {
            return Double.parseDouble(value.replaceAll("[^\\d.-]", ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static void parseQuantity(String quantityStr, DropSource source) {
        if (quantityStr.contains("–")) {
            String[] parts = quantityStr.split("–");
            source.setMinQuantity(parseIntOrDefault(parts[0], 1));
            source.setMaxQuantity(parseIntOrDefault(parts[1], 1));
        } else {
            int quantity = parseIntOrDefault(quantityStr, 1);
            source.setMinQuantity(quantity);
            source.setMaxQuantity(quantity);
        }
    }

    private static double parseDropRate(String dropRateStr) {
        if (dropRateStr.contains("/")) {
            String[] parts = dropRateStr.split("/");
            return (double) parseIntOrDefault(parts[0], 1) / parseIntOrDefault(parts[1], 1);
        } else if (dropRateStr.endsWith("%")) {
            return parseDoubleOrDefault(dropRateStr.replace("%", ""), 0) / 100;
        } else if (dropRateStr.equalsIgnoreCase("Always")) {
            return 1.0;
        }
        return 0.0;
    }

    public static List<String> parseDropSourcesFromHtmlOld(String html, String itemName) {
        List<String> dropSources = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements tables = doc.select("table.wikitable");
          // Find the table that follows the item-specific Drop sources template
       
        for (Element table : tables) {
            
            Elements rows = table.select("tr");
            
            if (rows.get(0).text().contains("Source") 
                && rows.get(0).text().contains("Level") 
                && rows.get(0).text().contains("Quantity") 
                && rows.get(0).text().contains("Rarity")) {
                logger.info("Header row contains Source, Level, Quantity, Rarity");
                logger.info(rows.get(0).text());
                logger.info("Number of rows: " + rows.size());
                logger.info("table context contains item name?: " + String.valueOf(table).contains(itemName));
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
    public static List<String> parseStoreLocationsFromHtmlOld(String html, String itemName) {
        List<String> storeLocations = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        Elements tables = doc.select("table.wikitable");
        
        for (Element table : tables) {
            Elements rows = table.select("tr");
            if ((rows.get(0).text().contains("Seller") 
                && rows.get(0).text().contains("Location") 
                && rows.get(0).text().contains("Number in stock") 
                && rows.get(0).text().contains("Restock time")
                && rows.get(0).text().contains("Price sold at")
                && rows.get(0).text().contains("Price bought at")
                && rows.get(0).text().contains("Change Per")
                && rows.get(0).text().contains("Members") )|| rows.get(0).select("td").size() >= 8
            ) {
                logger.info("Header row contains Seller, Location, Number in stock, Restock time, Price sold at, Price bought at, Change Per, Members");
                logger.info("Header: " + rows.get(0).text());
                logger.info("Number of rows: " + rows.size());
                logger.info("table context contains item name?: " + String.valueOf(table).contains(itemName));
            }else{
                continue;
            }
            for (int i = 1; i < rows.size(); i++) { // Skip header row
                Elements cells = rows.get(i).select("td");
                if (cells.size() == 8) {
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
}