package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.api.WikipediaApi;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ItemResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.util.StringUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.CompletableFuture;
class VSItemScraperTest {

    private static final Logger logger = LoggerFactory.getLogger(VSItemScraperTest.class);
    
    private VSItemScraper itemScraper;
    private final Path testDestination = Paths.get("src/test/resources/test_data");
    private final Path testImageFolder = testDestination.resolve("images");

    @BeforeEach
    void setUp() {
        Map<String, Object> databaseDict = new HashMap<>();
        itemScraper = new VSItemScraper(databaseDict, testDestination, "testDB", testImageFolder, true);
    }

    @Test
    void testGetItemsInfo() {

        WikipediaApi api = new WikipediaApi();
        CompletableFuture<WikipediaPage> futurePageContent = api.getPageContent("Abyssal whip");

        try {
            WikipediaPage page = futurePageContent.get(); // This will block until the future completes
            //logger.info("Page title: " + page.getTitle());
            //logger.info("Page content length: " + page.getContent().length());
            //logger.info("First 100 characters of content: " + page.getContent());
        } catch (Exception e) {
            System.err.println("Error retrieving page content: " + e.getMessage());
            e.printStackTrace();
        }
        String itemNameSearch = "Abyssal whip";
        logger.info("Starting test for item: {}", itemNameSearch);
        
        Map<String, ItemResult> itemInfo = itemScraper.getItemsInfo(itemNameSearch, true, false, ImageType.NORMAL, testImageFolder.toString(), true);

        logger.info("Retrieved item info: {}", itemInfo);

        assertNotNull(itemInfo, "Item info should not be null");
        List<String> itemNames = itemScraper.getItemKey(itemNameSearch);
        for(String itemName : itemNames) {
        
            
            assertTrue(itemInfo.containsKey(itemName), "Item info should contain the requested item");

            ItemResult item = itemInfo.get(itemName);
            assertNotNull(item, "Item result should not be null");

            if (item != null) {
                logger.info("Item details: {}", item);
                
                    // Normalize strings for comparison
                String normalizedSearchName = normalizeItemName(itemName);
                String normalizedResultName = normalizeItemName(item.getNames().get(0));
                assertEquals(normalizedSearchName,normalizedResultName, "Item name should match");
                
                assertFalse(item.getIds().isEmpty(), "Item should have at least one ID");
                assertNotNull(item.getExamine(), "Item should have an examine text");
                assertNotNull(item.getHighAlchValue(), "Item should have a high alchemy value");
                
                assertTrue(item.getTradeable().get(0), "Abyssal whip should be tradeable");
                assertTrue(item.getEquipable().get(0), "Abyssal whip should be equipable");
                assertFalse(item.getStackable().get(0), "Abyssal whip should not be stackable");
            }
        }
        
    }
    private String normalizeItemName(String name) {
        return StringUtil.capitalizeEachWord(name.replace("_", " ").toLowerCase());
    }
}