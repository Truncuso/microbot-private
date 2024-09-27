package net.runelite.client.plugins.VoxSylvaePlugins.teleportation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.VoxSylvaePlugins.util.teleport.*;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.shortestpath.pathfinder.Pathfinder;

import javax.inject.Inject;

import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors; 

public class TeleportationManager{


    @Inject
    private Client client;

    private Map<TeleportType, List<Teleport>> teleports;
    private Map<String, MagicAction> teleportToMagicActionMap;
    private Teleport currentTeleport;
    private boolean isTeleporting;
    private int teleportStartTick;
    private Map<String, CachedPathInfo> distanceCache;
    private String cacheDir = "teleport_cache/";
    private WorldPoint currentTarget;
    private Future<?> currentPathfindingTask;
    private static final int CACHE_PROXIMITY_THRESHOLD = 5; // Tiles

    private static class CachedPathInfo {
        int distance;
        long timestamp;

        CachedPathInfo(int distance, long timestamp) {
            this.distance = distance;
            this.timestamp = timestamp;
        }
    }
    public TeleportationManager(Client client) {
        if (client == null) {
          this.client = Microbot.getClient();
        }else{
          this.client = client;
        }        
        
        initialize();
        this.teleports = new HashMap<>();
        this.teleportToMagicActionMap = new HashMap<>();
        this.isTeleporting = false;
        this.distanceCache = new HashMap<>();
        initializeTeleportToMagicActionMap();
        loadDistanceCache();
        

    }

