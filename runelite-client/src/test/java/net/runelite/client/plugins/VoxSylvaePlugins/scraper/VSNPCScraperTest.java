package net.runelite.client.plugins.VoxSylvaePlugins.scraper;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiNPCInfo.WikiMonsterDrop;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiNPCInfo.WikiNPCResult;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;

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




class VSNPCScraperTest {
    private static final Logger logger = LoggerFactory.getLogger(VSWikiItemScraperTest.class);
    
    private VSWikiItemScraper itemScraper;
    private final Path testDestination = Paths.get("src/test/resources/test_data");
    private final Path testImageFolder = testDestination.resolve("images");
    
    

    
    

    private VSNPCScraper scraper;

    @BeforeEach
    void setUp() {
        
        Map<String, Object> databaseDict = new HashMap<>();
        scraper = new VSNPCScraper(databaseDict, testDestination, "testDatabase", testImageFolder, true);
    }

    @Test
    void testGetNPCInfoSingleVersion() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Gem trader", true, false, false, ImageType.NORMAL, "");
        
        assertNotNull(result);
        assertTrue(result.containsKey("Gem trader"));
        Map<String, WikiNPCResult> versions = result.get("Gem trader");
        assertEquals(1, versions.size());
        
        WikiNPCResult npc = versions.get("Standard");
        assertNotNull(npc);
        assertEquals("Gem trader", npc.getName());
        assertTrue(npc.getIds().contains(2874));
        assertTrue(npc.getOptions().contains("Talk-to"));
        assertTrue(npc.getOptions().contains("Trade"));
        assertEquals("Makes his money selling rocks.", npc.getExamine());
        assertFalse(npc.isMembers());
        assertEquals("Human", npc.getRace());
    }

    @Test
    void testGetNPCInfoMultipleVersions() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Mugger", true, false, false, ImageType.NORMAL, "");
        
        assertNotNull(result);
        assertTrue(result.containsKey("Mugger"));
        Map<String, WikiNPCResult> versions = result.get("Mugger");
        assertEquals(2, versions.size());
        
        WikiNPCResult regularMugger = versions.get("Regular");
        assertNotNull(regularMugger);
        assertEquals("Mugger", regularMugger.getName());
        assertTrue(regularMugger.getIds().contains(513));
        assertEquals(6, regularMugger.getCombatLevel());
        assertEquals(8, regularMugger.getHitpoints());
        assertTrue(regularMugger.isAggressive());
        assertFalse(regularMugger.isPoisonous());
        assertEquals("Crush", regularMugger.getAttackStyle());
        
        WikiNPCResult varlamoreMugger = versions.get("Varlamore");
        assertNotNull(varlamoreMugger);
        assertEquals("Mugger", varlamoreMugger.getName());
        assertTrue(varlamoreMugger.getIds().contains(13282));
    }

    @Test
    void testGetNPCInfoAbyssalDemon() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Abyssal demon", true, false, false, ImageType.NORMAL, "");
        
        assertNotNull(result);
        assertTrue(result.containsKey("Abyssal demon"));
        Map<String, WikiNPCResult> versions = result.get("Abyssal demon");
        assertEquals(3, versions.size());
        
        for (WikiNPCResult demon : versions.values()) {
            assertEquals("Abyssal demon", demon.getName());
            assertEquals(124, demon.getCombatLevel());
            assertEquals(150, demon.getHitpoints());
            assertEquals("A denizen of the Abyss!", demon.getExamine());
            assertTrue(demon.getAttributes().contains("demon"));
            assertFalse(demon.isAggressive());
            assertFalse(demon.isPoisonous());
            assertEquals("Stab", demon.getAttackStyle());
            assertTrue(demon.isMembers());
        }
        
        WikiNPCResult standardDemon = versions.get("Standard");
        assertNotNull(standardDemon);
        assertEquals("26 January 2005", standardDemon.getReleaseDate());
        assertTrue(standardDemon.getIds().contains(415));
        
        WikiNPCResult catacombsDemon = versions.get("Catacombs of Kourend");
        assertNotNull(catacombsDemon);
        assertEquals("9 June 2016", catacombsDemon.getReleaseDate());
        assertTrue(catacombsDemon.getIds().contains(7241));
        
        WikiNPCResult wildernessDemon = versions.get("Wilderness Slayer Cave");
        assertNotNull(wildernessDemon);
        assertEquals("3 February 2022", wildernessDemon.getReleaseDate());
        assertTrue(wildernessDemon.getIds().contains(11239));
    }

    @Test
    void testGetNPCInfoNonExistent() {
        assertThrows(RuntimeException.class, () -> {
            scraper.getNPCInfo("NonExistentNPC", true, false, false, ImageType.NORMAL, "");
        });
    }

    @Test
    void testGetNPCInfoCaching() {
        Map<String, Map<String, WikiNPCResult>> result1 = scraper.getNPCInfo("Goblin", true, false, false, ImageType.NORMAL, "");
        assertNotNull(result1);
        
        Map<String, Map<String, WikiNPCResult>> result2 = scraper.getNPCInfo("Goblin", false, false, false, ImageType.NORMAL, "");
        assertNotNull(result2);
        
        assertEquals(result1, result2);
    }

    @Test
    void testGetNPCInfoWithImageDownload() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Zulrah", true, true, false, ImageType.NORMAL, "testPath");
        assertNotNull(result);
        assertTrue(result.containsKey("Zulrah"));
        Map<String, WikiNPCResult> versions = result.get("Zulrah");
        assertFalse(versions.isEmpty());
        for (WikiNPCResult npc : versions.values()) {
            assertNotNull(npc.getImagePath());
            assertTrue(npc.getImagePath().contains("testPath"));
        }
    }
}