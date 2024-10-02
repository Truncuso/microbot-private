package net.runelite.client.plugins.VoxSylvaePlugins.util;
import net.runelite.api.InventoryID;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.ItemID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.Rs2InventorySetup;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.equipment.Rs2Equipment;
import net.runelite.client.plugins.microbot.util.inventory.DropOrder;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Item;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.shop.Rs2Shop;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import java.util.List;



import lombok.extern.slf4j.Slf4j;
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;

import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.shortestpath.ShortestPathPlugin;
import net.runelite.client.plugins.microbot.util.dialogues.Rs2Dialogue;

import net.runelite.client.plugins.microbot.util.math.Rs2Random;
import net.runelite.client.plugins.microbot.util.menu.NewMenuEntry;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
import static net.runelite.client.plugins.microbot.util.Global.sleepUntil;
import static net.runelite.client.plugins.microbot.util.Global.sleep;
import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.questhelper.runeliteobjects.extendedruneliteobjects.FaceAnimationIDs;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.apache.commons.lang3.ObjectUtils.Null;
import java.util.*;

@Slf4j
public class VoxSylvaeInventoryAndBankManagementScript {

    @Inject
    private Client client;
    @Inject
    private ItemManager itemManager;
    
    public ScheduledFuture<?> mainScheduledFuture;
    public boolean isRunning() {
        return mainScheduledFuture != null && !mainScheduledFuture.isDone();
    }
    public void shutdown() {
        if (mainScheduledFuture != null && !mainScheduledFuture.isDone()) {
            mainScheduledFuture.cancel(true);
            ShortestPathPlugin.exit();
            if (Microbot.getClientThread().scheduledFuture != null)
                Microbot.getClientThread().scheduledFuture.cancel(true);
            
            Microbot.pauseAllScripts = false;
            Microbot.getSpecialAttackConfigs().reset();
        }
    }


    public static class BankItemInfo {
        public final int slot;
        public final int tab;
        public final int itemId;
        public final String itemName;
        public final int quantity;

        public BankItemInfo(int slot, int tab, int itemId, String itemName, int quantity) {
            this.slot = slot;
            this.tab = tab;
            this.itemId = itemId;
            this.itemName = itemName;
            this.quantity = quantity;
        }
    }
    enum InvnetoryAndBankingState{
        IDLE,
        BANKING,
        DEPOSITING,
        WITHDRAWING,
        EQUIPPING,
        DROPPING,
        COMBINING,
        CLOSING_BANK
    }
    enum BankingAction{
        DEPOSIT_INVENTORY,
        DEPOSIT_WORN_ITEMS,
        WITHDRAW,
        EQUIP,
        DROP,
        COMBINE,
        CLOSE_BANK
    }
    InvnetoryAndBankingState currentState = InvnetoryAndBankingState.IDLE;
    public void dropInventoryItems(DropOrder configuredDropOrder, String subStringItem ) {
        DropOrder dropOrder = configuredDropOrder == DropOrder.RANDOM ? DropOrder.random() : configuredDropOrder;
        Rs2Inventory.dropAll(x -> x.name.toLowerCase().contains(subStringItem), dropOrder);
    }

    
    private boolean retrieveAllItemsFromNearestBank(int itemId) {
          //not open bank
          if (!openNearestBank()) {
            Microbot.log("<retrieveAllItemsFromBank> Bank did not open");
            return false;
        }
        if (Rs2Bank.hasItem(itemId)) {
            Rs2Bank.withdrawAll(itemId);
            sleepUntil(() -> Rs2Inventory.hasItem(itemId), (int)Rs2Random.truncatedGauss(600, 1000, 0));
            return Rs2Inventory.hasItem(itemId);
        }
        return false;
    }
    public boolean retrieveAmountItemsFromNearestBank(int itemId, int amount) {
        //not open bank
        if (!openNearestBank()) {
            Microbot.log("<retrieveAmountItemsFromBank> Bank did not open");
            return false;
        }
        if (Rs2Bank.hasItem(itemId)) {
            Rs2Bank.withdrawX(itemId, amount);
            sleepUntil(() -> Rs2Inventory.hasItemAmount(itemId,amount), (int)Rs2Random.truncatedGauss(600, 1000, 0));
            return Rs2Inventory.hasItemAmount(itemId,amount);
        }
        return false;
    }
    private Widget openAndScrollToBankTab(int itemTabID, int ItemSlotId) {
        
        if (!Rs2Bank.isTabOpen(itemTabID)) {
            log.info("Switching to tab: {}", itemTabID);
            Rs2Bank.openTab(itemTabID);
            sleepUntil(() -> Rs2Bank.isTabOpen(itemTabID), 5000);
        }

        Rs2Bank.scrollBankToSlot(ItemSlotId);
        Rs2Random.wait(200, 500);
        
        return Rs2Bank.getItemWidget(ItemSlotId);
    }
    public BankItemInfo findItemInBank(int itemId) {
        if(!openNearestBank()){
            Microbot.log("<findItemInBank> Bank did not open");
            return null;
        }
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        // throw exeption if bank is not open

        if (bank == null) {
            return null; // Bank is not open
        }

        Item[] bankItems = bank.getItems();
        int currentTab = 0;
        int currentSlot = 0;

        for (Item item : bankItems) {
            if (item.getId() == itemId) {
                ItemComposition itemComposition = itemManager.getItemComposition(itemId);
                String itemName = itemComposition.getName();
                return new BankItemInfo(currentSlot, currentTab, itemId, itemName, item.getQuantity());
            }

            currentSlot++;
            if (item.getId() == 21) { // ID 21 is the item ID for bank tab separators
                currentTab++;
                currentSlot = 0;
            }
        }

        return null; // Item not found
    }

