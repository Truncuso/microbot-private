package net.runelite.client.plugins.VoxSylvaePlugins.scraper;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.ImageType;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiNPCInfo.WikiMonsterDrop;
import net.runelite.client.plugins.VoxSylvaePlugins.scraper.model.wikiNPCInfo.WikiNPCResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VSNPCScraperDropsTest {

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

    @Test
    void testMuggerDrops() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Mugger", true, false, false, ImageType.NORMAL, "");
        WikiNPCResult mugger = result.get("Mugger").get("Regular");
        assertNotNull(mugger);

        Map<String, List<WikiMonsterDrop>> drops = mugger.getDrops();
        assertNotNull(drops);

        // Test 100% drops
        List<WikiMonsterDrop> alwaysDrops = drops.get("100%");
        assertNotNull(alwaysDrops);
        assertEquals(1, alwaysDrops.size());
        assertEquals("Bones", alwaysDrops.get(0).getItem().getName());
        assertEquals(1.0, alwaysDrops.get(0).getRarity());

        // Test other drop categories
        assertTrue(drops.containsKey("Weapons and armour"));
        assertTrue(drops.containsKey("Runes and ammunition"));
        assertTrue(drops.containsKey("Herbs"));
        assertTrue(drops.containsKey("Coins"));
        assertTrue(drops.containsKey("Other"));
        assertTrue(drops.containsKey("Tertiary"));

        // Test specific drops
        List<WikiMonsterDrop> weapons = drops.get("Weapons and armour");
        assertTrue(weapons.stream().anyMatch(drop -> drop.getItem().getName().equals("Bronze med helm")));

        List<WikiMonsterDrop> runes = drops.get("Runes and ammunition");
        assertTrue(runes.stream().anyMatch(drop -> drop.getItem().getName().equals("Bronze bolts")));

        List<WikiMonsterDrop> herbs = drops.get("Herbs");
        assertFalse(herbs.isEmpty());

        List<WikiMonsterDrop> coins = drops.get("Coins");
        assertTrue(coins.stream().anyMatch(drop -> drop.getItem().getName().equals("Coins") && drop.getQuantity().get(0) == 3));

        List<WikiMonsterDrop> other = drops.get("Other");
        assertTrue(other.stream().anyMatch(drop -> drop.getItem().getName().equals("Nothing")));

        List<WikiMonsterDrop> tertiary = drops.get("Tertiary");
        assertTrue(tertiary.stream().anyMatch(drop -> drop.getItem().getName().equals("Clue scroll (beginner)")));
    }

    @Test
    void testAbyssalDemonDrops() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Abyssal demon", true, false, false, ImageType.NORMAL, "");
        WikiNPCResult abyssalDemon = result.get("Abyssal demon").get("Standard");
        assertNotNull(abyssalDemon);

        Map<String, List<WikiMonsterDrop>> drops = abyssalDemon.getDrops();
        assertNotNull(drops);

        // Test 100% drops
        List<WikiMonsterDrop> alwaysDrops = drops.get("100%");
        assertNotNull(alwaysDrops);
        assertEquals(1, alwaysDrops.size());
        assertEquals("Abyssal ashes", alwaysDrops.get(0).getItem().getName());

        // Test other drop categories
        assertTrue(drops.containsKey("Weapons and armour"));
        assertTrue(drops.containsKey("Runes"));
        assertTrue(drops.containsKey("Herbs"));
        assertTrue(drops.containsKey("Materials"));
        assertTrue(drops.containsKey("Coins"));
        assertTrue(drops.containsKey("Other"));
        assertTrue(drops.containsKey("Rare and Gem drop table"));
        assertTrue(drops.containsKey("Tertiary"));

        // Test specific drops
        List<WikiMonsterDrop> weapons = drops.get("Weapons and armour");
        assertTrue(weapons.stream().anyMatch(drop -> drop.getItem().getName().equals("Abyssal whip")));

        List<WikiMonsterDrop> runes = drops.get("Runes");
        assertTrue(runes.stream().anyMatch(drop -> drop.getItem().getName().equals("Blood rune")));

        List<WikiMonsterDrop> herbs = drops.get("Herbs");
        assertFalse(herbs.isEmpty());

        List<WikiMonsterDrop> materials = drops.get("Materials");
        assertTrue(materials.stream().anyMatch(drop -> drop.getItem().getName().equals("Pure essence")));

        List<WikiMonsterDrop> coins = drops.get("Coins");
        assertFalse(coins.isEmpty());

        List<WikiMonsterDrop> tertiary = drops.get("Tertiary");
        assertTrue(tertiary.stream().anyMatch(drop -> drop.getItem().getName().equals("Abyssal head")));
    }

    @Test
    void testZulrahDrops() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Zulrah", true, false, false, ImageType.NORMAL, "");
        WikiNPCResult zulrah = result.get("Zulrah").get("Standard");
        assertNotNull(zulrah);

        Map<String, List<WikiMonsterDrop>> drops = zulrah.getDrops();
        assertNotNull(drops);

        // Test 100% drops
        List<WikiMonsterDrop> alwaysDrops = drops.get("100% drops");
        assertNotNull(alwaysDrops);
        assertEquals(1, alwaysDrops.size());
        assertEquals("Zulrah's scales", alwaysDrops.get(0).getItem().getName());

        // Test other drop categories
        assertTrue(drops.containsKey("Uniques"));
        assertTrue(drops.containsKey("Mutagens"));
        assertTrue(drops.containsKey("Weapons and armour"));
        assertTrue(drops.containsKey("Runes"));
        assertTrue(drops.containsKey("Herbs"));
        assertTrue(drops.containsKey("Seeds"));
        assertTrue(drops.containsKey("Resources"));
        assertTrue(drops.containsKey("Other"));
        assertTrue(drops.containsKey("Rare drop table"));
        assertTrue(drops.containsKey("Tertiary"));
        // Test specific drops
        List<WikiMonsterDrop> uniques = drops.get("Uniques");
        assertTrue(uniques.stream().anyMatch(drop -> drop.getItem().getName().equals("Tanzanite fang")));

        List<WikiMonsterDrop> mutagens = drops.get("Mutagens");
        assertTrue(mutagens.stream().anyMatch(drop -> drop.getItem().getName().equals("Tanzanite mutagen")));
        List<WikiMonsterDrop> runes = drops.get("Runes");
        assertTrue(runes.stream().anyMatch(drop -> drop.getItem().getName().equals("Death rune")));

        List<WikiMonsterDrop> herbs = drops.get("Herbs");
        assertTrue(herbs.stream().anyMatch(drop -> drop.getItem().getName().equals("Torstol")));

        List<WikiMonsterDrop> seeds = drops.get("Seeds");
        assertTrue(seeds.stream().anyMatch(drop -> drop.getItem().getName().equals("Magic seed")));

        List<WikiMonsterDrop> resources = drops.get("Resources");
        assertTrue(resources.stream().anyMatch(drop -> drop.getItem().getName().equals("Pure essence")));

        List<WikiMonsterDrop> other = drops.get("Other");
        assertTrue(other.stream().anyMatch(drop -> drop.getItem().getName().equals("Zul-andra teleport")));

        List<WikiMonsterDrop> tertiary = drops.get("Tertiary");
        assertTrue(tertiary.stream().anyMatch(drop -> drop.getItem().getName().equals("Pet snakeling")));

        // Test drop rates
        WikiMonsterDrop tanzaniteFang = uniques.stream()
            .filter(drop -> drop.getItem().getName().equals("Tanzanite fang"))
            .findFirst()
            .orElse(null);
        assertNotNull(tanzaniteFang);
        assertEquals(1.0/1024, tanzaniteFang.getRarity(), 0.0001);

        WikiMonsterDrop petSnakeling = tertiary.stream()
            .filter(drop -> drop.getItem().getName().equals("Pet snakeling"))
            .findFirst()
            .orElse(null);
        assertNotNull(petSnakeling);
        assertEquals(1.0/4000, petSnakeling.getRarity(), 0.0001);
    }


    @Test
    void testVorkathDrops() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Vorkath", true, false, false, ImageType.NORMAL, "");
        WikiNPCResult vorkath = result.get("Vorkath").get("Standard");
        assertNotNull(vorkath);

        Map<String, List<WikiMonsterDrop>> drops = vorkath.getDrops();
        assertNotNull(drops);

        // Test 100% drops
        List<WikiMonsterDrop> alwaysDrops = drops.get("100%");
        assertNotNull(alwaysDrops);
        assertTrue(alwaysDrops.stream().anyMatch(drop -> drop.getItem().getName().equals("Vorkath's bones")));

        // Test other drop categories
        assertTrue(drops.containsKey("Weapons and armour"));
        assertTrue(drops.containsKey("Runes and ammunition"));
        assertTrue(drops.containsKey("Resources"));
        assertTrue(drops.containsKey("Seeds"));
        assertTrue(drops.containsKey("Other"));
        assertTrue(drops.containsKey("Tertiary"));

        // Test specific drops
        List<WikiMonsterDrop> weapons = drops.get("Weapons and armour");
        assertTrue(weapons.stream().anyMatch(drop -> drop.getItem().getName().equals("Dragonbone necklace")));

        List<WikiMonsterDrop> runes = drops.get("Runes and ammunition");
        assertTrue(runes.stream().anyMatch(drop -> drop.getItem().getName().equals("Dragon bolts (unf)")));

        List<WikiMonsterDrop> resources = drops.get("Resources");
        assertTrue(resources.stream().anyMatch(drop -> drop.getItem().getName().equals("Superior dragon bones")));

        List<WikiMonsterDrop> tertiary = drops.get("Tertiary");
        assertTrue(tertiary.stream().anyMatch(drop -> drop.getItem().getName().equals("Vorki")));

        // Test drop rates
        WikiMonsterDrop dracolicVisage = weapons.stream()
            .filter(drop -> drop.getItem().getName().equals("Draconic visage"))
            .findFirst()
            .orElse(null);
        assertNotNull(dracolicVisage);
        assertEquals(1.0/5000, dracolicVisage.getRarity(), 0.0001);
    }

    @Test
    void testCorporealBeastDrops() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Corporeal Beast", true, false, false, ImageType.NORMAL, "");
        WikiNPCResult corpBeast = result.get("Corporeal Beast").get("Standard");
        assertNotNull(corpBeast);

        Map<String, List<WikiMonsterDrop>> drops = corpBeast.getDrops();
        assertNotNull(drops);

        // Test unique drops
        List<WikiMonsterDrop> uniques = drops.get("Unique");
        assertNotNull(uniques);
        assertTrue(uniques.stream().anyMatch(drop -> drop.getItem().getName().equals("Spectral sigil")));
        assertTrue(uniques.stream().anyMatch(drop -> drop.getItem().getName().equals("Arcane sigil")));
        assertTrue(uniques.stream().anyMatch(drop -> drop.getItem().getName().equals("Elysian sigil")));

        // Test other drop categories
        assertTrue(drops.containsKey("Weapons and armour"));
        assertTrue(drops.containsKey("Runes"));
        assertTrue(drops.containsKey("Resources"));
        assertTrue(drops.containsKey("Other"));
        assertTrue(drops.containsKey("Tertiary"));

        // Test specific drops
        List<WikiMonsterDrop> resources = drops.get("Resources");
        assertTrue(resources.stream().anyMatch(drop -> drop.getItem().getName().equals("Cannonball")));

        List<WikiMonsterDrop> tertiary = drops.get("Tertiary");
        assertTrue(tertiary.stream().anyMatch(drop -> drop.getItem().getName().equals("Pet dark core")));

        // Test drop rates
        WikiMonsterDrop elysianSigil = uniques.stream()
            .filter(drop -> drop.getItem().getName().equals("Elysian sigil"))
            .findFirst()
            .orElse(null);
        assertNotNull(elysianSigil);
        assertEquals(1.0/4095, elysianSigil.getRarity(), 0.0001);
    }

    @Test
    void testBarrowsDrops() {
        Map<String, Map<String, WikiNPCResult>> result = scraper.getNPCInfo("Dharok the Wretched", true, false, false, ImageType.NORMAL, "");
        WikiNPCResult dharok = result.get("Dharok the Wretched").get("Standard");
        assertNotNull(dharok);

        Map<String, List<WikiMonsterDrop>> drops = dharok.getDrops();
        assertNotNull(drops);

        // Test Barrows equipment drops
        List<WikiMonsterDrop> barrowsEquipment = drops.get("Barrows equipment");
        assertNotNull(barrowsEquipment);
        assertTrue(barrowsEquipment.stream().anyMatch(drop -> drop.getItem().getName().equals("Dharok's greataxe")));
        assertTrue(barrowsEquipment.stream().anyMatch(drop -> drop.getItem().getName().equals("Dharok's platebody")));
        assertTrue(barrowsEquipment.stream().anyMatch(drop -> drop.getItem().getName().equals("Dharok's platelegs")));
        assertTrue(barrowsEquipment.stream().anyMatch(drop -> drop.getItem().getName().equals("Dharok's helm")));

        // Test other drop categories
        assertTrue(drops.containsKey("Runes"));
        assertTrue(drops.containsKey("Other"));

        // Test specific drops
        List<WikiMonsterDrop> runes = drops.get("Runes");
        assertTrue(runes.stream().anyMatch(drop -> drop.getItem().getName().equals("Death rune")));

        List<WikiMonsterDrop> other = drops.get("Other");
        assertTrue(other.stream().anyMatch(drop -> drop.getItem().getName().equals("Bolt rack")));

        // Test drop rates
        WikiMonsterDrop dharoksGreataxe = barrowsEquipment.stream()
            .filter(drop -> drop.getItem().getName().equals("Dharok's greataxe"))
            .findFirst()
            .orElse(null);
        assertNotNull(dharoksGreataxe);
        assertEquals(1.0/392, dharoksGreataxe.getRarity(), 0.0001);
    }
}