package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;

import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.hunter.HunterConfig;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.AntibanOverlay;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.api.AnimationID;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDependency;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerPlugin;
import net.runelite.client.plugins.microbot.util.antiban.AntibanPlugin;
//import net.runelite.client.plugins.microbot.fishing.barbarian.BarbarianFishingConfig;
//import net.runelite.client.plugins.microbot.fishing.barbarian.BarbarianFishingScript;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.util.antiban.enums.CombatSkills;
import net.runelite.client.plugins.microbot.util.antiban.ui.MasterPanel;
import net.runelite.client.plugins.VoxSylvaePlugins.util.*;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;

import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import java.awt.*;


@PluginDescriptor(
    name = PluginDescriptor.TRUNC+"Hunter Rumours Plugin",
    description = "Automatically handles hunter tasks",
    tags = {"hunter", "rumours", "automation"},
    alwaysOn = false,
    hidden = false
)
@PluginDependency(XpTrackerPlugin.class)
@PluginDependency(XpTrackerPlugin.class)
@Slf4j
public class HuntersRumoursPlugin extends Plugin{

    public static int ticksSinceLogin;
    private static int idleTicks = 0;
    private final Map<Skill, Integer> skillExp = new EnumMap<>(Skill.class);
    private boolean ready;
    private Skill lastSkillChanged;
    private NavigationButton navButton;
    private int IDLE_TIMEOUT = 400;
    
    
    @Inject
    Notifier notifier;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private AIOHunterConfig config;
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AIOHunterOverlay AIOHunterOverlay_instance;
    @Inject
    HuntersRumoursScript HuntersRumoursScript;

    private boolean isRunning = false;
    @Inject
    private KeyManager keyManager;
    
    private final HotkeyListener hotkeyListener = new HotkeyListener(() -> config.toggleKeybind()) {
        @Override
        public void hotkeyPressed() {
            togglePlugin();
        }
    };

 
    @Override
    protected void startUp() throws Exception{
       /*  final MasterPanel panel = injector.getInstance(MasterPanel.class);
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "antiban.png");
        navButton = NavigationButton.builder()
                .tooltip("Antiban")
                .icon(icon)
                .priority(1)
                .panel(panel)
                .build();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(panel::loadSettings);
            }
        }, 0, 600); */

        Microbot.pauseAllScripts = false;
        Microbot.setClient(client);
        Microbot.setClientThread(clientThread);
        Microbot.setNotifier(notifier);
        Microbot.setMouse(new VirtualMouse());
        if (overlayManager != null) {            
            overlayManager.add(AIOHunterOverlay_instance);
        }
        keyManager.registerKeyListener(hotkeyListener);
        
        
        
        //eventBus.register(this);
        //clientToolbar.addNavigation(navButton);
        //overlayManager.add(new AntibanOverlay());
    }
    
    @Override
    protected void shutDown() throws Exception{
        overlayManager.removeIf(overlay -> overlay instanceof AIOHunterOverlay);
        //clientToolbar.removeNavigation(navButton);
        keyManager.unregisterKeyListener(hotkeyListener);
        stopPlugin();
    }


    private void togglePlugin() {
        if (isRunning) {
            stopPlugin();
        } else {
            startPlugin();
        }
    }
   
    private void startPlugin() {
        if (!isRunning) {
            isRunning = true;
            ensurePluginEnabled(BreakHandlerPlugin.class , config.devDebug());
            HuntersRumoursScript.run(config);
            Microbot.showMessage("AIO hunter plugin started by user");               
        }
    }

    private void stopPlugin() {
        if (isRunning) {
            isRunning = false;
            HuntersRumoursScript.shutdown();
            Microbot.showMessage("AIO hunter plugin stopped by user");                                
        }
    }

    @Provides
    AIOHunterConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AIOHunterConfig.class);
    }
    @Subscribe
    public void onChatMessage(ChatMessage event) {
        //if (Rs2Antiban.checkForCookingEvent(event)) {
        //   updateLastCookingAction();
        //}
        if (isRunning) {
            HuntersRumoursScript.onChatMessage(event);
        }
    }
    @Subscribe
    public void onProfileChanged(ProfileChanged event) {
        Rs2Antiban.resetAntibanSettings();
        if (isRunning) {
            HuntersRumoursScript.onProfileChanged(event);
        }
    }


    @Subscribe
    public void onGameTick(GameTick event) {      
        if (isRunning) {
            HuntersRumoursScript.onGameTick();
        }        
    }
}