    public BankItemInfo findItemInBank(String itemName, int minItemQuantity) {
        if(!openNearestBank()){
            Microbot.log("<findItemInBank> Bank did not open");
            return null;
        }
        
        ItemContainer bank = client.getItemContainer(InventoryID.BANK);
        if (bank == null) {
            return null; // Bank is not open
        }

        Item[] bankItems = bank.getItems();
        int currentTab = 0;
        int currentSlot = 0;

        for (Item item : bankItems) {
            ItemComposition itemComposition = itemManager.getItemComposition(item.getId());
            if (itemComposition.getName().equalsIgnoreCase(itemName) && item.getQuantity() >= minItemQuantity) {
                return new BankItemInfo(currentSlot, currentTab, item.getId(), itemName, item.getQuantity());
            }

            currentSlot++;
            if (item.getId() == 21) { // ID 21 is the item ID for bank tab separators
                currentTab++;
                currentSlot = 0;
            }
        }

        return null; // Item not found
    }

    private int getItemIdByName(String itemName) {
       

        for (int i = 0; i < client.getItemCount(); i++) {
            ItemComposition itemComposition = itemManager.getItemComposition(i);
            if (itemComposition.getName().equalsIgnoreCase(itemName)) {
                return i;
            }
        }
        return -1; // Item not found
    }
    public boolean retrieveItemsFromNearestBankById(List<Integer> itemIDs, int amount) {
        if (!openNearestBank()) {
            Microbot.log("<retrieveItemsFromBank> Bank did not open");
            return false;
        }

                    
        for (int itemID : itemIDs) {
            if (Rs2Bank.hasItem(itemID)) {
                Rs2Bank.withdrawX(itemID, amount);
                sleepUntil(() -> Rs2Inventory.hasItem(itemID), (int)Rs2Random.fancyNormalSample(600, 1000));
                if (!Rs2Inventory.hasItem(itemID)) {
                    Microbot.log("Failed to withdraw item: " + itemID);
                    Rs2Bank.closeBank();
                    return false;
                }
            } else {
                System.out.println("Missing item in bank: " + itemID);
                Rs2Bank.closeBank();
                return false;
            }
        }

        Rs2Bank.closeBank();
        return true;
    }
    private boolean retrieveItemsFromNearestBankByName(List<String> items) {
        if (!openNearestBank()) {
            Microbot.log("<retrieveItemsFromBank> Bank did not open to retrieve items: " + items);
            return false;
        }

        try {           
            for (String item : items) {
                if (Rs2Bank.hasItem(item)) {
                    Rs2Bank.withdrawItem(item);
                    sleepUntil(() -> Rs2Inventory.hasItem(item), (int)Rs2Random.fancyNormalSample(600, 1000));
                    if (!Rs2Inventory.hasItem(item)) {
                        Microbot.log("Failed to withdraw item: " + item);
                        Rs2Bank.closeBank();
                        return false;
                    }
                } else {
                    System.out.println("Missing item in bank: " + item);
                    Rs2Bank.closeBank();
                    return false;
                }
            }
        } catch (Exception ignored) {           
            Microbot.pauseAllScripts = true;
            Microbot.log("Failed to get "+items+" from, with exception: " + ignored.getMessage());
        }
        Rs2Bank.closeBank();
        return true;
    }
    public boolean loadInventoryAndEquipment(String inventorySetupName) {
        boolean hasEquipment = false;
        boolean hasInventory = false;
        if (!openNearestBank()) {
            Microbot.log("Bank did not open");
            
            return false;
        }
        try {
            Rs2InventorySetup rs2InventorySetup = new Rs2InventorySetup(inventorySetupName, mainScheduledFuture);
            
            if (!rs2InventorySetup.hasSpellBook()) {
                Microbot.showMessage("Your spellbook is not matching "+ inventorySetupName + " the inventory setup.");
                sleep(100);
                return false;
            }
            
            hasEquipment = rs2InventorySetup.doesEquipmentMatch();

            hasInventory = rs2InventorySetup.doesInventoryMatch();
          
            if (!hasEquipment) {
                hasEquipment = rs2InventorySetup.loadEquipment();
            }
            if (!hasInventory && rs2InventorySetup.doesEquipmentMatch()) {
                hasInventory = rs2InventorySetup.loadInventory();
                Rs2Random.wait(600, 800);
            }
        } catch (Exception ignored) {            
            Microbot.pauseAllScripts = true;
            Microbot.log("Failed to "+inventorySetupName+"load inventory setup, with exception: " + ignored.getMessage());
        }
        Rs2Bank.closeBank();
        return hasInventory && hasEquipment;
    }
    
