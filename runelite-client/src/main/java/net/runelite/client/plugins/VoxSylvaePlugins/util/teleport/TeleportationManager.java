package net.runelite.client.plugins.VoxSylvaePlugins.util.teleport;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.plugins.VoxSylvaePlugins.data.teleportationData.FairyRingTeleportLoader;
import net.runelite.client.plugins.VoxSylvaePlugins.data.teleportationData.SpellbookTeleportLoader;
import net.runelite.client.plugins.VoxSylvaePlugins.data.teleportationData.TeleportItemLoader;
import net.runelite.client.plugins.VoxSylvaePlugins.util.VoxSylvaeInventoryAndBankManagementScript;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.magic.Rs2Magic;
import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.skillcalculator.skills.MagicAction;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.shortestpath.Transport;

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
    @Inject
    private VoxSylvaeInventoryAndBankManagementScript inventoryAndBankManagementScript;

    private Map<TeleportType, List<Teleport>> teleports;
    private Map<String, MagicAction> teleportToMagicActionMap;
    private Teleport currentTeleport;
    private boolean isTeleporting;
    private int teleportStartTick;
    private Map<String, CachedPathInfo> distanceCache;
    private String cacheDir = "../.../data/teleportCacheData/";
    private WorldPoint currentTarget;
    private Future<?> currentPathfindingTask;
    private ExecutorService pathfindingExecutor;
    private static final int CACHE_PROXIMITY_THRESHOLD = 5; // Tiles
    
    private static class CachedPathInfo {
        int distance;
        long timestamp;

        CachedPathInfo(int distance, long timestamp) {
            this.distance = distance;
            this.timestamp = timestamp;
        }
    }

    private static class TeleportDistancePair {
        private final Teleport teleport;
        private final double distance;

        TeleportDistancePair(Teleport teleport, double distance) {
            this.teleport = teleport;
            this.distance = distance;
        }

        public Teleport getTeleport() {
            return teleport;
        }

        public double getDistance() {
            return distance;
        }
    }
   
    public TeleportationManager(Client client) {
        if (client == null) {
          this.client = Microbot.getClient();
        }else{
          this.client = client;
        }        
        
        initialize();
        
        

    }

   
    public void initialize() {
        
        
        
        loadTeleportData();
        initializeTeleportToMagicActionMap();
        loadDistanceCache();
                
        this.isTeleporting = false;
        
        
        this.pathfindingExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
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
        this.teleportToMagicActionMap = new HashMap<>();
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
    private void loadTeleportData() {
        this.teleports = new HashMap<>();
        SpellbookTeleportLoader SpellbookLoader = new SpellbookTeleportLoader();
        SpellbookLoader.loadSpellbookTeleports(teleports, "standardSpellbookTeleports.json");
        SpellbookLoader.loadSpellbookTeleports(teleports, "ancientMagicksTeleports.json");
        SpellbookLoader.loadSpellbookTeleports(teleports, "lunarSpellbookTeleports.json");
        SpellbookLoader.loadSpellbookTeleports(teleports, "arceuusSpellbookTeleports.json");
        /*
        //loadItemTeleports("enchantedJewellery.json", TeleportType.ENCHANTED_JEWELLERY);
        //loadItemTeleports("teleportTablets.json", TeleportType.TELEPORT_TABLETS);
        loadItemTeleports("teleportScrolls.json", TeleportType.TELEPORT_SCROLLS);
        loadItemTeleports("achievementDiaryItems.json", TeleportType.ACHIEVEMENT_DIARY_ITEMS);
        loadItemTeleports("otherItems.json", TeleportType.OTHER_ITEMS);
        loadItemTeleports("questRelatedItems.json", TeleportType.QUEST_RELATED_ITEMS);
        loadItemTeleports("skillCapes.json", TeleportType.SKILL_CAPES);
        loadItemTeleports("combatAchievementItems.json", TeleportType.COMBAT_ACHIEVEMENT_ITEMS); */
        
        // Example usage in your main code or plugin

        FairyRingTeleportLoader fairyRingLoader = new FairyRingTeleportLoader();
        fairyRingLoader.loadFairyRingTeleports(teleports, "fairyRingTeleports.json");

        TeleportItemLoader teleportItemLoader = new TeleportItemLoader();
        teleportItemLoader.loadTeleportItemLoaderTeleports(teleports, "teleportTablets.json");
        teleportItemLoader.loadTeleportItemLoaderTeleports(teleports, "enchantedJewelryTeleports.json");
        //loadPOHTeleports();
        //loadAncientMagicksTeleports();
        //loadArceuusTeleports();
        //loadLunarMagicTeleports();
        // ... (load other teleport types if needed)
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
        this.distanceCache = new HashMap<>();        
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

    public Teleport findNearestTeleport(WorldPoint destination, boolean useShortestPath) {
        return findNearestTeleport(destination, useShortestPath, null);
    }   
    /*public Teleport findNearestTeleport(WorldPoint destination) {
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
    }*/
    public Teleport findNearestTeleportShortestPath(WorldPoint destination) {
        if (currentTarget != null && currentTarget.equals(destination) && currentPathfindingTask != null && !currentPathfindingTask.isDone()) {
            return null; // Pathfinding is already in progress for this destination
        }

        setTarget(destination);

        CompletableFuture<Teleport> pathfindingFuture = new CompletableFuture<>();

        currentPathfindingTask = CompletableFuture.runAsync(() -> {
            Microbot.getClientThread().runOnSeperateThread(() -> {
                try {
                    List<CompletableFuture<TeleportDistancePair>> futures = teleports.values().stream()
                        .flatMap(List::stream)
                        .map(teleport -> CompletableFuture.supplyAsync(() -> {
                            double distance = getDistanceToViaShortestPath(teleport.getDestinations().get(0), destination);
                            return new TeleportDistancePair(teleport, distance);
                        }, pathfindingExecutor))
                        .collect(Collectors.toList());

                    CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0])
                    );

                    CompletableFuture<TeleportDistancePair> minDistanceFuture = allFutures.thenApply(v ->
                        futures.stream()
                            .map(CompletableFuture::join)
                            .min(Comparator.comparingDouble(TeleportDistancePair::getDistance))
                            .orElse(null)
                    );

                    TeleportDistancePair result = minDistanceFuture.get(30, TimeUnit.SECONDS);
                    pathfindingFuture.complete(result != null ? result.getTeleport() : null);
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
    public Teleport findNearestTeleport(WorldPoint destination, boolean useShortestPath, TeleportType type) {
        if (currentTarget != null && currentTarget.equals(destination) && currentPathfindingTask != null && !currentPathfindingTask.isDone()) {
            return null; // Pathfinding is already in progress for this destination
        }
    
        setTarget(destination);
    
        CompletableFuture<Teleport> pathfindingFuture = new CompletableFuture<>();
    
        currentPathfindingTask = CompletableFuture.runAsync(() -> {
            Microbot.getClientThread().runOnSeperateThread(() -> {
                try {
                    List<Teleport> teleportList = (type != null) ? teleports.get(type) : 
                        teleports.values().stream().flatMap(List::stream).collect(Collectors.toList());
    
                    if (teleportList == null || teleportList.isEmpty()) {
                        pathfindingFuture.complete(null);
                        return null;
                    }
    
                    // List to hold CompletableFuture of TeleportDistancePair (for each teleport)
                    List<CompletableFuture<TeleportDistancePair>> futures = teleportList.stream()
                        .map(teleport -> CompletableFuture.supplyAsync(() -> {
                            // For each teleport, calculate the minimum distance across all destinations
                            List<WorldPoint> destinations = teleport.getDestinations();
                            
                            // Create futures for each destination
                            List<CompletableFuture<Double>> distanceFutures = destinations.stream()
                                .map(dest -> CompletableFuture.supplyAsync(() -> {
                                    if (useShortestPath) {
                                        return getDistanceToViaShortestPath(dest, destination);
                                    } else {
                                        return teleport.getDistanceTo(dest);
                                    }
                                }, pathfindingExecutor))
                                .collect(Collectors.toList());
    
                            // Wait for all destination distance futures to complete and get the minimum
                            CompletableFuture<Void> allDestFutures = CompletableFuture.allOf(
                                distanceFutures.toArray(new CompletableFuture[0])
                            );
                            
                            Double minDistance = allDestFutures.thenApply(v ->
                                distanceFutures.stream()
                                    .map(CompletableFuture::join)
                                    .min(Comparator.comparingDouble(Double::doubleValue))
                                    .orElse(Double.MAX_VALUE)
                            ).join(); // Block and wait for the minimum distance for this teleport
    
                            return new TeleportDistancePair(teleport, minDistance);
                        }, pathfindingExecutor))
                        .collect(Collectors.toList());
    
                    CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0])
                    );
    
                    // After all teleport distance pairs are computed, get the minimum distance pair
                    CompletableFuture<TeleportDistancePair> minDistanceFuture = allFutures.thenApply(v ->
                        futures.stream()
                            .map(CompletableFuture::join)
                            .min(Comparator.comparingDouble(TeleportDistancePair::getDistance))
                            .orElse(null)
                    );
    
                    // Block and get the result
                    TeleportDistancePair result = minDistanceFuture.get(30, TimeUnit.SECONDS);
                    pathfindingFuture.complete(result != null ? result.getTeleport() : null);
                } catch (Exception e) {
                    pathfindingFuture.completeExceptionally(e);
                }
                return null;
            });
        });
        // Wait for the pathfinding to complete, no timeout
        return pathfindingFuture.join(); // This will block until the task is done
        /*try {
            return pathfindingFuture.get(30, TimeUnit.SECONDS); // Adjust timeout as needed
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            return null;
        }*/
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

    public double getDistanceToViaShortestPath(WorldPoint start, WorldPoint end) {
        // Check if there's a nearby cached path
        Optional<Map.Entry<String, CachedPathInfo>> nearbyCachedPath = findNearbyCachedPath(start, end);
        if (nearbyCachedPath.isPresent()) {
            return nearbyCachedPath.get().getValue().distance;
        }

        String cacheKey = start.getRegionID()+"-"+start.getX()+"_"+start.getY()+ "-" + end.getX()+"_"+end.getY()+"-"+end.getRegionID();
        CompletableFuture<ShortestPathResult> pathFuture = new CompletableFuture<>();
        
        currentPathfindingTask = CompletableFuture.runAsync(() -> {
            Microbot.getClientThread().runOnSeperateThread(() -> {
                try {
                    Pathfinder pathfinder = new Pathfinder(ShortestPathPlugin.getPathfinderConfig(), start, end);
                    sleepUntil(pathfinder::isDone);
                    while (!pathfinder.isDone()) {
                        Thread.sleep(100);
                    }
                    int distance = pathfinder.getPath().size();
                    List<WorldPoint> foundPath = pathfinder.getPath();
                    int indexOfStartPoint = 0;
                    Map<WorldPoint,List<Transport>> foundPathTransports = ShortestPathPlugin.getTransports();
                    List<Transport> foundTransportation= ShortestPathPlugin.getTransports().getOrDefault(foundPath.get(indexOfStartPoint), new ArrayList<>());
                    ShortestPathResult foundPathResult = new ShortestPathResult(foundPath, foundPathTransports, distance, start, end);
                    pathFuture.complete(foundPathResult);
                    return foundPathResult;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    pathFuture.completeExceptionally(e);
                }
                return null;
            });
        });

        try {
            ShortestPathResult foundPathResult = pathFuture.get(5, TimeUnit.SECONDS);
            int distance = foundPathResult.getDistance();
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
                String[] splitCachedKey = entry.getKey().split("-");
                assert splitCachedKey.length == 4;
                int startRegionId = Integer.parseInt(splitCachedKey[0]);                
                int cachedEndRegionId = Integer.parseInt(splitCachedKey[3]);
                String[] startPointCoordStrings = splitCachedKey[1].split("_");                
                String[] endPointCoordStrings = splitCachedKey[2].split("_");
                assert startPointCoordStrings.length == 2;
                assert endPointCoordStrings.length == 2;
                int cachedStartX = Integer.parseInt(startPointCoordStrings[0]);
                int cachedStartY = Integer.parseInt(startPointCoordStrings[1]);
                int cachedEndX = Integer.parseInt(endPointCoordStrings[0]);
                int cachedEndY = Integer.parseInt(endPointCoordStrings[1]);
                WorldPoint cachedStart = new WorldPoint(cachedStartX, cachedStartY,0);
                WorldPoint cachedEnd = new WorldPoint(cachedEndX, cachedEndY,0);
                boolean isSameRegion = startRegionId == start.getRegionID() && cachedEndRegionId == end.getRegionID();
                return cachedStart.distanceTo(start) <= CACHE_PROXIMITY_THRESHOLD &&
                       cachedEnd.distanceTo(end) <= CACHE_PROXIMITY_THRESHOLD;
            })
            .min(Comparator.comparingLong(entry -> entry.getValue().timestamp));
    }
  
    public Teleport findNearestTeleportSimple(WorldPoint destination) {
        return findNearestTeleport(destination, false, null);   
    }

    public Teleport findNearestTeleportSimple(WorldPoint destination, TeleportType type) {
        return findNearestTeleport(destination, false, type);         
    }

    public Teleport findNearestTeleport(WorldPoint destination, List<String> completedQuests) {
        Teleport nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (List<Teleport> teleportList : teleports.values()) {
            for (Teleport teleport : teleportList) {
                if (meetsQuestRequirements(teleport)) {
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

    private boolean meetsQuestRequirements(Teleport teleport ) {
        if (teleport.getRequiredQuests() == null || teleport.getRequiredQuests().isEmpty()) {
            return true;
        }
        
        for (Quest questReq : teleport.getRequiredQuests()) {
            if (!QuestState.FINISHED.equals(questReq.getState(Microbot.getClient()))) {
                return false;
            }
            
        }
        
        return true;
    }
    public boolean hasRequiredItems(Teleport teleport) {
        for (ItemQuantity requiredItem : teleport.getRequiredItems()) {
            if (!Rs2Inventory.hasItemAmount(requiredItem.getName(), requiredItem.getQuantity())) {
                return false;
            }
        }
        return true;
    }
    
    public boolean hasRequiredItemsInBank(Teleport teleport, boolean useBank) {
        
        List<VoxSylvaeInventoryAndBankManagementScript.BankItemInfo> needBankItems = new ArrayList<>();
        for (ItemQuantity requiredItem : teleport.getRequiredItems()) {
            VoxSylvaeInventoryAndBankManagementScript.BankItemInfo bankItem = null;
            bankItem = inventoryAndBankManagementScript.findItemInBank(requiredItem.getName(), 
                                                                        requiredItem.getQuantity());
            if (bankItem != null) {
                needBankItems.add(bankItem);
            }
        }        
        return needBankItems.size() == teleport.getRequiredItems().size();
    }
    // get items from bank
    public boolean getRequiredItemsFromBank(Teleport teleport) {
        List<VoxSylvaeInventoryAndBankManagementScript.BankItemInfo> needBankItems = new ArrayList<>();
        for (ItemQuantity requiredItem : teleport.getRequiredItems()) {
            VoxSylvaeInventoryAndBankManagementScript.BankItemInfo bankItem = null;
            bankItem = inventoryAndBankManagementScript.findItemInBank(requiredItem.getName(), 
                                                                        requiredItem.getQuantity());
            boolean itemRetrieved = inventoryAndBankManagementScript.retrieveAmountItemsFromBank(bankItem.itemId,
                                                                                                    requiredItem.getQuantity());
            if (itemRetrieved) {
                needBankItems.add(bankItem);
            }
        }
        boolean inVentory = true;
        if (!needBankItems.isEmpty()) {
            
            for (VoxSylvaeInventoryAndBankManagementScript.BankItemInfo bankItem : needBankItems) {
                if (!Rs2Inventory.hasItem(bankItem.itemId)) {
                    inVentory = false;
                    break;
                }
            }
            
        }
        return needBankItems.size() == teleport.getRequiredItems().size() && inVentory;
    }
    public boolean hasRequiredSkillLevel(Teleport teleport) {
        if (teleport.getType() != TeleportType.STANDARD_SPELLBOOK || teleport.getType() != TeleportType.ANCIENT_MAGICKS ||
            teleport.getType() != TeleportType.LUNAR_SPELLBOOK || teleport.getType() != TeleportType.ARCEUUS_SPELLBOOK) {
            return true;
        }
        
        return client.getRealSkillLevel(Skill.MAGIC) >= teleport.getRequiredLevel();
    }
     public boolean performTeleport(Teleport teleport) {
        if (!canPerformTeleport(teleport)) {
            Microbot.showMessage("Cannot perform teleport: " + teleport.getName());
            return false;
        }

        if (teleport instanceof SpellbookTeleport) {
            return castTeleportSpell((SpellbookTeleport) teleport);
        } else if (teleport instanceof TeleportItem) {
            return useItemTeleport((TeleportItem) teleport);
        } else if (teleport instanceof POHTeleport) {
            return usePOHTeleport((POHTeleport) teleport);
        }

        Microbot.showMessage("Unsupported teleport type: " + teleport.getClass().getSimpleName());
        return false;
    }

    private boolean canPerformTeleport(Teleport teleport) {
        int magicLevel = client.getRealSkillLevel(Skill.MAGIC);
        if (teleport.getRequiredLevel() > client.getRealSkillLevel(Skill.MAGIC)) {
            return false;
        }

        if (magicLevel < teleport.getRequiredLevel()) {
            return false;
        }
    
        if (!hasRequiredItems(teleport)) {
            return false;
        }
        
            //if (!QuestState.FINISHED.equals(quest.getState(Microbot.getClient()))
        for (Quest questReq : teleport.getRequiredQuests()) {
            QuestState.FINISHED.equals(questReq.getState(Microbot.getClient()));
            //if (!Rs2Player.isQuestCompleted(quest)) {
            //    return false;
            //}
        }
    
        return true;
    }

    private boolean castTeleportSpell( SpellbookTeleport teleport) {
        MagicAction magicTeleportAction = teleportToMagicActionMap.get(teleport.getName());
        if (magicTeleportAction == null) {
            Microbot.showMessage("Unknown teleport spell: " + teleport.getName());
            return false;
        }
        WorldPoint playerLocationBeforeTeleport = client.getLocalPlayer().getWorldLocation();
        List<WorldPoint> teleportDestinations = teleport.getDestinations();
        assert( teleportDestinations.size()==1);
        WorldPoint targetLocation = teleportDestinations.get(0);
        Rs2Magic.cast(magicTeleportAction);
        

        sleepUntil(() -> !Rs2Player.isNearArea(targetLocation,1)  && !Rs2Player.isInteracting() && !Rs2Player.isAnimating(), 500);
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

    private boolean useItemTeleport(TeleportItem teleport) {
        // Implement jewelry teleport logic
        // This might involve finding the jewelry in the inventory and using it
        return false; // Placeholder
    }

    private boolean usePOHTeleport(POHTeleport teleport) {
        // Implement POH teleport logic
        // This might involve interacting with POH objects
        return false; // Placeholder
    }
    private static void logDebug(String message) {
        if (Rs2AntibanSettings.devDebug) {
            Microbot.log("<col=4B0082>" + message + "</col>");
        }
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
                if (currentTeleport.getDistanceTo(playerLocation) < 1) { // Teleport successful
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