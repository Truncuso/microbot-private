//package net.runelite.client.plugins.VoxSylvaePlugins.util;
import net.runelite.client.plugins.microbot.util.inventory.DropOrder;
public void dropInventoryItems(DropOrder configuredDropOrder, String subStringItem ) {
    DropOrder dropOrder = configuredDropOrder == DropOrder.RANDOM ? DropOrder.random() : configuredDropOrder;
    Rs2Inventory.dropAll(x -> x.name.toLowerCase().contains(subStringItem), dropOrder);
}

public boolean loadInventoryAndEquipment(String inventorySetupName, scheduledFuture<?> mainScheduledFuture) {
    rs2InventorySetup = new Rs2InventorySetup(inventorySetupName, mainScheduledFuture);
    
    if (!rs2InventorySetup.hasSpellBook()) {
        Microbot.showMessage("Your spellbook is not matching "+inventorySetupName+" the inventory setup.");
        sleep(10000);
        return;
    }
    
    hasEquipment = rs2InventorySetup.doesEquipmentMatch();
    hasInventory = rs2InventorySetup.doesInventoryMatch();
    if (!Rs2Bank.isOpen()) {
        Rs2Bank.walkToBankAndUseBank();
    }
    if (!hasEquipment) {
        hasEquipment = rs2InventorySetup.loadEquipment();
    }
    if (!hasInventory && rs2InventorySetup.doesEquipmentMatch()) {
        hasInventory = rs2InventorySetup.loadInventory();
        sleep(1000);
    }
    return hasInventory && hasEquipment;
}

private void checkBeforeWithdrawAndEquip(int itemId) {
    if (!Rs2Equipment.isWearing(itemId)) {
        Rs2Bank.withdrawAndEquip(itemId);
    }
}
public void equipGraceful() {
    checkBeforeWithdrawAndEquip("GRACEFUL HOOD");
    checkBeforeWithdrawAndEquip("GRACEFUL CAPE");
    checkBeforeWithdrawAndEquip("GRACEFUL BOOTS");
    checkBeforeWithdrawAndEquip("GRACEFUL GLOVES");
    checkBeforeWithdrawAndEquip("GRACEFUL TOP");
    checkBeforeWithdrawAndEquip("GRACEFUL LEGS");
}

public void withdrawDigsitePendant() {
    if (Rs2Bank.hasItem(ItemID.DIGSITE_PENDANT_1)) {
        checkBeforeWithdrawAndEquip(ItemID.DIGSITE_PENDANT_1);
    } else if (Rs2Bank.hasItem(ItemID.DIGSITE_PENDANT_2)) {
        checkBeforeWithdrawAndEquip(ItemID.DIGSITE_PENDANT_2);
    } else if (Rs2Bank.hasItem(ItemID.DIGSITE_PENDANT_3)) {
        checkBeforeWithdrawAndEquip(ItemID.DIGSITE_PENDANT_3);
    } else if (Rs2Bank.hasItem(ItemID.DIGSITE_PENDANT_4)) {
        checkBeforeWithdrawAndEquip(ItemID.DIGSITE_PENDANT_4);
    } else {
        checkBeforeWithdrawAndEquip(ItemID.DIGSITE_PENDANT_5);
    }
}

public static boolean retrieveItemsFromBank(List<String> items, WorldPoint desiredBankLocation) {
    if (desiredBankLocation != null) {
        if (!walkTo(desiredBankLocation)) {
            System.out.println("Failed to reach the bank");
            return false;
        }    
    }
    if (!Rs2Bank.isOpen()) {
        Rs2Bank.walkToBankAndUseBank();
    }
    

    if (!Rs2Bank.open()) {
        System.out.println("Failed to open the bank");
        return false;
    }

    for (String item : items) {
        if (Rs2Bank.hasItem(item)) {
            Rs2Bank.withdrawAllItem(item);
        } else {
            System.out.println("Missing item in bank: " + item);
            Rs2Bank.close();
            return false;
        }
    }

    Rs2Bank.close();
    return true;
}
