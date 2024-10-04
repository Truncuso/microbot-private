package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ItemSources;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ShopSource;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.SpawnLocation;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikiItemResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.api.WikipediaApi;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.CombatStats;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.DropSource;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.util.StringUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VSWikiItemScraperTest {

    private static final Logger logger = LoggerFactory.getLogger(VSWikiItemScraperTest.class);
    
    private VSWikiItemScraper itemScraper;
    @Mock
    private WikipediaApi wikipediaApi;
    private final Path testDestination = Paths.get("src/test/resources/test_data");
    private final Path testImageFolder = testDestination.resolve("images");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        Map<String, Object> databaseDict = new HashMap<>();
        itemScraper = new VSWikiItemScraper(databaseDict, testDestination, "testDB", testImageFolder, true);
        
    }

    @Test
    void testGetItemsInfoAbyssalWhip() {
        Map<String, WikiItemResult> itemInfo = itemScraper.getItemsInfo("Abyssal whip", true, false, ImageType.NORMAL, testImageFolder.toString(), true);

        assertNotNull(itemInfo);
        assertTrue(itemInfo.containsKey("Abyssal whip"));
        WikiItemResult whip = itemInfo.get("Abyssal whip");

        assertEquals("Abyssal whip", whip.getName());
        assertEquals(4151, whip.getId());
        assertTrue(whip.isMembers());
        assertTrue(whip.isTradeable());
        assertTrue(whip.isEquipable());
        assertFalse(whip.isStackable());
        assertTrue(whip.isNoteable());
        assertEquals("A weapon from the Abyss.", whip.getExamine());
        assertEquals(120001, whip.getValue());
        assertEquals(0.453, whip.getWeight(), 0.001);

        CombatStats stats = whip.getCombatStats();
        assertNotNull(stats);
        assertEquals(82, stats.getAslash());
        assertEquals(82, stats.getStr());
        assertEquals("weapon", stats.getSlot());
        assertEquals(4, stats.getSpeed());
        assertEquals(1, stats.getAttackRange());
        assertEquals("Whip", stats.getCombatStyle());
    }

    @Test
    void testGetItemsInfoAbyssalDagger() {
        Map<String, WikiItemResult> itemInfo = itemScraper.getItemsInfo("Abyssal dagger", true, false, ImageType.NORMAL, testImageFolder.toString(), true);

        assertNotNull(itemInfo);
        assertEquals(4, itemInfo.size());

        String[] versions = {"Abyssal dagger", "Abyssal dagger (p)", "Abyssal dagger (p+)", "Abyssal dagger (p++)"};
        int[] ids = {13265, 13267, 13269, 13271};

        for (int i = 0; i < versions.length; i++) {
            WikiItemResult dagger = itemInfo.get(versions[i]);
            assertNotNull(dagger);
            assertEquals(versions[i], dagger.getName());
            assertEquals(ids[i], dagger.getId());
            assertTrue(dagger.isMembers());
            assertTrue(dagger.isTradeable());
            assertTrue(dagger.isEquipable());
            assertFalse(dagger.isStackable());
            assertTrue(dagger.isNoteable());
            assertTrue(dagger.getExamine().contains("Something sharp"));
            assertEquals(115001 + i, dagger.getValue());
            assertEquals(0.453, dagger.getWeight(), 0.001);

            CombatStats stats = dagger.getCombatStats();
            assertNotNull(stats);
            assertEquals(75, stats.getAstab());
            assertEquals(40, stats.getAslash());
            assertEquals(75, stats.getStr());
            assertEquals("weapon", stats.getSlot());
            assertEquals(4, stats.getSpeed());
            assertEquals(1, stats.getAttackRange());
            assertEquals("Stab Sword", stats.getCombatStyle());
        }
    }

    @Test
    void testGetItemsInfoBucketOfWater() {
        Map<String, WikiItemResult> itemInfo = itemScraper.getItemsInfo("Bucket of water", true, false, ImageType.NORMAL, testImageFolder.toString(), true);

        assertNotNull(itemInfo);
        assertTrue(itemInfo.containsKey("Bucket of water"));
        WikiItemResult bucket = itemInfo.get("Bucket of water");

        assertEquals("Bucket of water", bucket.getName());
        assertEquals(1929, bucket.getId());
        assertFalse(bucket.isMembers());
        assertTrue(bucket.isTradeable());
        assertFalse(bucket.isEquipable());
        assertFalse(bucket.isStackable());
        assertTrue(bucket.isNoteable());
        assertEquals("It's a bucket of water.", bucket.getExamine());
        assertEquals(6, bucket.getValue());
        assertEquals(3.000, bucket.getWeight(), 0.001);

        // Check for spawn locations
        ItemSources itemSources = bucket.getItemSources();
        assertNotNull(itemSources);
        assertTrue(itemSources.hasDropSources());
        assertTrue(itemSources.hasSpawnLocations());
        List<SpawnLocation> spawnLocations = itemSources.getSpawnLocations();
        
        

        //List<SpawnLocation> spawnLocations = scraper.parseSpawnLocations(wikiText);

        assertNotNull(spawnLocations);
        assertEquals(17, spawnLocations.size());

        // Test the complex cases
        testSpawnLocation(spawnLocations.get(0), "Cam Torum", "The Lost Pickaxe", true, 2, -1, 1435, 9587);
        testSpawnLocation(spawnLocations.get(16), "Shields of Mistrock", null, true, 1, -1, 1388, 2866);
        
        //assertTrue(spawnLocations.contains("Catherby - Caleb's house"));
        //assertTrue(spawnLocations.contains("Meiyerditch - all around the city"));

        // Check for shop locations
        List<ShopSource> shopSources = itemSources.getShopSources();
        assertNotNull(shopSources);

        assertEquals(7, shopSources.size());
        for (ShopSource shopSource : shopSources) {
            if (shopSource.getShopName().equals("General Store")) {
                assertEquals("General Store", shopSource.getShopName());
                assertEquals("Sells 5", shopSource.getNotes());
            }else if (shopSource.getShopName().equals("Wydin's Food Store")) {
                assertEquals("Wydin's Food Store", shopSource.getShopName());
                assertEquals("Sells 10", shopSource.getNotes());
            }
            
        }
        // Add assertions for specific shop locations if available in the scraper
    }
    @Test
    void testParseShopSources() {
        String wikiText = "===Shop locations===\n{{Store locations list|Needle}}";
        String itemName = "Needle";
    
        // Mock the WikipediaApi response
        List<String> mockStoreLocations = Arrays.asList(
            "Rommik's Crafty Supplies\tRimmington\t3\t1m\t1\t0\t2.0%\tYes",
            "Raetul and Co's Cloth Store\tSophanem\t20\t30s\t1\t0\t1.0%\tYes\tafter Contact!",
            "Fancy Clothes Store\tVarrock\t3\t1m\t1\t0\t2.0%\tNo"
        );
        when(wikipediaApi.getStoreLocations(itemName)).thenReturn(CompletableFuture.completedFuture(mockStoreLocations));
    
        List<ShopSource> shopSources = itemScraper.parseShopSources(wikiText, itemName);
    
        assertEquals(3, shopSources.size());
    
        ShopSource shop1 = shopSources.get(0);
        assertEquals("Rommik's Crafty Supplies", shop1.getShopName());
        assertEquals("Rimmington", shop1.getLocation());
        assertEquals(3, shop1.getNumberInStock());
        assertEquals("1m", shop1.getRestockTime());
        assertEquals(1, shop1.getPriceSoldAt());
        assertEquals(0, shop1.getPriceBoughtAt());
        assertEquals(2.0, shop1.getChangePercent(), 0.001);
        assertTrue(shop1.isMembers());
    
        ShopSource shop2 = shopSources.get(1);
        assertEquals("Raetul and Co's Cloth Store", shop2.getShopName());
        assertEquals("Sophanem", shop2.getLocation());
        assertEquals("after Contact!", shop2.getNotes());
    
        ShopSource shop3 = shopSources.get(2);
        assertEquals("Fancy Clothes Store", shop3.getShopName());
        assertEquals("Varrock", shop3.getLocation());
        assertFalse(shop3.isMembers());
    }
    @Test
    void testParseDropSources() {
        String wikiText = "===Drop sources===\n{{Drop sources|Steel arrow}}";
        String itemName = "Steel arrow";

        // Mock the WikipediaApi response
        List<String> mockDropSources = Arrays.asList(
            "Accumulator max cape\tN/A\t1\t1/1,013",
            "Ava's accumulator\tN/A\t1\t1/1,013",
            "Zombie\t13–76\t5–14\t1/5",
            "Tortoise\t79\t12–23\t1/6.4\tNo riders",
            "Strange shrine\tN/A\t0–632\t1/9"
        );
        try {
//          when(wikipediaApi.getDropSources(itemName).get()).thenAnswer(invocation ->mockDropSources);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<String, WikiItemResult> itemInfo = itemScraper.getItemsInfo(itemName,  true, false, ImageType.NORMAL, testImageFolder.toString(), true);
        List<DropSource> dropSources = itemScraper.parseDropSources(wikiText, itemName);
        assertEquals(8, dropSources.size());

        DropSource source1 = dropSources.get(0);
        assertEquals("Accumulator max cape", source1.getSourceName());
        assertEquals(-1, source1.getSourceLevel());
        assertEquals(1, source1.getMinQuantity());
        assertEquals(1, source1.getMaxQuantity());
        assertEquals(1.0/1013, source1.getDropRate(), 0.00001);

        DropSource source3 = dropSources.get(2);
        assertEquals("Zombie", source3.getSourceName());
        assertEquals(13, source3.getSourceLevel());
        assertEquals(5, source3.getMinQuantity());
        assertEquals(14, source3.getMaxQuantity());
        assertEquals(1.0/5, source3.getDropRate(), 0.00001);

        DropSource source4 = dropSources.get(3);
        assertEquals("Tortoise", source4.getSourceName());
        assertEquals(79, source4.getSourceLevel());
        assertEquals(12, source4.getMinQuantity());
        assertEquals(23, source4.getMaxQuantity());
        assertEquals(1.0/6.4, source4.getDropRate(), 0.00001);
        assertEquals("No riders", source4.getNotes());
    }
    @Test
    void testGetItemsInfoHammer() {
        Map<String, WikiItemResult> itemInfo = itemScraper.getItemsInfo("Hammer", true, false, ImageType.NORMAL, testImageFolder.toString(), true);

        assertNotNull(itemInfo);
        assertTrue(itemInfo.containsKey("Hammer"));
        WikiItemResult Hammer = itemInfo.get("Hammer");

        assertEquals("Hammer", Hammer.getName());
        //assertEquals(1929, bucket.getId());
        assertFalse(Hammer.isMembers());
        assertTrue(Hammer.isTradeable());
        assertFalse(Hammer.isEquipable());
        assertFalse(Hammer.isStackable());
        assertTrue(Hammer.isNoteable());
        //assertEquals("It's a bucket of water.", bucket.getExamine());
        //assertEquals(6, bucket.getValue());
        //assertEquals(3.000, bucket.getWeight(), 0.001);

        // Check for spawn locations
        ItemSources itemSources = Hammer.getItemSources();
        assertNotNull(itemSources);
        assertTrue(itemSources.hasDropSources());
        assertTrue(itemSources.hasSpawnLocations());
        List<SpawnLocation> spawnLocations = itemSources.getSpawnLocations();
        
        

        //List<SpawnLocation> spawnLocations = scraper.parseSpawnLocations(wikiText);

        assertNotNull(spawnLocations);
        
        assertEquals(23, spawnLocations.size());

        // Test a few specific spawn locations
        testSpawnLocation(spawnLocations.get(0), "Burgh de Rott", "upstairs", true, 1, -1, 3492, 3242);
        testSpawnLocation(spawnLocations.get(1), "Cam Torum Blacksmith", null, true, 1, -1, 1452, 9587);
        testSpawnLocation(spawnLocations.get(8), "Falador", "upstairs of the furnace building", false, 1, -1, 2975, 3368);
        testSpawnLocation(spawnLocations.get(11), "Karamja", "Ship Yard", true, 0, -1, 2954, 3060, 2963, 3058, 2986, 3048, 2989, 3061);
        testSpawnLocation(spawnLocations.get(14), "Observatory Dungeon", null, true, 0, 10122, 2351, 9383);
        testSpawnLocation(spawnLocations.get(18), "Slayer Tower", "basement", true, 0, 14, 3429, 9931, 3442, 9946);
        testSpawnLocation(spawnLocations.get(22), "Mistrock", null, true, 0, -1, 1401, 2866);
        
        //assertTrue(spawnLocations.contains("Catherby - Caleb's house"));
        //assertTrue(spawnLocations.contains("Meiyerditch - all around the city"));

        // Check for shop locations
        List<ShopSource> shopList = itemSources.getShopSources();
        assertNotNull(shopList);
        // Add assertions for specific shop locations if available in the scraper
    }
    private void testSpawnLocation(SpawnLocation location, String expectedLocation, String expectedSublocation, 
            boolean expectedMembers, int expectedPlane, int expectedMapID, int... expectedCoords) {
        assertEquals(expectedLocation, location.getLocation());
        assertEquals(expectedSublocation, location.getSublocation());
        assertEquals(expectedMembers, location.isMembers());
        assertEquals(expectedPlane, location.getPlane());
        assertEquals(expectedMapID, location.getMapID());

        List<SpawnLocation.Coordinate> coordinates = location.getCoordinates();
        assertEquals(expectedCoords.length / 2, coordinates.size());
        for (int i = 0; i < expectedCoords.length; i += 2) {
        assertEquals(expectedCoords[i], coordinates.get(i/2).getX());
        assertEquals(expectedCoords[i+1], coordinates.get(i/2).getY());
        }
    }


    // Add more test methods for other items or edge cases as needed
}