    public boolean openNearestBank () {
        try {
            if (Rs2Bank.isOpen()) {
                return true;
            }
            int tryOpenBank = 0;
            while (!Rs2Bank.isOpen()) {
              
                sleepUntil(() ->   Rs2Bank.walkToBankAndUseBank(),(int)Rs2Random.truncatedGauss(5000, 10000, 0));
                tryOpenBank++;
                if (tryOpenBank > 5) {
                    Microbot.log("<openBank> Failed to reach the bank and open it");
                    return false;
                }
            }            
            
            sleepUntil(Rs2Bank::isOpen, 10000);
        } catch (Exception ignored) {            
            Microbot.pauseAllScripts = true;
            Microbot.log("<openBank> Failed to open bank, with exception: " + ignored.getMessage());
        }
        return Rs2Bank.isOpen();
    }
    
    private boolean checkBeforeWithdrawAndEquip(int itemId) {
        try {
            if (!Rs2Equipment.isWearing(itemId)) {
                Rs2Bank.withdrawAndEquip(itemId);
            //check if it is wearing
                sleepUntil(() -> Rs2Equipment.isWearing(itemId), 5000);
                return Rs2Equipment.isWearing(itemId);
            }
            return true;
        } catch (Exception ignored) {            
            Microbot.pauseAllScripts = true;
            Microbot.log("<checkBeforeWithdrawAndEquip> Failed to withdraw item: "+itemId+"from bank with exception: " + ignored.getMessage());
            return false;
        }
    }
    private boolean checkBeforeWithdrawAndEquip(String itemName) {
        if (!Rs2Equipment.isWearing(itemName)) {
            Rs2Bank.withdrawAndEquip(itemName);
            //check if it is wearing
            sleepUntil(() -> Rs2Equipment.isWearing(itemName), 5000);
            return Rs2Equipment.isWearing(itemName);
        }
        return true;
    }
  
   
    public boolean withdrawAndEquipItemWithMultipleIds(List<Integer> itemIds) {
        if (!openNearestBank()) {
            Microbot.log("<withdrawAndEquipItemWithMultipleIds> Bank did not open, cant get any item with ids: " + itemIds);
            return false;
            
        }
        for (int itemId : itemIds) {
            if (Rs2Bank.hasItem(itemId)) {                
                return checkBeforeWithdrawAndEquip(itemId);
            } else {
              continue;
            }
        }
        Microbot.log("<withdrawAndEquipItemWithMultipleIds> Missing all items in bank: " + itemIds);        
        return false;
    }
    public boolean withdrawAndEquip(String itemName) {
        if (!openNearestBank()) {
            Microbot.log("<withdrawAndEquip> Bank did not open, item: " + itemName);
            return false;
            
        }
        if (Rs2Bank.hasItem(itemName)) {
            checkBeforeWithdrawAndEquip(itemName);
            return true;
        }
        Microbot.log("<withdrawAndEquip> Missing item in bank: " + itemName);
        return false;

    }

