package net.runelite.client.plugins.microbot.qualityoflife;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
<<<<<<< HEAD
import net.runelite.api.MenuEntry;
=======
import net.runelite.api.*;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
<<<<<<< HEAD
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.NeverLogoutScript;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.pouch.PouchOverlay;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.pouch.PouchScript;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
=======
import net.runelite.client.plugins.config.ConfigPlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.qualityoflife.enums.WintertodtActions;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.AutoRunScript;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.CameraScript;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.NeverLogoutScript;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.SpecialAttackScript;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.pouch.PouchOverlay;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.pouch.PouchScript;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.wintertodt.WintertodtOverlay;
import net.runelite.client.plugins.microbot.qualityoflife.scripts.wintertodt.WintertodtScript;
import net.runelite.client.plugins.microbot.util.antiban.FieldUtil;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.reflect.FieldUtils;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

<<<<<<< HEAD
=======
import static net.runelite.client.plugins.microbot.qualityoflife.scripts.wintertodt.WintertodtScript.isInWintertodtRegion;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
@PluginDescriptor(
        name = PluginDescriptor.See1Duck + "QoL",
        description = "Quality of Life Plugin",
        tags = {"QoL", "microbot"},
        enabledByDefault = false
)
@Slf4j
public class QoLPlugin extends Plugin {
<<<<<<< HEAD
    public static String version = "1.0.0";
    public static String LOADOUT_TO_LOAD = "";
    public static List<NewMenuEntry> bankMenuEntries = new LinkedList<>();
    public static List<NewMenuEntry> furnaceMenuEntries = new LinkedList<>();
    public static List<NewMenuEntry> anvilMenuEntries = new LinkedList<>();
=======
    public static final List<NewMenuEntry> bankMenuEntries = new LinkedList<>();
    public static final List<NewMenuEntry> furnaceMenuEntries = new LinkedList<>();
    public static final List<NewMenuEntry> anvilMenuEntries = new LinkedList<>();
    private static final int HALF_ROTATION = 1024;
    private static final int FULL_ROTATION = 2048;
    private static final int PITCH_INDEX = 0;
    private static final int YAW_INDEX = 1;
    private static final BufferedImage SWITCHER_ON_IMG = getImageFromConfigResource("switcher_on");
    private static final BufferedImage STAR_ON_IMG = getImageFromConfigResource("star_on");
    public static String loadoutToLoad = "";
    private static GameState lastGameState = GameState.UNKNOWN;
    private final int[] deltaCamera = new int[3];
    private final int[] previousCamera = new int[3];

>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    public static NewMenuEntry workbenchMenuEntry;
    public static boolean recordActions = false;
    public static boolean executeBankActions = false;
    public static boolean executeFurnaceActions = false;
    public static boolean executeAnvilActions = false;
    public static boolean executeWorkbenchActions = false;
    public static boolean executeLoadoutActions = false;
<<<<<<< HEAD
    @Inject
    QoLScript qoLScript;
=======
    private final String BANK_OPTION = "Bank";
    private final String SMELT_OPTION = "Smelt";
    private final String SMITH_OPTION = "Smith";
    @Inject
    public ConfigManager configManager;
    @Inject
    WintertodtScript wintertodtScript;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    @Inject
    PouchScript pouchScript;
    @Inject
    private QoLConfig config;
    @Inject
<<<<<<< HEAD
=======
    private QoLScript qoLScript;
    @Inject
    private AutoRunScript autoRunScript;
    @Inject
    private SpecialAttackScript specialAttackScript;
    @Inject
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    private OverlayManager overlayManager;
    @Inject
    private QoLOverlay qoLOverlay;
    @Inject
    private PouchOverlay pouchOverlay;
<<<<<<< HEAD
=======
    @Inject
    private WintertodtOverlay wintertodtOverlay;

>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    @Provides
    QoLConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(QoLConfig.class);
    }

