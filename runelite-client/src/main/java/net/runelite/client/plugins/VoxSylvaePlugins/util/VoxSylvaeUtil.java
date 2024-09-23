
//package net.runelite.client.plugins.VoxSylvaePlugins.util;
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
import net.runelite.client.plugins.PluginInstantiationException;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
public void ensurePluginEnabled(Class pluginClass,  boolean devDebug){
    if (!Microbot.isPluginEnabled(pluginClass)) {
        String pluginName = pluginClass.getName();
        if (devDebug)
            Microbot.showMessage("AIO Hunter plugin depend on the plugin \""+pluginName +"\", enabling it now.");

        Microbot.log("\""+pluginName +"\""+"BreakHandlerPlugin not enabled, enabling it now.");
        
        Plugin PluginObject = Microbot.getPluginManager().getPlugins().stream()
                .filter(x -> x.getClass().getName().equals(pluginName))
                .findFirst()
                .orElse(null);
        Microbot.startPlugin(PluginObject);
    }
}
public static Plugin getPluginByNameByName(String pluginName) {
    for (Plugin plugin : Microbot.getPluginManager().getPlugins()) {
        PluginDescriptor descriptor = plugin.getClass().getAnnotation(PluginDescriptor.class);
        if (descriptor != null && descriptor.name().contains(pluginName)) {
            return plugin;
        }
    }
    return null;
}
public static String startPluginByName(String pluginName) {
        System.out.println("Starting startPlugin"); // Debug line
        try {
            Microbot.getPluginManager().setPluginEnabled(getPluginByName(pluginName), true);
            //sleep(100);
            Microbot.getPluginManager().startPlugins();
            //if (!(currentPluginName == null))
            //    lastPluginName = currentPluginName;
            //currentPluginName = pluginName;
            //activity = "running";
            //pluginStartTime = System.currentTimeMillis();
            System.out.println("started plugin: " + pluginName);
            return pluginName;
        } catch (Exception e) {
            System.out.println("Failed to start plugin: " + e.getMessage());
            return null;
        }
    }

public static void stopPluginByName(String pluginName) {
    try {
        Microbot.getPluginManager().setPluginEnabled(getPluginByName(pluginName), false);
        sleep(500);
        SwingUtilities.invokeLater(() -> {
            try {
                Microbot.getPluginManager().stopPlugin(getPluginByName(pluginName));
                System.out.println("stopped plugin: " + pluginName);
            } catch (PluginInstantiationException e) {
                System.out.println("error stopPlugin"); // Debug line
                throw new RuntimeException(e);
            }
        });
    } catch (Exception e) {
        System.out.println("Failed to stop plugin: " + e.getMessage());
    }
}

