package net.runelite.client.plugins.microbot;

<<<<<<< HEAD
import lombok.Getter;
=======
import com.google.common.base.Stopwatch;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.globval.enums.InterfaceTab;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.tabs.Rs2Tab;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
<<<<<<< HEAD
import java.util.function.BooleanSupplier;


=======
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

@Slf4j
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
public abstract class Script implements IScript {

    protected ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10);
    protected ScheduledFuture<?> scheduledFuture;
    public ScheduledFuture<?> mainScheduledFuture;
    public static boolean hasLeveledUp = false;
    public static boolean useStaminaPotsIfNeeded = true;
<<<<<<< HEAD

=======
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    public boolean isRunning() {
        return mainScheduledFuture != null && !mainScheduledFuture.isDone();
    }

    @Getter
    protected static WorldPoint initialPlayerLocation;

    public void sleep(int time) {
        try {
<<<<<<< HEAD
=======
            Microbot.log("Sleeping for " + time);
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
            Thread.sleep(time);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public void sleep(int start, int end) {
        int randTime = Random.random(start, end);
        try {
            Thread.sleep(randTime);
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
<<<<<<< HEAD
=======

>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    public boolean sleepUntil(BooleanSupplier awaitedCondition) {
        return sleepUntil(awaitedCondition, 5000);
    }

    public boolean sleepUntil(BooleanSupplier awaitedCondition, int time) {
        boolean done;
        long startTime = System.currentTimeMillis();
        do {
            done = awaitedCondition.getAsBoolean();
        } while (!done && System.currentTimeMillis() - startTime < time);
        return done;
    }

<<<<<<< HEAD
=======

    public boolean sleepUntil(BooleanSupplier awaitedCondition, BooleanSupplier resetCondition, int timeout) {
        final Stopwatch watch = Stopwatch.createStarted();
        while (!awaitedCondition.getAsBoolean() && watch.elapsed(TimeUnit.MILLISECONDS) < timeout) {
            sleep(100);
            if (resetCondition.getAsBoolean() && Microbot.isLoggedIn()) {
                watch.reset();
                watch.start();
            }
        }
        return awaitedCondition.getAsBoolean();
    }

>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    public void sleepUntilOnClientThread(BooleanSupplier awaitedCondition) {
        sleepUntilOnClientThread(awaitedCondition, 5000);
    }

    public void sleepUntilOnClientThread(BooleanSupplier awaitedCondition, int time) {
        boolean done;
        long startTime = System.currentTimeMillis();
        do {
            Microbot.status = "[ConditionalSleep] for " + time / 1000 + " seconds";
            done = Microbot.getClientThread().runOnClientThread(() -> awaitedCondition.getAsBoolean() || hasLeveledUp);
        } while (!done && System.currentTimeMillis() - startTime < time);
    }


    public void shutdown() {
        if (mainScheduledFuture != null && !mainScheduledFuture.isDone()) {
            mainScheduledFuture.cancel(true);
            ShortestPathPlugin.exit();
            if (Microbot.getClientThread().scheduledFuture != null)
                Microbot.getClientThread().scheduledFuture.cancel(true);
            initialPlayerLocation = null;
            Microbot.pauseAllScripts = false;
            Microbot.getSpecialAttackConfigs().reset();
        }
    }

    public boolean run() {
        hasLeveledUp = false;
<<<<<<< HEAD
        Microbot.getSpecialAttackConfigs().useSpecWeapon();
=======
        //Microbot.getSpecialAttackConfigs().useSpecWeapon();
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6

        if (Microbot.pauseAllScripts)
            return false;

        if (Microbot.isLoggedIn()) {
            if (Microbot.enableAutoRunOn)
                Rs2Player.toggleRunEnergy(true);

            if (Rs2Widget.getWidget(15269889) != null) { //levelup congratulations interface
                Rs2Keyboard.keyPress(KeyEvent.VK_SPACE);
            }
<<<<<<< HEAD
            Widget clickHereToPlayButton = Rs2Widget.getWidget(24772680); //on login screen
            if (clickHereToPlayButton != null && !Microbot.getClientThread().runOnClientThread(clickHereToPlayButton::isHidden)) {
                Rs2Widget.clickWidget(clickHereToPlayButton.getId());
            }

            boolean hasRunEnergy = Microbot.getClient().getEnergy() > 4000;

            if (!hasRunEnergy && useStaminaPotsIfNeeded && Rs2Player.isMoving()) {
=======
            Widget clickHereToPlayButton = Rs2Widget.getWidget(24772680); // on login screen

            if (clickHereToPlayButton != null && !Microbot.getClientThread().runOnClientThread(clickHereToPlayButton::isHidden)) {
                // Runs a synchronized block to prevent multiple plugins from clicking the play button
                synchronized (Rs2Widget.class) {
                    if (!Microbot.getClientThread().runOnClientThread(clickHereToPlayButton::isHidden)) {
                        Rs2Widget.clickWidget(clickHereToPlayButton.getId());

                        sleepUntil(() -> Microbot.getClientThread().runOnClientThread(clickHereToPlayButton::isHidden), 10000);
                    }
                }
            }

            boolean hasRunEnergy = Microbot.getClient().getEnergy() > Microbot.runEnergyThreshold;

            if (!hasRunEnergy && Microbot.useStaminaPotsIfNeeded && Rs2Player.isMoving()) {
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
                Rs2Inventory.useRestoreEnergyItem();
            }
        }

        return true;
    }

    public void keyPress(char c) {
        Rs2Keyboard.keyPress(c);
    }

<<<<<<< HEAD
    @Deprecated(since="Use Rs2Player.logout()", forRemoval = true)
=======
    @Deprecated(since = "Use Rs2Player.logout()", forRemoval = true)
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
    public void logout() {
        Rs2Tab.switchToLogout();
        sleepUntil(() -> Rs2Tab.getCurrentTab() == InterfaceTab.LOGOUT);
        sleep(600, 1000);
        Rs2Widget.clickWidget("Click here to logout");
    }

    public void onWidgetLoaded(WidgetLoaded event) {
        int groupId = event.getGroupId();

        if (groupId == InterfaceID.LEVEL_UP) {
            hasLeveledUp = true;
        }
    }
}