    private void loadTeleportData() {
        loadStandardSpellbookTeleports();
        loadAncientMagicksTeleports();
        loadArceuusTeleports();
        loadLunarMagicTeleports();
        loadJewelryTeleports();
        loadFairyRingTeleports();
        loadPOHTeleports();
    }
    public void initialize() {
        this.teleports = new HashMap<>();
        this.isTeleporting = false;
        loadTeleportData();
        initializeTeleportToMagicActionMap();
    }
    private List<Teleport> loadTeleportsFromFile(String filename) {
        try (FileReader reader = new FileReader("../data/locationData/" + filename)) {
            Gson gson = new Gson();
            Type teleportListType = new TypeToken<ArrayList<Teleport>>(){}.getType();
            return gson.fromJson(reader, teleportListType);
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    private void initializeTeleportToMagicActionMap() {
        teleportToMagicActionMap.put("Varrock Teleport", MagicAction.VARROCK_TELEPORT);
        teleportToMagicActionMap.put("Lumbridge Teleport", MagicAction.LUMBRIDGE_TELEPORT);
        teleportToMagicActionMap.put("Falador Teleport", MagicAction.FALADOR_TELEPORT);
        teleportToMagicActionMap.put("Camelot Teleport", MagicAction.CAMELOT_TELEPORT);
        teleportToMagicActionMap.put("Ardougne Teleport", MagicAction.ARDOUGNE_TELEPORT);
        teleportToMagicActionMap.put("Watchtower Teleport", MagicAction.WATCHTOWER_TELEPORT);
        teleportToMagicActionMap.put("Teleport To House", MagicAction.TELEPORT_TO_HOUSE);
        teleportToMagicActionMap.put("Trollheim Teleport", MagicAction.TROLLHEIM_TELEPORT);
        teleportToMagicActionMap.put("Ape Atoll Teleport", MagicAction.APE_ATOLL_TELEPORT);
        teleportToMagicActionMap.put("Kourend Castle Teleport", MagicAction.KOUREND_CASTLE_TELEPORT);
        teleportToMagicActionMap.put("Teleport To Ape Atoll", MagicAction.TELEPORT_APE_ATOLL);
        //MagicAction BATTLEFRONT_TELEPORT= new MagicAction("Battlefront Teleport", 1, 5.5f, SpriteID.SPELL_WIND_STRIKE, false);
        // Arceuus spellbook teleports
        teleportToMagicActionMap.put("Arceuus Library Teleport", MagicAction.ARCEUUS_LIBRARY_TELEPORT);
        teleportToMagicActionMap.put("Draynor Manor Teleport", MagicAction.DRAYNOR_MANOR_TELEPORT);
        //teleportToMagicActionMap.put("Battlefront Teleport", MagicAction.BATTLEFRONT_TELEPORT);
        teleportToMagicActionMap.put("Mind Altar Teleport", MagicAction.MIND_ALTAR_TELEPORT);
        teleportToMagicActionMap.put("Respawn Teleport", MagicAction.RESPAWN_TELEPORT);
        teleportToMagicActionMap.put("Salve Graveyard Teleport", MagicAction.SALVE_GRAVEYARD_TELEPORT);
        teleportToMagicActionMap.put("Fenkenstrains Castle Teleport", MagicAction.FENKENSTRAINS_CASTLE_TELEPORT);
        teleportToMagicActionMap.put("West Ardougne Teleport", MagicAction.WEST_ARDOUGNE_TELEPORT);
        teleportToMagicActionMap.put("Harmony Island Teleport", MagicAction.HARMONY_ISLAND_TELEPORT);
        teleportToMagicActionMap.put("Cemetery Teleport", MagicAction.CEMETERY_TELEPORT);
        teleportToMagicActionMap.put("Barrows Teleport", MagicAction.BARROWS_TELEPORT);
        teleportToMagicActionMap.put("Ape Atoll Teleport", MagicAction.APE_ATOLL_TELEPORT);

        // Ancient Magicks teleports
        teleportToMagicActionMap.put("Paddewwa Teleport", MagicAction.PADDEWWA_TELEPORT);
        teleportToMagicActionMap.put("Senntisten Teleport", MagicAction.SENNTISTEN_TELEPORT);
        teleportToMagicActionMap.put("Kharyrll Teleport", MagicAction.KHARYRLL_TELEPORT);
        teleportToMagicActionMap.put("Lassar Teleport", MagicAction.LASSAR_TELEPORT);
        teleportToMagicActionMap.put("Dareeyak Teleport", MagicAction.DAREEYAK_TELEPORT);
        teleportToMagicActionMap.put("Carrallangar Teleport", MagicAction.CARRALLANGER_TELEPORT);
        teleportToMagicActionMap.put("Annakarl Teleport", MagicAction.ANNAKARL_TELEPORT);
        teleportToMagicActionMap.put("Ghorrock Teleport", MagicAction.GHORROCK_TELEPORT);

        // Lunar spellbook teleports
        teleportToMagicActionMap.put("Moonclan Teleport", MagicAction.MOONCLAN_TELEPORT);
        teleportToMagicActionMap.put("Ourania Teleport", MagicAction.OURANIA_TELEPORT);
        teleportToMagicActionMap.put("Waterbirth Teleport", MagicAction.WATERBIRTH_TELEPORT);
        teleportToMagicActionMap.put("Barbarian Teleport", MagicAction.BARBARIAN_TELEPORT);
        teleportToMagicActionMap.put("Khazard Teleport", MagicAction.KHAZARD_TELEPORT);
        teleportToMagicActionMap.put("Fishing Guild Teleport", MagicAction.FISHING_GUILD_TELEPORT);
        teleportToMagicActionMap.put("Catherby Teleport", MagicAction.CATHERBY_TELEPORT);
        teleportToMagicActionMap.put("Ice Plateau Teleport", MagicAction.ICE_PLATEAU_TELEPORT);
    }
    private void loadStandardSpellbookTeleports() {
        List<Teleport> standardTeleports = loadTeleportsFromFile("teleportationSpells.json");
        teleports.put(TeleportType.STANDARD_SPELLBOOK, standardTeleports);
    }

    private void loadAncientMagicksTeleports() {
        List<Teleport> ancientTeleports = loadTeleportsFromFile("ancientMagicksTeleports.json");
        teleports.put(TeleportType.ANCIENT_MAGICKS, ancientTeleports);
    }

    private void loadArceuusTeleports() {
        List<Teleport> arceuusTeleports = loadTeleportsFromFile("arceuusTeleports.json");
        teleports.put(TeleportType.ARCEUUS_SPELLBOOK, arceuusTeleports);
    }

    private void loadLunarMagicTeleports() {
        List<Teleport> lunarTeleports = loadTeleportsFromFile("lunarMagicTeleports.json");
        teleports.put(TeleportType.LUNAR_SPELLBOOK, lunarTeleports);
    }
    private void loadJewelryTeleports() {
        try (FileReader reader = new FileReader("../data/locationData/jewelryTeleports.json")) {
            Gson gson = new Gson();
            Type jewelryTeleportListType = new TypeToken<ArrayList<JewelryTeleport>>(){}.getType();
            List<JewelryTeleport> jewelryTeleports = gson.fromJson(reader, jewelryTeleportListType);
            
            // Convert List<JewelryTeleport> to List<Teleport>
            List<Teleport> teleportList = new ArrayList<>(jewelryTeleports.stream()
                .map(jt -> (Teleport) jt)
                .collect(Collectors.toList()));
            
            teleports.put(TeleportType.JEWELRY, teleportList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFairyRingTeleports() {
        try (FileReader reader = new FileReader("../data/locationData/fairyRingTeleports.json")) {
            Gson gson = new Gson();
            Type fairyRingTeleportListType = new TypeToken<ArrayList<FairyRingTeleport>>(){}.getType();
            List<FairyRingTeleport> fairyRingTeleports = gson.fromJson(reader, fairyRingTeleportListType);
            
            // Convert List<FairyRingTeleport> to List<Teleport>
            List<Teleport> teleportList = new ArrayList<>(fairyRingTeleports.stream()
                .map(frt -> (Teleport) frt)
                .collect(Collectors.toList()));
            
            teleports.put(TeleportType.FAIRY_RING, teleportList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPOHTeleports() {
        try (FileReader reader = new FileReader("../data/locationData/POHTeleportOptions.json")) {
            Gson gson = new Gson();
            Type pohTeleportListType = new TypeToken<ArrayList<POHTeleport>>(){}.getType();
            List<POHTeleport> pohTeleports = gson.fromJson(reader, pohTeleportListType);
            teleports.put(TeleportType.POH, new ArrayList<>(pohTeleports));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadDistanceCache() {
        File cacheFile = new File(cacheDir + "distance_cache.json");
        if (cacheFile.exists()) {
            try (Reader reader = new FileReader(cacheFile)) {
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, Integer>>(){}.getType();
                distanceCache = gson.fromJson(reader, type);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveDistanceCache() {
        File cacheDir = new File(this.cacheDir);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File cacheFile = new File(cacheDir, "distance_cache.json");
        try (Writer writer = new FileWriter(cacheFile)) {
            Gson gson = new Gson();
            gson.toJson(distanceCache, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void cleanOldCacheEntries(long maxAgeMillis) {
        long now = System.currentTimeMillis();
        distanceCache.entrySet().removeIf(entry -> now - entry.getValue().timestamp > maxAgeMillis);
    }

    public String getRegionName(WorldPoint point) {
        // This method should be implemented to fetch the region name from the OSRS Wiki
        // You might need to create a separate utility class for this functionality
        // For now, we'll return a placeholder
        return "Unknown Region";
    }

    // ... (other methods remain the same)



    public Teleport findNearestTeleport(WorldPoint destination) {
        if (currentTarget != null && currentTarget.equals(destination) && currentPathfindingTask != null && !currentPathfindingTask.isDone()) {
            return null; // Pathfinding is already in progress for this destination
        }

        setTarget(destination);
        
        CompletableFuture<Teleport> pathfindingFuture = new CompletableFuture<>();
        
        currentPathfindingTask = CompletableFuture.runAsync(() -> {
            Microbot.getClientThread().runOnSeperateThread(() -> {
                try {
                    Teleport nearest = null;
                    double minDistance = Double.MAX_VALUE;

                    for (List<Teleport> teleportList : teleports.values()) {
                        for (Teleport teleport : teleportList) {
                            double distance = getDistanceToViaShortestPath(teleport.getDestination(), destination);
                            if (distance < minDistance) {
                                minDistance = distance;
                                nearest = teleport;
                            }
                        }
                    }

                    pathfindingFuture.complete(nearest);
                } catch (Exception e) {
                    pathfindingFuture.completeExceptionally(e);
                }
                return null;
            });
        });

        try {
            return pathfindingFuture.get(30, TimeUnit.SECONDS); // Adjust timeout as needed
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }

    public double getDistanceToViaShortestPath(WorldPoint start, WorldPoint end) {
        // Check if there's a nearby cached path
        Optional<Map.Entry<String, CachedPathInfo>> nearbyCachedPath = findNearbyCachedPath(start, end);
        if (nearbyCachedPath.isPresent()) {
            return nearbyCachedPath.get().getValue().distance;
        }

        String cacheKey = start.toString() + "-" + end.toString();
        CompletableFuture<Integer> pathFuture = new CompletableFuture<>();
        
        currentPathfindingTask = CompletableFuture.runAsync(() -> {
            Microbot.getClientThread().runOnSeperateThread(() -> {
                try {
                    Pathfinder pathfinder = new Pathfinder(ShortestPathPlugin.getPathfinderConfig(), start, end);
                    while (!pathfinder.isDone()) {
                        Thread.sleep(100);
                    }
                    int distance = pathfinder.getPath().size();
                    pathFuture.complete(distance);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    pathFuture.completeExceptionally(e);
                }
                return null;
            });
        });

        try {
            int distance = pathFuture.get(5, TimeUnit.SECONDS);
            distanceCache.put(cacheKey, new CachedPathInfo(distance, System.currentTimeMillis()));
            saveDistanceCache();
            return distance;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return Double.MAX_VALUE;
        }
    }

    private Optional<Map.Entry<String, CachedPathInfo>> findNearbyCachedPath(WorldPoint start, WorldPoint end) {
        return distanceCache.entrySet().stream()
            .filter(entry -> {
                String[] points = entry.getKey().split("-");
                WorldPoint cachedStart = WorldPoint.fromString(points[0]);
                WorldPoint cachedEnd = WorldPoint.fromString(points[1]);
                return cachedStart.distanceTo(start) <= CACHE_PROXIMITY_THRESHOLD &&
                       cachedEnd.distanceTo(end) <= CACHE_PROXIMITY_THRESHOLD;
            })
            .min(Comparator.comparingLong(entry -> entry.getValue().timestamp));
    }
    private void setTarget(WorldPoint target) {
        if (!Microbot.isLoggedIn()) return;
        
        currentTarget = target;

        if (target == null) {
            if (currentPathfindingTask != null) {
                currentPathfindingTask.cancel(true);
                currentPathfindingTask = null;
            }
        }
    }
    public Teleport findNearestTeleport(WorldPoint destination) {
        Teleport nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (List<Teleport> teleportList : teleports.values()) {
            for (Teleport teleport : teleportList) {
                double distance = teleport.getDistanceTo(destination);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = teleport;
                }
            }
        }

        return nearest;
    }

    public Teleport findNearestTeleport(WorldPoint destination, TeleportType type) {
        List<Teleport> typeSpecificTeleports = teleports.get(type);
        if (typeSpecificTeleports == null) {
            return null;
        }

        return findNearestTeleportInList(destination, typeSpecificTeleports);
    }

    public Teleport findNearestTeleport(WorldPoint destination, List<String> completedQuests) {
        Teleport nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (List<Teleport> teleportList : teleports.values()) {
            for (Teleport teleport : teleportList) {
                if (meetsQuestRequirements(teleport, completedQuests)) {
                    double distance = teleport.getDistanceTo(destination);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearest = teleport;
                    }
                }
            }
        }

        return nearest;
    }

    private Teleport findNearestTeleportInList(WorldPoint destination, List<Teleport> teleportList) {
        Teleport nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Teleport teleport : teleportList) {
            double distance = teleport.getDistanceTo(destination);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = teleport;
            }
        }

        return nearest;
    }

    private boolean meetsQuestRequirements(Teleport teleport, List<String> completedQuests) {
        if (teleport.getRequiredQuests() == null || teleport.getRequiredQuests().isEmpty()) {
            return true;
        }
        return completedQuests.containsAll(teleport.getRequiredQuests());
    }

     public boolean performTeleport(Teleport teleport) {
        if (!canPerformTeleport(teleport)) {
            Microbot.showMessage("Cannot perform teleport: " + teleport.getName());
            return false;
        }

        if (teleport instanceof StandardTeleport) {
            return castTeleportSpell((StandardTeleport) teleport);
        } else if (teleport instanceof JewelryTeleport) {
            return useJewelryTeleport((JewelryTeleport) teleport);
        } else if (teleport instanceof POHTeleport) {
            return usePOHTeleport((POHTeleport) teleport);
        }

        Microbot.showMessage("Unsupported teleport type: " + teleport.getClass().getSimpleName());
        return false;
    }

    private boolean canPerformTeleport(Teleport teleport) {
        if (teleport.getRequiredLevel() > client.getRealSkillLevel(Skill.MAGIC)) {
            return false;
        }

        if (teleport.getRequiredItems() != null) {
            for (String item : teleport.getRequiredItems()) {
                if (!Rs2Inventory.hasItem(item)) {
                    Microbot.showMessage("Missing required item" + item+" for teleport: " + teleport.getName());
                    return false;
                }
            }
        }

        // Add more checks as needed (e.g., quest requirements)
        return true;
    }

    private boolean castTeleportSpell(StandardTeleport teleport) {
        MagicAction magicTeleportAction = teleportToMagicActionMap.get(teleport.getName());
        if (magicTeleportAction == null) {
            Microbot.showMessage("Unknown teleport spell: " + teleport.getName());
            return false;
        }

        Rs2Magic.cast(magicTeleportAction);
        
        sleepUntil(() -> {return Rs2Player.isIdle() && !Rs2Player.isAnimating(); }, 5000);
        //Microbot.log("teleport with"+teleport.getName() +  to location {}, current player location", , client.getLocalPlayer().getWorldLocation());
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        boolean success = playerLocation.distanceTo(teleport.getDestination()) < 5;
        if (success) {
            currentTeleport = teleport;
            isTeleporting = true;
            teleportStartTick = client.getTickCount();
        }
        return success;
    }

    private boolean useJewelryTeleport(JewelryTeleport teleport) {
        // Implement jewelry teleport logic
        // This might involve finding the jewelry in the inventory and using it
        return false; // Placeholder
    }

    private boolean usePOHTeleport(POHTeleport teleport) {
        // Implement POH teleport logic
        // This might involve interacting with POH objects
        return false; // Placeholder
    }

    public void onGameTick() {
        if (isTeleporting) {
            checkTeleportCompletion();
        }
    }

    private void checkTeleportCompletion() {
        Player player = client.getLocalPlayer();
        if (player == null) return;

        if (player.getAnimation() == -1 && client.getGameState() == GameState.LOGGED_IN) {
            int ticksSinceTeleport = client.getTickCount() - teleportStartTick;
            if (ticksSinceTeleport > 5) { // Adjust this value as needed
                WorldPoint playerLocation = player.getWorldLocation();
                if (currentTeleport.getDistanceTo(playerLocation) < 5) { // Teleport successful
                    isTeleporting = false;
                    currentTeleport = null;
                    Microbot.showMessage("Teleport completed successfully");
                } else {
                    isTeleporting = false;
                    currentTeleport = null;
                    Microbot.showMessage("Teleport failed");
                }
            }
        }
    }
    
    private void shutdown() {
        if (currentPathfindingTask != null) {
            currentPathfindingTask.cancel(true);
        }
        saveDistanceCache();
    }
    // Add more methods as needed, such as filtering by requirements, etc.
}