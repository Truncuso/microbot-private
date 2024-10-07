package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikipediaPage;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.CombatStats;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.DropSource;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.ItemSources;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.ShopSource;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.SpawnLocation;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiItemInfo.WikiItemResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.api.WikipediaApi;
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
        String itemName = "Needle";
        
        WikipediaPage page = itemScraper.getWikiPage(itemName);
        assertNotNull(page, "Failed to retrieve wiki page for item: " + itemName);
        
        String wikiText = page.getContent();
        List<ShopSource> shopSources = itemScraper.parseShopSources(wikiText, itemName);

        assertFalse(shopSources.isEmpty(), "Shop sources list should not be empty");
        
        // Test the total number of shop sources
        assertEquals(23, shopSources.size(), "There should be 23 shop sources for the needle");

        // Test a selection of shop sources to cover various cases
        testShopSource(shopSources, "Arnold's Eclectic Supplies.", "Piscatoris Fishing Colony", 3, "36s", 1, 0, 2.0, true);
        testShopSource(shopSources, "Artima's Crafting Supplies", "Civitas illa Fortis", 5, "6s", 1, 0, 2.0, true);
        testShopSource(shopSources, "Cam Torum General Store", "Cam Torum", 5, "12s", 1, 0, 3.0, true);
        testShopSource(shopSources, "Carefree Crafting Stall", "Keldagrim", 3, "1m", 1, 0, 3.0, true);
        testShopSource(shopSources, "Fancy Clothes Store", "Varrock", 3, "1m", 1, 0, 2.0, true);
        testShopSource(shopSources, "General Store (Canifis)", "Canifis", 2, "1m", 2, 0, 3.0, true);
        testShopSource(shopSources, "Jamila's Craft Stall", "Sophanem", 3, "1m", 1, 0, 1.0, true);
        testShopSource(shopSources, "Moon Clan Fine Clothes.", "Lunar Isle", 5, "06s", 1, 0, 1.0, true);
        testShopSource(shopSources, "Raetul and Co's Cloth Store.", "Sophanem", 20, "30s", 1, 0, 1.0, true, "after Contact!");

        // Test for some specific cases
        ShopSource raetulBeforeContact = findShopSource(shopSources, "Raetul and Co's Cloth Store");
        assertNotNull(raetulBeforeContact);
        assertEquals(15, raetulBeforeContact.getNumberInStock());
        assertEquals("16.8s", raetulBeforeContact.getRestockTime());
        assertEquals(1.0, raetulBeforeContact.getChangePercent(), 0.001);
        assertNull(raetulBeforeContact.getNotes());

        ShopSource fortisSilkStall = findShopSource(shopSources, "Fortis Silk Stall");
        assertNotNull(fortisSilkStall);
        assertEquals(5, fortisSilkStall.getNumberInStock());
        assertEquals("3s", fortisSilkStall.getRestockTime());
        assertEquals(0.0, fortisSilkStall.getChangePercent(), 0.001);
    }

    private void testShopSource(List<ShopSource> sources, String name, String location, int stock, 
                                String restockTime, int priceSold, int priceBought, double changePercent, 
                                boolean members, String... notes) {
        ShopSource source = findShopSource(sources, name);
        assertNotNull(source, "Shop source not found: " + name);
        assertEquals(name, source.getShopName());
        assertEquals(location, source.getLocation());
        assertEquals(stock, source.getNumberInStock());
        assertEquals(restockTime, source.getRestockTime());
        assertEquals(priceSold, source.getPriceSoldAt());
        assertEquals(priceBought, source.getPriceBoughtAt());
        assertEquals(changePercent, source.getChangePercent(), 0.001);
        assertEquals(members, source.isMembers());
        if (notes.length > 0) {
            assertEquals(notes[0], source.getNotes());
        }
    }

    private ShopSource findShopSource(List<ShopSource> sources, String name) {
        return sources.stream()
            .filter(source -> source.getShopName().equals(name))
            .findFirst()
            .orElse(null);
    }
    void testParseDropSources() {
        String itemName = "Steel arrow";
        
        WikipediaPage page = itemScraper.getWikiPage(itemName);
        assertNotNull(page, "Failed to retrieve wiki page for item: " + itemName);
        
        String wikiText = page.getContent();
        List<DropSource> dropSources = itemScraper.parseDropSources(wikiText, itemName);
    
        assertFalse(dropSources.isEmpty(), "Drop sources list should not be empty");
        
        // Test a selection of drop sources to cover various cases
        testDropSource(dropSources, "Accumulator max cape", -1, 1, 1, 1975.0/2000);
        testDropSource(dropSources, "Alexis", 24, 50, 50, 10.0/512);
        testDropSource(dropSources, "Angry barbarian spirit", 166, 20, 20, 3.0/128);
        testDropSource(dropSources, "Balfrug Kreeyath", 151, 95, 100, 7.0/127);
        testDropSource(dropSources, "Dagannoth Supreme", 303, 50, 250, 5.0/128);
        testDropSource(dropSources, "Guard (H.A.M. Storerooms)", 20, 1, 13, 6.0/500);
        testDropSource(dropSources, "Strange shrine", -1, 0, 632, 1.0/9);
        testDropSource(dropSources, "Zombie", 13, 5, 14, 20.0/100);
        testDropSource(dropSources, "Ava's attractor", -1, 1, 1, 1.0/2000);
        testDropSource(dropSources, "Broken arrow", -1, 1, 1, 10.0/100);
    
        // Test for some specific cases
        DropSource elaborateLockbox = findDropSource(dropSources, "Elaborate lockbox");
        assertNotNull(elaborateLockbox);
        assertEquals(2, elaborateLockbox.getMinQuantity());
        assertEquals(2, elaborateLockbox.getMaxQuantity());
        assertEquals(6.0/50, elaborateLockbox.getDropRate(), 0.00001);
        assertEquals("2 ×", elaborateLockbox.getNotes());
    
        DropSource guard = findDropSource(dropSources, "Guard");
        assertNotNull(guard);
        assertEquals("19; 20; 21; 22", guard.getNotes());
    
        // Verify the total number of drop sources
        assertTrue(dropSources.size() > 80, "There should be more than 80 drop sources");
    }
    
    private void testDropSource(List<DropSource> sources, String name, int expectedLevel, 
                                int expectedMinQuantity, int expectedMaxQuantity, double expectedDropRate) {
        DropSource source = findDropSource(sources, name);
        assertNotNull(source, "Drop source not found: " + name);
        assertEquals(name, source.getSourceName());
        assertEquals(expectedLevel, source.getSourceLevel());
        assertEquals(expectedMinQuantity, source.getMinQuantity());
        assertEquals(expectedMaxQuantity, source.getMaxQuantity());
        assertEquals(expectedDropRate, source.getDropRate(), 0.00001);
    }
    
    private DropSource findDropSource(List<DropSource> sources, String name) {
        return sources.stream()
            .filter(source -> source.getSourceName().equals(name))
            .findFirst()
            .orElse(null);
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