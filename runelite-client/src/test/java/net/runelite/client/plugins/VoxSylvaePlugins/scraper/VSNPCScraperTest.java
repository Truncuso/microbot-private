package net.runelite.client.plugins.VoxSylvaePlugins.scraper;

import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.WikiNPCResult;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VSNPCScraperTest {

    @Mock
    private Path mockDestination;

    @Mock
    private Path mockImageFolder;

    private VSNPCScraper scraper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        Map<String, Object> databaseDict = new HashMap<>();
        scraper = new VSNPCScraper(databaseDict, mockDestination, "testDatabase", mockImageFolder, true);
    }
    @AfterEach
    void tearDown() throws Exception {
        //closeable.close();
    }
    @Test
    void testGetNPCInfoSingleVersion() {
        // Test for a single version NPC (e.g., Gem trader)
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Gem trader", true, false, false, ImageType.NORMAL, "");
        
        assertNotNull(result);
        assertTrue(result.containsKey("Gem trader"));
        Map<String, WikiNPCResult> versions = result.get("Gem trader");
        assertEquals(1, versions.size());
        
        WikiNPCResult npc = versions.get("Standard");
        assertNotNull(npc);
        assertEquals("Gem trader", npc.getName());
        assertTrue(npc.getIds().contains(2874));
        //assertEquals("Male", npc.getGender());
        assertTrue(npc.getOptions().contains("Talk-to"));
        assertTrue(npc.getOptions().contains("Trade"));
        assertEquals("Makes his money selling rocks.", npc.getExamine());
        assertFalse(npc.isMembers());
        assertEquals("Human", npc.getRace());
    }

    @Test
    void testGetNPCInfoMultipleVersions() {
        // Test for a multiple version monster (e.g., Mugger)
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Mugger", true, false, false, ImageType.NORMAL, "");
        
        assertNotNull(result);
        assertTrue(result.containsKey("Mugger"));
        Map<String, WikiNPCResult> versions = result.get("Mugger");
        assertEquals(2, versions.size());
        
        WikiNPCResult regularMugger = versions.get("Regular");
        assertNotNull(regularMugger);
        assertEquals(0, regularMugger.getOptions().size());
        assertEquals("Mugger", regularMugger.getName());
        assertTrue(regularMugger.getIds().contains(513));
        assertTrue(regularMugger.getIds().contains(1461));
        assertTrue(regularMugger.getIds().contains(6996));
        assertEquals(6, regularMugger.getCombatLevel());
        assertEquals(8, regularMugger.getHitpoints());
        assertTrue(regularMugger.isAggressive());
        assertFalse(regularMugger.isPoisonous());
        assertEquals("Crush", regularMugger.getAttackStyle());
        
        WikiNPCResult varlamoreMugger = versions.get("Varlamore");
        assertNotNull(varlamoreMugger);
        assertEquals("Mugger", varlamoreMugger.getName());
        assertTrue(varlamoreMugger.getIds().contains(13282));
        assertEquals(6, varlamoreMugger.getCombatLevel());
        assertEquals(8, varlamoreMugger.getHitpoints());
        assertTrue(varlamoreMugger.isAggressive());
        assertFalse(varlamoreMugger.isPoisonous());
        assertEquals("Crush", varlamoreMugger.getAttackStyle());
    }

    @Test
    void testGetNPCInfoAbyssalDemon() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Abyssal demon", true, false, false, ImageType.NORMAL, "");
        
        assertNotNull(result);
        assertTrue(result.containsKey("Abyssal demon"));
        Map<String, WikiNPCResult> versions = result.get("Abyssal demon");
        assertEquals(3, versions.size());
        
        // Test common attributes for all versions
        for (WikiNPCResult demon : versions.values()) {
            assertEquals("Abyssal demon", demon.getName());
            assertEquals(124, demon.getCombatLevel());
            assertEquals(150, demon.getHitpoints());
            assertEquals(1, demon.getSize());
            assertEquals("A denizen of the Abyss!", demon.getExamine());
            assertTrue(demon.getAttributes().contains("demon"));
            assertEquals(0, demon.getXpBonus());
            assertEquals(8, demon.getMaxHit());
            assertFalse(demon.isAggressive());
            assertFalse(demon.isPoisonous());
            assertEquals("Stab", demon.getAttackStyle());
            assertEquals(4, demon.getAttackSpeed());
            assertEquals(85, demon.getSlayerLevel());
            assertEquals(150, demon.getSlayerXp());
            assertTrue(demon.isMembers());
            assertEquals(0, demon.getOptions().size());
            // Test combat stats
            assertEquals(97, demon.getAttackLevel());
            assertEquals(67, demon.getStrengthLevel());
            assertEquals(135, demon.getDefenceLevel());
            assertEquals(1, demon.getMagicLevel());
            assertEquals(1, demon.getRangedLevel());
            
            // Test bonuses and defences
            assertEquals(0, demon.getAttackBonus());
            assertEquals(0, demon.getStrengthBonus());
            assertEquals(20, demon.getStabDefence());
            assertEquals(20, demon.getSlashDefence());
            assertEquals(20, demon.getCrushDefence());
            assertEquals(0, demon.getMagicDefence());
            assertEquals(20, demon.getRangedDefence());
            
            // Test immunities
            assertFalse(demon.isImmunePoison());
            assertFalse(demon.isImmuneVenom());
            assertFalse(demon.isImmuneCannon());
            assertFalse(demon.isImmuneThrall());
        }
        
        // Test specific attributes for each version
        WikiNPCResult standardDemon = versions.get("Standard");
        assertNotNull(standardDemon);
        assertEquals("26 January 2005", standardDemon.getReleaseDate());
        assertEquals("Slayer Skill", standardDemon.getUpdateName());
        assertTrue(standardDemon.getIds().contains(415));
        assertTrue(standardDemon.getIds().contains(416));
        assertEquals(5, standardDemon.getRespawnTime());
        
        WikiNPCResult catacombsDemon = versions.get("Catacombs of Kourend");
        assertNotNull(catacombsDemon);
        assertEquals("9 June 2016", catacombsDemon.getReleaseDate());
        assertEquals("The Catacombs of Kourend", catacombsDemon.getUpdateName());
        assertTrue(catacombsDemon.getIds().contains(7241));
        assertEquals(15, catacombsDemon.getRespawnTime());
        
        WikiNPCResult wildernessDemon = versions.get("Wilderness Slayer Cave");
        assertNotNull(wildernessDemon);
        assertEquals("3 February 2022", wildernessDemon.getReleaseDate());
        assertEquals("Revenant Maledictus & Wilderness Changes", wildernessDemon.getUpdateName());
        assertTrue(wildernessDemon.getIds().contains(11239));
        assertEquals(5, wildernessDemon.getRespawnTime());
    }

    @Test
    void testGetNPCInfoNonExistent() {
        assertThrows(RuntimeException.class, () -> {
            scraper.getNPCInfo("NonExistentNPC", true, false, false, ImageType.NORMAL, "");
        });
    }

    @Test
    void testGetNPCInfoCaching() {
        // First call should fetch from wiki
        Map<String, Map<String, WikiNPCResult>> result1 = scraper.getNPCInfo("Goblin", true, false, false, ImageType.NORMAL, "");
        assertNotNull(result1);
        
        // Second call should fetch from cache
        Map<String, Map<String, WikiNPCResult>> result2 = scraper.getNPCInfo("Goblin", false, false, false, ImageType.NORMAL, "");
        assertNotNull(result2);
        
        // Results should be the same
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