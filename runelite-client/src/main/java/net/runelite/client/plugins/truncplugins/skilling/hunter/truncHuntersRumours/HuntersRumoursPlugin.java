package net.runelite.client.plugins.truncplugins.skilling.hunter.truncHuntersRumours;

import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.hunter.HunterConfig;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.antiban.AntibanOverlay;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
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
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.microbot.breakhandler.BreakHandlerPlugin;
import net.runelite.client.plugins.microbot.fishing.barbarian.BarbarianFishingConfig;
import net.runelite.client.plugins.microbot.fishing.barbarian.BarbarianFishingScript;
import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.util.antiban.enums.CombatSkills;
import net.runelite.client.plugins.microbot.util.antiban.ui.MasterPanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import javax.swing.*;

import com.google.inject.Provides;


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
public class HuntersRumoursPlugin {

    public static int ticksSinceLogin;
    private static int idleTicks = 0;
    private final Map<Skill, Integer> skillExp = new EnumMap<>(Skill.class);
    private boolean ready;
    private Skill lastSkillChanged;
    private NavigationButton navButton;


    
    @Inject
    Notifier notifier;
    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private HuntersRumoursConfig config;
    @Inject
    private OverlayManager overlayManager;

    @Inject
    private HuntersRumoursOverlay hunterRumoursOverlay;
    @Inject
    HuntersRumoursScript HuntersRumoursScript;

    public static boolean isIdle() {
        return idleTicks > IDLE_TIMEOUT;
    }

    
   

    
    @Override
    protected void startUp() throws AWTException {
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
            overlayManager.add(hunterRumoursOverlay);
        }
        HuntersRumoursScript.run(config);
        
        //eventBus.register(this);
        //clientToolbar.addNavigation(navButton);
        //overlayManager.add(new AntibanOverlay());
    }
    @Override
    protected void shutDown() {
        overlayManager.removeIf(overlay -> overlay instanceof AntibanOverlay);
        //clientToolbar.removeNavigation(navButton);
    }

    @Provides
    HuntersRumoursConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HuntersRumoursConfig.class);
    }
    @Subscribe
    public void onChatMessage(ChatMessage event) {
        //if (Rs2Antiban.checkForCookingEvent(event)) {
        //   updateLastCookingAction();
        //}
    }
    @Subscribe
    public void onProfileChanged(ProfileChanged event) {
        //Rs2Antiban.resetAntibanSettings();
    }


    @Subscribe
    public void onGameTick(GameTick event) {
      

        // Handle antiban and break actions
        //if (breakHandler.shouldTakeBreak(config.breakDuration())) {
        //    breakHandler.takeBreak();
        //} else {
        //    antibanHandler.performAntiban();
        //}
    }
}