<<<<<<< HEAD
    private final int HALF_ROTATION = 1024;
    private final int FULL_ROTATION = 2048;

    private final int PITCH_INDEX = 0;
    private final int YAW_INDEX = 1;
    private final int SCROLL_INDEX = 2;
    //Made an array just in case a setter for camera pitch is ever added
    private final int[] deltaCamera = new int[3];
    private final int[] previousCamera = new int[3];

    private int lerp(int x, int y, float alpha) {
        return x + (int) ((y - x) * alpha);
    }

    private int getSmallestAngle(int x, int y) {
        return mod(((y - x) + HALF_ROTATION), FULL_ROTATION) - HALF_ROTATION;
    }

    //https://stackoverflow.com/questions/1878907/how-can-i-find-the-difference-between-two-angles
    //This function was tested to produce results that were much more reasonable than the modulo operator
    public int mod(int a, int n) {
        return (int) (a - Math.floor(a / (float) n) * n);
    }

    private void applySmoothingToAngle(int index) {
        int deltaChange;
        int changed;
        int newDeltaAngle;
        newDeltaAngle = getSmallestAngle(previousCamera[index], index == YAW_INDEX ? Microbot.getClient().getMapAngle() : 0/*No pitch method in RL*/);
        deltaCamera[index] += newDeltaAngle;

        deltaChange = lerp(deltaCamera[index], 0, (80 / 100.0f));
        changed = previousCamera[index] + deltaChange;

        deltaCamera[index] -= deltaChange;
        if (index == YAW_INDEX) {
            Microbot.getClient().setCameraYawTarget(changed);
        }/* else if(index == PITCH_INDEX) {
			//No pitch method in RL yet
		}*/
        previousCamera[index] += deltaChange;
=======
    private static BufferedImage getImageFromConfigResource(String imgName) {
        try {
            Class<?> clazz = Class.forName("net.runelite.client.plugins.config.ConfigPanel");
            return ImageUtil.loadImageResource(clazz, imgName.concat(".png"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static ImageIcon remapImage(BufferedImage image, Color color) {
        if (color != null) {
            BufferedImage img = new BufferedImage(image.getWidth(), image.getHeight(), 2);
            Graphics2D graphics = img.createGraphics();
            graphics.drawImage(image, 0, 0, null);
            graphics.setColor(color);
            graphics.setComposite(AlphaComposite.getInstance(10, 1));
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.dispose();
            return new ImageIcon(img);
        } else {
            return null;
        }
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    @Override
    protected void startUp() throws AWTException {
        if (overlayManager != null) {
<<<<<<< HEAD
            overlayManager.add(qoLOverlay);
=======
            overlayManager.add(pouchOverlay);
            overlayManager.add(qoLOverlay);
            overlayManager.add(wintertodtOverlay);
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
        }
        if (config.displayPouchCounter()) {
            overlayManager.add(pouchOverlay);
            pouchScript.startUp();
        }
<<<<<<< HEAD
        qoLScript.run(config);
    }

    protected void shutDown() {
        pouchScript.shutdown();
        qoLScript.shutdown();
        overlayManager.remove(qoLOverlay);
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
=======
        if (config.useSpecWeapon()) {
            Microbot.getSpecialAttackConfigs().setSpecialAttack(true);
            Microbot.getSpecialAttackConfigs().setSpecialAttackWeapon(config.specWeapon());
            Microbot.getSpecialAttackConfigs().setMinimumSpecEnergy(config.specWeapon().getEnergyRequired());
        }
        if (config.autoRun()) {
            Microbot.enableAutoRunOn = true;
        }
        if (config.autoStamina()) {
            Microbot.useStaminaPotsIfNeeded = true;
            Microbot.runEnergyThreshold = config.staminaThreshold() * 100;
        }
        autoRunScript.run(config);
        specialAttackScript.run(config);
        qoLScript.run(config);
        wintertodtScript.run(config);
        updateUiElements();
    }

    @Override
    protected void shutDown() {
        qoLScript.shutdown();
        autoRunScript.shutdown();
        specialAttackScript.shutdown();
        overlayManager.remove(pouchOverlay);
        overlayManager.remove(qoLOverlay);
        overlayManager.remove(wintertodtOverlay);
    }

    @Subscribe
    public void onChatMessage(ChatMessage chatMessage) {
        ChatMessageType chatMessageType = chatMessage.getType();


        if (isInWintertodtRegion()
                && (chatMessageType == ChatMessageType.GAMEMESSAGE || chatMessageType == ChatMessageType.SPAM)) {
            wintertodtScript.onChatMessage(chatMessage);
        }


    }

    @Subscribe
    public void onGameTick(GameTick event) {
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
        if (!Microbot.isLoggedIn()) return;
        if (config.neverLogout()) {
            NeverLogoutScript.onGameTick(event);
        }
<<<<<<< HEAD
=======
        if (config.useSpecWeapon()) {
            if (Microbot.getSpecialAttackConfigs().getSpecialAttackWeapon() != config.specWeapon()) {
                Microbot.getSpecialAttackConfigs().setSpecialAttack(true);
                Microbot.getSpecialAttackConfigs().setSpecialAttackWeapon(config.specWeapon());
                Microbot.getSpecialAttackConfigs().setMinimumSpecEnergy(config.specWeapon().getEnergyRequired());
            }
        }
    }

    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() != GameState.UNKNOWN && lastGameState == GameState.UNKNOWN) {
            updateUiElements();
        }
        if (event.getGameState() == GameState.LOGGED_IN) {
            if (config.fixCameraPitch())
                CameraScript.fixPitch();
            if (config.fixCameraZoom())
                CameraScript.fixZoom();
        }
        lastGameState = event.getGameState();
    }

    @Subscribe
    public void onHitsplatApplied(HitsplatApplied event) {
        if ((config.resumeFletchingKindling() || config.resumeFeedingBrazier())) {
            if (event.getActor() == Microbot.getClient().getLocalPlayer()) {
                if (config.wintertodtActions() != WintertodtActions.NONE) {
                    updateWintertodtInterupted(true);
                }

            }
        }
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    @Subscribe
    private void onMenuOptionClicked(MenuOptionClicked event) {
        pouchScript.onMenuOptionClicked(event);
<<<<<<< HEAD
        if (Rs2Bank.isOpen() && config.useDoLastBank() && recordActions) {
            MenuEntry menuEntry = event.getMenuEntry();
            if (menuEntry.getOption().contains("Withdraw")) {
            }
            NewMenuEntry cachedMenuEntry = new NewMenuEntry(menuEntry.getOption(), menuEntry.getTarget(), menuEntry.getIdentifier(), menuEntry.getType(), menuEntry.getParam0(), menuEntry.getParam1(), menuEntry.isForceLeftClick());
            cachedMenuEntry.setItemId(menuEntry.getItemId());
            cachedMenuEntry.setWidget(menuEntry.getWidget());
            bankMenuEntries.add(cachedMenuEntry);
            if (menuEntry.getOption().equals("Close")) {
                recordActions = false;
                Microbot.log("<col=5F1515>Stopped recording actions</col>");
            }
        }
        if ((Rs2Widget.isProductionWidgetOpen() || Rs2Widget.isGoldCraftingWidgetOpen()) && config.useDoLastFurnace() && recordActions) {
            MenuEntry menuEntry = event.getMenuEntry();
            NewMenuEntry cachedMenuEntry = new NewMenuEntry(menuEntry.getOption(), menuEntry.getTarget(), menuEntry.getIdentifier(), menuEntry.getType(), menuEntry.getParam0(), menuEntry.getParam1(), menuEntry.isForceLeftClick());
            cachedMenuEntry.setItemId(menuEntry.getItemId());
            cachedMenuEntry.setWidget(menuEntry.getWidget());
            furnaceMenuEntries.add(cachedMenuEntry);
            if (menuEntry.getOption().equals("Make sets:") || menuEntry.getOption().equals("Smelt") || menuEntry.getOption().contains("Make")) {
                recordActions = false;
                Microbot.log("<col=5F1515>Stopped recording actions</col>");
            }
        }
        if (Rs2Widget.isSmithingWidgetOpen() && config.useDoLastAnvil() && recordActions) {
            MenuEntry menuEntry = event.getMenuEntry();
            NewMenuEntry cachedMenuEntry = new NewMenuEntry(menuEntry.getOption(), menuEntry.getTarget(), menuEntry.getIdentifier(), menuEntry.getType(), menuEntry.getParam0(), menuEntry.getParam1(), menuEntry.isForceLeftClick());
            cachedMenuEntry.setItemId(menuEntry.getItemId());
            cachedMenuEntry.setWidget(menuEntry.getWidget());
            anvilMenuEntries.add(cachedMenuEntry);
            if (menuEntry.getOption().equals("Smith set") || menuEntry.getOption().equals("Smith")) {
                recordActions = false;
                Microbot.log("<col=5F1515>Stopped recording actions</col>");
            }
        }
        if (event.getMenuOption().equals("Track")) {
            event.consume();
        }

        if (event.getMenuOption().equals("Work-at")) {
            MenuEntry menuEntry = event.getMenuEntry();
            workbenchMenuEntry = new NewMenuEntry(menuEntry.getOption(), menuEntry.getTarget(), menuEntry.getIdentifier(), menuEntry.getType(), menuEntry.getParam0(), menuEntry.getParam1(), menuEntry.isForceLeftClick());
        }

=======
        MenuEntry menuEntry = event.getMenuEntry();
        if (recordActions) {
            if (Rs2Bank.isOpen() && config.useDoLastBank()) {
                handleMenuOptionClicked(menuEntry, bankMenuEntries, "Close");
            } else if ((Rs2Widget.isProductionWidgetOpen() || Rs2Widget.isGoldCraftingWidgetOpen() || Rs2Widget.isSilverCraftingWidgetOpen()) && config.useDoLastFurnace()) {
                handleMenuOptionClicked(menuEntry, furnaceMenuEntries, "Make", "Smelt", "Craft");
            } else if (Rs2Widget.isSmithingWidgetOpen() && config.useDoLastAnvil()) {
                handleMenuOptionClicked(menuEntry, anvilMenuEntries, "Smith");
            }
        }

        if ("Track".equals(event.getMenuOption())) {
            event.consume();
        }

        if ((config.resumeFletchingKindling() || config.resumeFeedingBrazier()) && isInWintertodtRegion()) {
            if (event.getMenuOption().contains("Fletch") && event.getMenuTarget().isEmpty() && config.resumeFletchingKindling()) {
                WintertodtActions action = WintertodtActions.FLETCH;
                action.setMenuEntry(createCachedMenuEntry(menuEntry));
                updateLastWinthertodtAction(action);
                updateWintertodtInterupted(false);
                Microbot.log("Setting action to Fletch Kindle");
            }
            if (event.getMenuOption().contains("Feed") && config.resumeFeedingBrazier()) {
                WintertodtActions action = WintertodtActions.FEED;
                action.setMenuEntry(createCachedMenuEntry(menuEntry));
                updateLastWinthertodtAction(action);
                updateWintertodtInterupted(false);
            }
            if (event.getMenuOption().contains("Chop") || event.getMenuOption().contains("Walk")) {
                updateLastWinthertodtAction(WintertodtActions.NONE);
                updateWintertodtInterupted(false);
            }
        }
    }

    private void handleMenuOptionClicked(MenuEntry menuEntry, List<NewMenuEntry> menuEntriesList, String... stopOptions) {
        NewMenuEntry cachedMenuEntry = createCachedMenuEntry(menuEntry);
        menuEntriesList.add(cachedMenuEntry);
        if (Arrays.stream(stopOptions).anyMatch(menuEntry.getOption()::contains)) {
            recordActions = false;
            Microbot.log("<col=5F1515>Stopped recording actions</col>");
        }
    }

    private NewMenuEntry createCachedMenuEntry(MenuEntry menuEntry) {
        NewMenuEntry cachedMenuEntry = new NewMenuEntry(
                menuEntry.getOption(),
                menuEntry.getTarget(),
                menuEntry.getIdentifier(),
                menuEntry.getType(),
                menuEntry.getParam0(),
                menuEntry.getParam1(),
                menuEntry.isForceLeftClick()
        );
        cachedMenuEntry.setItemId(menuEntry.getItemId());
        cachedMenuEntry.setWidget(menuEntry.getWidget());
        return cachedMenuEntry;
    }

    @Subscribe
    private void onNpcChanged(NpcChanged event) {
        if (isInWintertodtRegion()) {
            wintertodtScript.onNpcChanged(event);
        }
    }

    @Subscribe
    private void onNpcDespawned(NpcDespawned event) {
        if (isInWintertodtRegion()) {
            wintertodtScript.onNpcDespawned(event);
        }
    }

    @Subscribe
    private void onNpcSpawned(NpcSpawned event) {
        if (isInWintertodtRegion()) {
            wintertodtScript.onNpcSpawned(event);
        }
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    @Subscribe
    private void onMenuEntryAdded(MenuEntryAdded event) {
<<<<<<< HEAD
        if (config.rightClickCameraTracking()) {
            if (event.getMenuEntry().getNpc() != null && event.getMenuEntry().getNpc().getId() > 0) {
                addMenuEntry(event, "Track", event.getTarget(), this::customTrackOnClicked);
            }
        }

        if (config.useDoLastBank()) {
            if (event.getOption().equals("Talk-to")) {
                List<MenuEntry> entries = new LinkedList<>(Arrays.asList(Microbot.getClient().getMenuEntries()));
                if (entries.stream().anyMatch(e -> e.getOption().equals("Bank") && e.getTarget().equals(event.getTarget()))) {
                    event.getMenuEntry().setDeprioritized(true);
                }
            }
            if (event.getOption().equals("Bank")) {
                event.getMenuEntry().onClick(this::recordNewActions);

                addMenuEntry(event, "<col=FFA500>Do-Last</col>", event.getTarget(), this::customBankingOnClicked);
            }
        }
        if (config.useDoLastFurnace()) {
            if (event.getOption().equals("Smelt")) {
                event.getMenuEntry().onClick(this::recordNewActions);
                addMenuEntry(event, "<col=FFA500>Do-Last</col>", event.getTarget(), this::customFurnaceOnClicked);
            }
        }
        if (config.useDoLastAnvil()) {
            if (event.getOption().equals("Smith") && event.getTarget().contains("Anvil")) {
                event.getMenuEntry().onClick(this::recordNewActions);
                addMenuEntry(event, "<col=FFA500>Do-Last</col>", event.getTarget(), this::customAnvilOnClicked);
            }
        }

        if (config.useDoLastWorkbench()) {
            if (event.getOption().equals("Work-at")) {
                event.getMenuEntry().onClick(this::customWorkbenchOnClicked);
                //addMenuEntry(event, "<col=FFA500>Do-Last</col>", event.getTarget(), this::customWorkbenchOnClicked);
            }
        }
        if (config.displayInventorySetups()) {
            if (event.getOption().equals("Bank")) {
                if (config.displaySetup1()) {
                    addLoadoutMenuEntry(event, "<col=FFA500>Equip: " + config.Setup1() + "</col>", event.getTarget(), (menuEntry) -> customLoadoutOnClicked(menuEntry, config.Setup1()));
                }
                if (config.displaySetup2()) {
                    addLoadoutMenuEntry(event, "<col=FFA500>Equip: " + config.Setup2() + "</col>", event.getTarget(), (menuEntry) -> customLoadoutOnClicked(menuEntry, config.Setup2()));
                }
                if (config.displaySetup3()) {
                    addLoadoutMenuEntry(event, "<col=FFA500>Equip: " + config.Setup3() + "</col>", event.getTarget(), (menuEntry) -> customLoadoutOnClicked(menuEntry, config.Setup3()));
                }
                if (config.displaySetup4()) {
                    addLoadoutMenuEntry(event, "<col=FFA500>Equip: " + config.Setup4() + "</col>", event.getTarget(), (menuEntry) -> customLoadoutOnClicked(menuEntry, config.Setup4()));
                }
            }
        }

    }

    @Subscribe
    public void onConfigChanged(ConfigChanged ev) {
        if (ev.getKey().equals("smoothRotation")) {
            if (config.smoothCameraTracking()) {
                previousCamera[YAW_INDEX] = Microbot.getClient().getMapAngle();
            }
        }
        if (ev.getKey().equals("displayPouchCounter")) {
            if (config.displayPouchCounter()) {
                overlayManager.add(pouchOverlay);
                pouchScript.startUp();
            } else {
                pouchScript.shutdown();
                overlayManager.remove(pouchOverlay);
            }
        }
=======
        String option = event.getOption();
        String target = event.getTarget();
        MenuEntry menuEntry = event.getMenuEntry();
        boolean bankChestCheck = "Bank".equals(option) || ("Use".equals(option) && target.contains("Bank chest"));

        if (config.quickFletchKindling() && isInWintertodtRegion() && event.getItemId() == ItemID.KNIFE && "Use".equals(option)) {
            menuEntry.setOption("<col=FFA500>Fletch Kindle</col>");
            menuEntry.setTarget("");
            menuEntry.onClick(this::fletchBrumaRootsOnClicked);

        }

        if (config.rightClickCameraTracking() && menuEntry.getNpc() != null && menuEntry.getNpc().getId() > 0) {
            addMenuEntry(event, "Track", target, this::customTrackOnClicked);
        }


        if (config.useDoLastBank()) {
            if ("Talk-to".equals(option)) {
                for (MenuEntry e : Microbot.getClient().getMenuEntries()) {
                    if ("Bank".equals(e.getOption()) && e.getTarget().equals(target)) {
                        menuEntry.setDeprioritized(true);
                        break;
                    }
                }
            }
            if (bankChestCheck && event.getItemId() == -1) {
                menuEntry.onClick(this::recordNewActions);
                addMenuEntry(event, "<col=FFA500>Do-Last</col>", target, this::customBankingOnClicked);
            }
        }

        if (config.useDoLastFurnace() && "Smelt".equals(option) && target.contains("Furnace")) {
            menuEntry.onClick(this::recordNewActions);
            addMenuEntry(event, "<col=FFA500>Do-Last</col>", target, this::customFurnaceOnClicked);
        }

        if (config.useDoLastAnvil() && "Smith".equals(option) && target.contains("Anvil")) {
            menuEntry.onClick(this::recordNewActions);
            addMenuEntry(event, "<col=FFA500>Do-Last</col>", target, this::customAnvilOnClicked);
        }

        if (config.useDoLastWorkbench() && "Work-at".equals(option)) {
            menuEntry.onClick(this::customWorkbenchOnClicked);
        }

        if (config.displayInventorySetups() && bankChestCheck && event.getItemId() == -1) {
            addLoadoutMenuEntries(event, target);
        }
    }

    private void addLoadoutMenuEntries(MenuEntryAdded event, String target) {
        if (config.displaySetup1()) {
            addLoadoutMenuEntry(event, "<col=FFA500>Equip: " + config.Setup1() + "</col>", target, e -> customLoadoutOnClicked(e, config.Setup1()));
        }
        if (config.displaySetup2()) {
            addLoadoutMenuEntry(event, "<col=FFA500>Equip: " + config.Setup2() + "</col>", target, e -> customLoadoutOnClicked(e, config.Setup2()));
        }
        if (config.displaySetup3()) {
            addLoadoutMenuEntry(event, "<col=FFA500>Equip: " + config.Setup3() + "</col>", target, e -> customLoadoutOnClicked(e, config.Setup3()));
        }
        if (config.displaySetup4()) {
            addLoadoutMenuEntry(event, "<col=FFA500>Equip: " + config.Setup4() + "</col>", target, e -> customLoadoutOnClicked(e, config.Setup4()));
        }
    }


    @Subscribe
    public void onConfigChanged(ConfigChanged ev) {
        if ("smoothRotation".equals(ev.getKey()) && config.smoothCameraTracking()) {
            previousCamera[YAW_INDEX] = Microbot.getClient().getMapAngle();
        }
        if (ev.getKey().equals("accentColor") || ev.getKey().equals("toggleButtonColor") || ev.getKey().equals("pluginLabelColor")) {
            updateUiElements();
        }
        if (ev.getKey().equals("resumeFletchingKindling")) {
            if (config.resumeFletchingKindling()) {
                configManager.setConfiguration("QoL", "quickFletchKindling", true);
            }
        }
        if (ev.getKey().equals("displayPouchCounter")) {
            if (ev.getNewValue() == "true") {
                overlayManager.add(pouchOverlay);
            } else {
                overlayManager.remove(pouchOverlay);
            }
        }
        if (ev.getKey().equals("useSpecWeapon") || ev.getKey().equals("specWeapon")) {
            if (config.useSpecWeapon()) {
                Microbot.getSpecialAttackConfigs().setSpecialAttack(true);
                Microbot.getSpecialAttackConfigs().setSpecialAttackWeapon(config.specWeapon());
                Microbot.getSpecialAttackConfigs().setMinimumSpecEnergy(config.specWeapon().getEnergyRequired());
            } else {
                Microbot.getSpecialAttackConfigs().reset();
            }
        }

        if (ev.getKey().equals("autoRun")) {
            Microbot.enableAutoRunOn = config.autoRun();
        }
        if (ev.getKey().equals("autoStamina")) {
            Microbot.useStaminaPotsIfNeeded = config.autoStamina();
        }
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    @Subscribe
    public void onBeforeRender(BeforeRender render) {
<<<<<<< HEAD
        if (!Microbot.isLoggedIn()) {
            return;
        }

        if (config.smoothCameraTracking()) {
            //Pitch stuff to be added if runelite ever decides to add a Client.setCameraPitchTarget method
            //Until then, yaw going to have to stick with yaw!
            //applySmoothingToAngle(PITCH_INDEX);
            applySmoothingToAngle(YAW_INDEX);
        }


    }


=======
        if (!Microbot.isLoggedIn() || !config.smoothCameraTracking()) {
            return;
        }
        applySmoothingToAngle(YAW_INDEX);
    }

>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    @Subscribe
    public void onItemContainerChanged(final ItemContainerChanged event)
    {
        pouchScript.onItemContainerChanged(event);
    }

    private void customLoadoutOnClicked(MenuEntry event, String loadoutName) {
        recordActions = false;
<<<<<<< HEAD
        LOADOUT_TO_LOAD = loadoutName;
        executeLoadoutActions = true;

=======
        loadoutToLoad = loadoutName;
        executeLoadoutActions = true;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    private void customBankingOnClicked(MenuEntry event) {
        recordActions = false;
        if (bankMenuEntries.isEmpty()) {
            Microbot.log("<col=5F1515>No actions recorded</col>");
            return;
        }
        Microbot.log("<col=245C2D>Banking</col>");
<<<<<<< HEAD
        executeBankActions();

    }

    private void customFurnaceOnClicked(MenuEntry event) {
        recordActions = false;
        if (furnaceMenuEntries.isEmpty()) {
            Microbot.log("<col=5F1515>No actions recorded</col>");
            return;
        }
        Microbot.log("<col=245C2D>Furnace</col>");
        executeFurnaceActions();

    }

    private void customAnvilOnClicked(MenuEntry event) {
        recordActions = false;
        if (anvilMenuEntries.isEmpty()) {
            Microbot.log("<col=5F1515>No actions recorded</col>");
            return;
        }
        Microbot.log("<col=245C2D>Anvil</col>");
        executeAnvilActions();

=======
        executeBankActions = true;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    private void customTrackOnClicked(MenuEntry event) {
        if (Rs2Camera.isTrackingNpc()) {
            Rs2Camera.stopTrackingNpc();
            Microbot.log("<col=5F1515>Stopped tracking old NPC, try again to track new NPC</col>");
            return;
        }
        Rs2Camera.trackNpc(Objects.requireNonNull(event.getNpc()).getId());
    }

<<<<<<< HEAD
    private void customWorkbenchOnClicked(MenuEntry event) {
        Microbot.log("<col=245C2D>Workbench</col>");
        // get all pouches in inventory except for the "Rune pouch"
        executeWorkbenchActions();
=======
    private void customFurnaceOnClicked(MenuEntry event) {
        recordActions = false;
        if (furnaceMenuEntries.isEmpty()) {
            Microbot.log("<col=5F1515>No actions recorded</col>");
            return;
        }
        Microbot.log("<col=245C2D>Furnace</col>");
        executeFurnaceActions = true;
    }

    private void customAnvilOnClicked(MenuEntry event) {
        recordActions = false;
        if (anvilMenuEntries.isEmpty()) {
            Microbot.log("<col=5F1515>No actions recorded</col>");
            return;
        }
        Microbot.log("<col=245C2D>Anvil</col>");
        executeAnvilActions = true;
    }

    private void customWorkbenchOnClicked(MenuEntry event) {
        Microbot.log("<col=245C2D>Workbench</col>");
        executeWorkbenchActions = true;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    }

    private void recordNewActions(MenuEntry event) {
        recordActions = true;
<<<<<<< HEAD
        if (event.getOption().equals("Bank")) {
            bankMenuEntries.clear();
        }
        if (event.getOption().equals("Smelt")) {
            furnaceMenuEntries.clear();
        }
        if (event.getOption().equals("Smith")) {
            anvilMenuEntries.clear();
        }
        Microbot.log("<col=245C2D>Recording actions</col>");

    }

    private void executeBankActions() {
        executeBankActions = true;
    }

    private void executeFurnaceActions() {
        executeFurnaceActions = true;
    }

    private void executeAnvilActions() {
        executeAnvilActions = true;
    }

    private void executeWorkbenchActions() {
        executeWorkbenchActions = true;
    }

    private void addMenuEntry(MenuEntryAdded event, String option, String target, Consumer<MenuEntry> callback) {
        List<MenuEntry> entries = new LinkedList<>(Arrays.asList(Microbot.getClient().getMenuEntries()));

        if (entries.stream().anyMatch(e -> e.getOption().equals(option) && e.getTarget().equals(target))) {
            return;
        }

        Microbot.getClient().createMenuEntry(entries.size())
=======
        String option = event.getOption();
        if (BANK_OPTION.equals(option)) {
            bankMenuEntries.clear();
        } else if (SMELT_OPTION.equals(option)) {
            furnaceMenuEntries.clear();
        } else if (SMITH_OPTION.equals(option)) {
            anvilMenuEntries.clear();
        }
        Microbot.log("<col=245C2D>Recording actions for: </col>" + option);
    }

    private void fletchBrumaRootsOnClicked(MenuEntry event) {
        int brumaRootSlot = Rs2Inventory.slot(ItemID.BRUMA_ROOT);
        if (brumaRootSlot == -1) {
            Microbot.log("<col=5F1515>Bruma root not found in inventory</col>");
            return;
        }
        Microbot.log("<col=245C2D>Fletching Kindling</col>");
        NewMenuEntry combinedMenuEntry = new NewMenuEntry("Fletch", "Bruma root", 0, MenuAction.WIDGET_TARGET_ON_WIDGET, brumaRootSlot, event.getParam1(), false);
        combinedMenuEntry.setItemId(ItemID.BRUMA_ROOT);
        Microbot.doInvoke(combinedMenuEntry, new Rectangle(1, 1));
    }

    private void applySmoothingToAngle(int index) {
        int currentAngle = index == YAW_INDEX ? Microbot.getClient().getMapAngle() : 0;
        int newDeltaAngle = getSmallestAngle(previousCamera[index], currentAngle);
        deltaCamera[index] += newDeltaAngle;

        int deltaChange = lerp(deltaCamera[index], 0, 0.8f);
        int changed = previousCamera[index] + deltaChange;

        deltaCamera[index] -= deltaChange;
        if (index == YAW_INDEX) {
            Microbot.getClient().setCameraYawTarget(changed);
        }
        previousCamera[index] += deltaChange;
    }

    private int lerp(int x, int y, double alpha) {
        return x + (int) Math.round((y - x) * alpha);
    }

    private int getSmallestAngle(int x, int y) {
        return ((y - x + HALF_ROTATION) % FULL_ROTATION) - HALF_ROTATION;
    }

    private void addMenuEntry(MenuEntryAdded event, String option, String target, Consumer<MenuEntry> callback) {
        int index = Microbot.getClient().getMenuEntries().length;
        Microbot.getClient().createMenuEntry(index)
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
                .setOption(option)
                .setTarget(target)
                .setParam0(event.getActionParam0())
                .setParam1(event.getActionParam1())
                .setIdentifier(event.getIdentifier())
                .setType(event.getMenuEntry().getType())
                .onClick(callback);
<<<<<<< HEAD

    }

    private void addLoadoutMenuEntry(MenuEntryAdded event, String option, String target, Consumer<MenuEntry> callback) {
        Microbot.getClient().createMenuEntry(1)
=======
    }

    private void addLoadoutMenuEntry(MenuEntryAdded event, String option, String target, Consumer<MenuEntry> callback) {
        int index = Microbot.getClient().getMenuEntries().length - 1;
        Microbot.getClient().createMenuEntry(index)
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
                .setOption(option)
                .setTarget(target)
                .setParam0(event.getActionParam0())
                .setParam1(event.getActionParam1())
                .setIdentifier(event.getIdentifier())
                .setType(event.getMenuEntry().getType())
                .onClick(callback);
<<<<<<< HEAD

    }
=======
    }

    public void updateLastWinthertodtAction(WintertodtActions action) {
        configManager.setConfiguration("QoL", "wintertodtActions", action);
    }

    public void updateWintertodtInterupted(boolean interupted) {
        configManager.setConfiguration("QoL", "interrupted", interupted);
    }


    private void updateUiElements() {
        try {
            // Get the Field object for BRAND_ORANGE(Accent color)
            Field accentColorField = ColorScheme.class.getDeclaredField("BRAND_ORANGE");
            Class<?> pluginButton = Class.forName("net.runelite.client.plugins.config.PluginToggleButton");
            Field onSwitcherPluginPanel = pluginButton.getDeclaredField("ON_SWITCHER");
            onSwitcherPluginPanel.setAccessible(true);


            FieldUtil.setFinalStatic(accentColorField, config.accentColor());

            FieldUtil.setFinalStatic(onSwitcherPluginPanel, remapImage(SWITCHER_ON_IMG, config.toggleButtonColor()));
            ConfigPlugin configPlugin = (ConfigPlugin) Microbot.getPluginManager().getPlugins().stream().filter((plugin) ->
                    plugin instanceof ConfigPlugin).findAny().orElse(null);
            if (configPlugin == null) {
                Microbot.log("Config Plugin not found");
            } else {
                try {
                    Class<?> pluginListPanelClass = Class.forName("net.runelite.client.plugins.config.PluginListPanel");
                    JPanel pluginListPanel = (JPanel) configPlugin.getInjector().getProvider(pluginListPanelClass).get();
                    final List<?>[] pluginList = {(List<?>) FieldUtils.readDeclaredField(pluginListPanel, "pluginList", true)};

                    if (pluginList[0] == null) {
                        log.info("Plugin list is null, waiting for it to be initialized");
                        sleepUntil(() -> {
                            try {
                                pluginList[0] = (List<?>) FieldUtils.readDeclaredField(pluginListPanel, "pluginList", true);
                                return pluginList[0] != null;
                            } catch (Exception e) {
                                log.error("QoL Error updating plugin list: " + e.getMessage());
                                return false;
                            }
                        },10000);

                    }


                    for (Object plugin : pluginList[0]) {
                        if (plugin instanceof JPanel) {
                            Component[] var12 = ((JPanel) plugin).getComponents();
                            for (Component c : var12) {
                                if (c instanceof JLabel) {
                                    c.setForeground(config.pluginLabelColor());
                                }
                            }
                        }


                        JToggleButton onOffToggle = (JToggleButton) FieldUtils.readDeclaredField(plugin, "onOffToggle", true);
                        onOffToggle.setSelectedIcon(remapImage(SWITCHER_ON_IMG, config.toggleButtonColor()));
                    }

                } catch (Exception e) {
                    log.error("QoL Error updating plugin list panel: " + e.getMessage());
                    Microbot.log("QoL Error updating inner UI elements: " + e.getMessage());
                }

            }


        } catch (Exception e) {
            log.error("QoL Error updating UI elements: " + e.getMessage());
            Microbot.log("QoL Error updating UI elements: " + e.getMessage());
        }
    }

>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
}
