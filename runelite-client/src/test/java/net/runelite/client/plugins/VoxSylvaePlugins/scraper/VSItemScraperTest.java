package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikiItemResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.CombatStats;
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

class VSWikiItemScraperTest {

    private static final Logger logger = LoggerFactory.getLogger(VSWikiItemScraperTest.class);
    
    private VSWikiItemScraper itemScraper;
    private final Path testDestination = Paths.get("src/test/resources/test_data");
    private final Path testImageFolder = testDestination.resolve("images");

    @BeforeEach
    void setUp() {
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
        List<String> spawnLocations = bucket.getSpawnLocations();
        assertNotNull(spawnLocations);
        assertTrue(spawnLocations.contains("Catherby - Caleb's house"));
        assertTrue(spawnLocations.contains("Meiyerditch - all around the city"));

        // Check for shop locations
        List<String> shopLocations = bucket.getShopLocations();
        assertNotNull(shopLocations);
        // Add assertions for specific shop locations if available in the scraper
    }

    // Add more test methods for other items or edge cases as needed
}