    public boolean withdrawAndEquip(int itemId) {
        try{
            if (!openNearestBank()) {
                Microbot.log("<withdrawAndEquip> Bank did not open, item: " + itemId);
                return false;
            }
            if (Rs2Bank.hasItem(itemId)) {
                checkBeforeWithdrawAndEquip(itemId);
                return true;
            }
            Microbot.log("<withdrawAndEquip> Missing item in bank: " + itemId);
            return false;
        } catch (Exception ignored) {
            Microbot.pauseAllScripts = true;
            Microbot.log("<withdrawAndEquip> Failed to withdraw and equip item: " + itemId+" with exception: " + ignored.getMessage());
            return false;
        }
    
    }
    public <T>boolean hasAllItemsInInventory(List<T> items) {
        for (T item : items) {

            if (item.getClass() == String.class && !Rs2Inventory.hasItem((String)item)) {
                //Rs2Item invItem = Rs2Inventory.getItem((String)item);
                Microbot.log("<hasAllItemsInInventory> Missing item in inventory: " + item);
                return false;
            }
            if (item.getClass() == Integer.class && !Rs2Inventory.hasItem((Integer)item)) {
                Microbot.log("<hasAllItemsInInventory> Missing item in inventory: " + item);
                return false;                
            }
        }
        return true;
    }
    //utitlity methods

    public void withdrawAndEquipGraceful() {
        
        withdrawAndEquip("GRACEFUL HOOD");
        withdrawAndEquip("GRACEFUL CAPE");
        withdrawAndEquip("GRACEFUL BOOTS");
        withdrawAndEquip("GRACEFUL GLOVES");
        withdrawAndEquip("GRACEFUL TOP");
        withdrawAndEquip("GRACEFUL LEGS");
    }
    public boolean withdrawDigsitePendant() {
        List<Integer> digsitePendants = List.of(ItemID.DIGSITE_PENDANT_1, 
                                        ItemID.DIGSITE_PENDANT_2, 
                                        ItemID.DIGSITE_PENDANT_3, 
                                        ItemID.DIGSITE_PENDANT_4, 
                                        ItemID.DIGSITE_PENDANT_5);
        return withdrawAndEquipItemWithMultipleIds(digsitePendants);     
    }
    public boolean withdrawItemsWithMultipleCharges(String itemName, int chargesPriority) {
        if (!openNearestBank()) {
            Microbot.log("Nearest Bank did not open");
            
            return false;
        }
        //make sure that no number is added to the item name
        String baseItemName = itemName.replaceAll("(\\s*\\(\\d+\\))|(\\_\\(\\d+\\))$", "");
        List<Rs2Item> items = new ArrayList<>();
        for (int i = 1; i < 8; i++) {
            Rs2Item item = Rs2Bank.findBankItem(baseItemName + " (" + i + ")");
            if (item != null) {
                items.add(item);
            }
            
        }
        if (items.size() == 0) {
            Microbot.log("<withdrawItemsWithMultipleCharges> Missing all items in bank: " + itemName);
            return false;
        }
        Rs2Item itemWithLowestCharges = items.get(0);
        
        
        return retrieveAmountItemsFromNearestBank(itemWithLowestCharges.id, 1);
    }
    //@Override
    //public void shutdown() {
    //    super.shutdown();
    //    Microbot.pauseAllScripts = true;
    //}
}