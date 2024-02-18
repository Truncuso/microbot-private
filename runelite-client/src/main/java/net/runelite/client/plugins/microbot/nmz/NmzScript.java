package net.runelite.client.plugins.microbot.nmz;

import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.gameobject.Rs2GameObject;
import net.runelite.client.plugins.microbot.util.inventory.Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.VirtualKeyboard;
import net.runelite.client.plugins.microbot.util.math.Random;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.prayer.Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import java.util.concurrent.TimeUnit;

import static net.runelite.api.Varbits.NMZ_ABSORPTION;

public class NmzScript extends Script {

    public static double version = 1.9;

    public static NmzConfig config;

    public static boolean useOverload = false;

    public boolean canStartNmz() {
        return Inventory.hasItemAmount("overload (4)", config.overloadPotionAmount()) && Inventory.hasItemAmount("absorption (4)", config.absorptionPotionAmount());
    }


    public boolean run(NmzConfig config) {
        NmzScript.config = config;
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            if (!Microbot.isLoggedIn()) return;
            try {
                boolean isOutsideNmz = Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(new WorldPoint(2602, 3116, 0)) < 20;
                useOverload = Microbot.getClient().getBoostedSkillLevel(Skill.RANGED) == Microbot.getClient().getRealSkillLevel(Skill.RANGED);
                if (isOutsideNmz) {
                    handleOutsideNmz();
                } else {
                    handleInsideNmz();
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    public void handleOutsideNmz() {
        boolean hasStartedDream = Microbot.getVarbitValue(3946) > 0;
        if (!hasStartedDream) {
            startNmzDream();
        } else {
            if (canStartNmz()) {
                consumeEmptyVial();
                return;
            }
            manageInventoryOutsideNmz();
            handleStore();
        }
    }

    public void handleInsideNmz() {
        useZapperIfConfigured();
        useOverloadPotion();
        manageLocatorOrb();
        toggleSpecialAttack();
        useAbsorptionPotion();
    }

    public void startNmzDream() {
        Rs2Npc.interact(NpcID.DOMINIC_ONION, "Dream");
        sleepUntil(() -> Rs2Widget.hasWidget("Which dream would you like to experience?"));
        Rs2Widget.clickWidget("Previous:");
        sleepUntil(() -> Rs2Widget.hasWidget("Click here to continue"));
        Rs2Widget.clickWidget("Click here to continue");
        sleepUntil(() -> Rs2Widget.hasWidget("Agree to pay"));
        VirtualKeyboard.typeString("1");
        VirtualKeyboard.enter();
    }

    public void useZapperIfConfigured() {
        if (config.useZapper()) {
            interactWithObject(ObjectID.ZAPPER_26256);
            interactWithObject(ObjectID.RECURRENT_DAMAGE);
        }
    }

    public void interactWithObject(int objectId) {
        TileObject rs2GameObject = Rs2GameObject.findObjectById(objectId);
        if (rs2GameObject != null) {
            Microbot.getWalker().walkFastLocal(rs2GameObject.getLocalLocation());
            sleepUntil(() -> Microbot.getClient().getLocalPlayer().getWorldLocation().distanceTo(rs2GameObject.getWorldLocation()) < 5);
            Rs2GameObject.interact(objectId);
        }
    }

    public void manageLocatorOrb() {
        if (Inventory.hasItem(ItemID.LOCATOR_ORB)) {
            handleLocatorOrbUsage();
            randomlyToggleRapidHeal();
        }
    }

    public void handleLocatorOrbUsage() {
        if (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) > Random.random(1, 5) && !useOverload
                && Microbot.getClient().getBoostedSkillLevel(Skill.RANGED) != Microbot.getClient().getRealSkillLevel(Skill.RANGED)) {
            Inventory.useItemFast(ItemID.LOCATOR_ORB, "feel");
        }
    }

    public void randomlyToggleRapidHeal() {
        if (Random.random(1, 50) == 2) {
            Rs2Prayer.fastPray(Prayer.RAPID_HEAL, true);
            sleep(300, 600);
            Rs2Prayer.fastPray(Prayer.RAPID_HEAL, false);
        }
    }

    public void toggleSpecialAttack() {
        if (Microbot.getClient().getLocalPlayer().isInteracting() && config.useSpecialAttack()) {
            Rs2Combat.setSpecState(true, 1000);
        }
    }

    public void useOverloadPotion() {
        if (useOverload && Inventory.hasItemContains("overload") && Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) > 50) {
            Inventory.interact(new String[]{"overload (4)", "overload (3)", "overload (2)", "overload (1)"});
            sleep(10000);
        }
    }

    public void useAbsorptionPotion() {
        if (Microbot.getVarbitValue(NMZ_ABSORPTION) < Random.random(300, 600) && Inventory.hasItemContains("absorption")) {
            for (int i = 0; i < Random.random(1, 5); i++) {
                Inventory.interact(new String[]{"absorption (4)", "absorption (3)", "absorption (2)", "absorption (1)"});
                sleep(600, 1000);
            }
        }
    }

    public void manageInventoryOutsideNmz() {
        managePotionInventory(ObjectID.OVERLOAD_POTION, "overload", config.overloadPotionAmount(), "32");
        managePotionInventory(ObjectID.ABSORPTION_POTION, "absorption", config.absorptionPotionAmount(), "80");
    }

    public void managePotionInventory(int objectId, String itemName, int requiredAmount, String keyboardInput) {
        if (!Inventory.hasItemAmountExact(itemName, requiredAmount)) {
            Rs2GameObject.interact(objectId, "Store");
            String storeWidgetText = "Store all your " + itemName + " potion?";
            sleepUntil(() -> Rs2Widget.hasWidget(storeWidgetText));
            if (Rs2Widget.hasWidget(storeWidgetText)) {
                VirtualKeyboard.typeString("1");
                VirtualKeyboard.enter();
                sleepUntil(() -> !Inventory.hasItem(objectId));
            }
            Rs2GameObject.interact(objectId, "Take");
            String widgetText = "How many doses of " + itemName;
            sleepUntil(() -> Rs2Widget.hasWidget(widgetText));
            if (Rs2Widget.hasWidget(widgetText)) {
                VirtualKeyboard.typeString(keyboardInput);
                VirtualKeyboard.enter();
                sleepUntil(() -> Inventory.hasItemAmountExact(itemName + " (4)", requiredAmount));
            }
        }
    }

    public void consumeEmptyVial() {
        final int EMPTY_VIAL = 26291;
        Rs2GameObject.interact(EMPTY_VIAL, "drink");
        sleepUntil(() -> Rs2Widget.hasWidget("Nightmare zone"));
        Rs2Widget.clickWidget(8454150);
        sleep(5000);
    }

    public void handleStore() {
        int varbitOverload = 3953;
        int varbitAbsorption = 3954;
        int overloadAmt = Microbot.getVarbitValue(varbitOverload);
        int absorptionAmt = Microbot.getVarbitValue(varbitAbsorption);
        int nmzPoints = Microbot.getVarbitPlayerValue(VarPlayer.NMZ_REWARD_POINTS);

        if (!Inventory.isFull()) {
            if ((absorptionAmt < (config.absorptionPotionAmount() * 4) || overloadAmt < config.overloadPotionAmount() * 4) && nmzPoints < 100000) {
                Microbot.showMessage("BOT SHUTDOWN: Not enough points to buy potions");
                shutdown();
                return;
            }
        }

        Rs2GameObject.interact(26273);

        sleepUntil(() -> Rs2Widget.getWidget(13500418) != null, 10000);

        Widget benefitsBtn = Rs2Widget.getWidget(13500418);
        if (benefitsBtn == null) return;
        boolean notSelected = benefitsBtn.getSpriteId() != 813;
        if (notSelected) {
            Rs2Widget.clickWidgetFast(benefitsBtn, 4, 4);
        }
        int count = 0;
        while (count < Random.random(3, 5)) {
            Widget nmzRewardShop = Rs2Widget.getWidget(206, 6);
            if (nmzRewardShop == null) break;
            Widget overload = nmzRewardShop.getChild(6);
            Rs2Widget.clickWidgetFast(overload, 6, 4);
            Widget absorption = nmzRewardShop.getChild(9);
            Rs2Widget.clickWidgetFast(absorption, 9, 4);
            sleep(600, 1200);
            count++;
        }
    }

}
