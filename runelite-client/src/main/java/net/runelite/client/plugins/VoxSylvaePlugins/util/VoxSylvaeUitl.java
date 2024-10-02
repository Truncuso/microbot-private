
package net.runelite.client.plugins.VoxSylvaePlugins.util;

import static net.runelite.client.plugins.microbot.util.Global.sleep;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.util.antiban.AntibanOverlay;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.antiban.Rs2AntibanSettings;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.xptracker.XpTrackerPlugin;
import net.runelite.api.AnimationID;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.Widget;
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

import net.runelite.client.plugins.microbot.util.antiban.enums.Activity;
import net.runelite.client.plugins.microbot.util.antiban.enums.ActivityIntensity;
import net.runelite.client.plugins.microbot.util.antiban.enums.CombatSkills;
import net.runelite.client.plugins.microbot.util.antiban.ui.MasterPanel;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.PluginInstantiationException;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.HotkeyListener;
import net.runelite.client.util.ImageUtil;

import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.security.Login;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.awt.event.KeyEvent;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
public class VoxSylvaeUitl {
    public static void ensurePluginEnabled(Class pluginClass,  boolean devDebug){
        if (!Microbot.isPluginEnabled(pluginClass)) {
            String pluginName = pluginClass.getName();
            if (devDebug)
                Microbot.showMessage("Current plugin depend on the plugin \""+pluginName +"\", enabling it now.");

            Microbot.log("\""+pluginName +"\""+"not enabled, enabling it now.");
            
            Plugin PluginObject = Microbot.getPluginManager().getPlugins().stream()
                    .filter(x -> x.getClass().getName().equals(pluginName))
                    .findFirst()
                    .orElse(null);
            Microbot.startPlugin(PluginObject);
        }
    }
    public static Plugin getPluginByName(String pluginName) {
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
                sleep(100);
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
     /**
     * Hops to a new world
     */
    private void hopWorld() {
        // Stock level dropped below minimum, pause or stop execution
        System.out.println("Hopping world");
        Rs2Shop.closeShop();
        Rs2Bank.closeBank();
        sleep(2400, 4800); // this sleep is required to avoid the message: please finish what you're doing before using the world switcher.
        // This is where we need to hop worlds.
        int world = Login.getRandomWorld(true, null);
        boolean isHopped = Microbot.hopToWorld(world);
        if (!isHopped) return;
        Rs2Widget.sleepUntilHasWidget("Switch World");
        sleepUntil(() -> Rs2Widget.findWidget("Switch World", null, false) != null, 5000);
        Widget SwitchWorldWidget = Rs2Widget.findWidget("Switch World", null, false);
        if (SwitchWorldWidget == null) {
            System.out.println("Switch World Widget not found");
            return;
        }
        boolean result = Rs2Widget.clickWidget(SwitchWorldWidget);
        if (result) {
            Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            sleepUntil(() -> Microbot.getClient().getGameState() == GameState.HOPPING, 5000);
            sleepUntil(() -> Microbot.getClient().getGameState() == GameState.LOGGED_IN, 5000);
            if (Microbot.getClient().getGameState() == GameState.LOGGED_IN) {
                System.out.println("Successfully hopped to world " + world);
            } else {
                Microbot.pauseAllScripts = true;
                System.out.println("Failed to hop to world " + world);
            }
            
        }
    }
    public static int getSkillLevel(Skill skill) {
        return Microbot.getClient().getRealSkillLevel(skill);
    }
    
}

