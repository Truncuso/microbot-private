package net.runelite.client.plugins.microbot.playerassist.combat;

import net.runelite.api.Actor;
import net.runelite.api.NPC;
import net.runelite.api.Skill;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.playerassist.PlayerAssistConfig;
import net.runelite.client.plugins.microbot.playerassist.PlayerAssistPlugin;
import net.runelite.client.plugins.microbot.playerassist.enums.AttackStyle;
import net.runelite.client.plugins.microbot.playerassist.enums.AttackStyleMapper;
import net.runelite.client.plugins.microbot.util.camera.Rs2Camera;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.npc.Rs2NpcManager;
<<<<<<< HEAD
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
=======
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.plugins.microbot.util.prayer.Rs2Prayer;
import net.runelite.client.plugins.microbot.util.prayer.Rs2PrayerEnum;
import net.runelite.client.plugins.microbot.util.tile.Rs2Tile;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
<<<<<<< HEAD
import java.util.concurrent.atomic.AtomicReference;
=======
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
import java.util.stream.Collectors;

public class AttackNpcScript extends Script {

    public static Actor currentNpc = null;
<<<<<<< HEAD
    public static List<NPC> attackableNpcs = new ArrayList();
    String[] configAttackableNpcs;
    boolean clicked = false;

    boolean messageShown = false;
=======
    public static List<NPC> attackableNpcs = new ArrayList<>();
    private boolean messageShown = false;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6

    public static void skipNpc() {
        currentNpc = null;
    }

    public void run(PlayerAssistConfig config) {
        try {
            Rs2NpcManager.loadJson();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
<<<<<<< HEAD
        AtomicReference<List<String>> npcsToAttack = new AtomicReference<>(Arrays.stream(Arrays.stream(config.attackableNpcs().split(",")).map(String::trim).toArray(String[]::new)).collect(Collectors.toList()));
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (!config.toggleCombat()) return;
                npcsToAttack.set(Arrays.stream(Arrays.stream(config.attackableNpcs().split(",")).map(String::trim).toArray(String[]::new)).collect(Collectors.toList()));
                double treshHold = (double) (Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) * 100) / Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS);
                if (Rs2Inventory.getInventoryFood().isEmpty() && treshHold < 10) return;
                if (config.centerLocation().getX() == 0 && config.centerLocation().getY() == 0 && config.toggleCenterTile()) {
                    if(!messageShown){
                        Microbot.showMessage("Please set a center location");
                        messageShown = true;
                    }

                return;
=======

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn() || !super.run() || !config.toggleCombat())
                    return;

                List<String> npcsToAttack = Arrays.stream(config.attackableNpcs().split(","))
                        .map(String::trim)
                        .collect(Collectors.toList());

                double healthPercentage = (double) Microbot.getClient().getBoostedSkillLevel(Skill.HITPOINTS) * 100
                        / Microbot.getClient().getRealSkillLevel(Skill.HITPOINTS);
                if (Rs2Inventory.getInventoryFood().isEmpty() && healthPercentage < 10)
                    return;

                if (config.toggleCenterTile() && config.centerLocation().getX() == 0
                        && config.centerLocation().getY() == 0) {
                    if (!messageShown) {
                        Microbot.showMessage("Please set a center location");
                        messageShown = true;
                    }
                    return;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
                }
                messageShown = false;

                attackableNpcs = Microbot.getClient().getNpcs().stream()
<<<<<<< HEAD
                        .sorted(Comparator.comparingInt(value -> value.getLocalLocation().distanceTo(Microbot.getClient().getLocalPlayer().getLocalLocation())))
                        .filter(x -> !x.isDead()
                                && x.getWorldLocation().distanceTo(config.centerLocation()) < config.attackRadius()
                                && (x.getInteracting() == null || x.getInteracting() == Microbot.getClient().getLocalPlayer())
                                && x.getAnimation() == -1
                                && npcsToAttack.get().stream().anyMatch(n -> n.equalsIgnoreCase(x.getName())))
                        .collect(Collectors.toList());
                if (PlayerAssistPlugin.getCooldown() > 0) return;
                if (Rs2Combat.inCombat()) {
                    return;
                }
                for (NPC npc : attackableNpcs) {
                    if (npc == null
                            || npc.getAnimation() != -1
                            || npc.isDead()
                            || (npc.getInteracting() != null && npc.getInteracting() != Microbot.getClient().getLocalPlayer())
                            || npcsToAttack.get().stream().noneMatch(n -> npc.getName().equalsIgnoreCase(n)))
                        break;
                    if (npc.getWorldLocation().distanceTo(config.centerLocation()) > config.attackRadius())
                        break;
                    if (!Rs2Camera.isTileOnScreen(npc.getLocalLocation()))
                        Rs2Camera.turnTo(npc);

                    if (!Rs2Npc.hasLineOfSight(npc))
                        continue;


                    if (config.togglePrayer() && !config.toggleQuickPray()) {
                        AttackStyle attackStyle = AttackStyleMapper.mapToAttackStyle(Rs2NpcManager.getAttackStyle(npc.getId()));
                        if (attackStyle != null) {
                            switch (attackStyle) {
                                case MAGE:

                                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);
                                    break;
                                case MELEE:
                                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
                                    break;
                                case RANGED:
                                    Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
                                    break;
                            }

                        }
                    }
                    if (config.togglePrayer() && config.toggleQuickPray()) {
                        Rs2Prayer.toggleQuickPrayer(true);
                    }
                    Rs2Npc.interact(npc, "attack");
                    Microbot.status = "Attacking " + npc.getName();
                    PlayerAssistPlugin.setCooldown(config.playStyle().getRandomTickInterval());
                    sleepUntil(() -> Microbot.getClient().getLocalPlayer().isInteracting() && Microbot.getClient().getLocalPlayer().getInteracting() instanceof NPC);



                    break;
=======
                        .filter(npc -> !npc.isDead()
                                && npc.getWorldLocation().distanceTo(config.centerLocation()) <= config.attackRadius()
                                && (npc.getInteracting() == null
                                || npc.getInteracting() == Microbot.getClient().getLocalPlayer())
                                && npcsToAttack.contains(npc.getName())
                                && Rs2Npc.hasLineOfSight(npc))
                        .sorted(Comparator
                                .comparing((NPC npc) -> npc.getInteracting() == Microbot.getClient().getLocalPlayer() ? 0 : 1)
                                .thenComparingInt(npc -> npc.getLocalLocation()
                                        .distanceTo(Microbot.getClient().getLocalPlayer().getLocalLocation())))
                        .collect(Collectors.toList());

                if (PlayerAssistPlugin.getCooldown() > 0 || Rs2Combat.inCombat())
                    return;

                if (!attackableNpcs.isEmpty()) {
                    NPC npc = attackableNpcs.get(0);

                    if (!Rs2Camera.isTileOnScreen(npc.getLocalLocation()))
                        Rs2Camera.turnTo(npc);

                    Rs2Npc.interact(npc, "attack");
                    Microbot.status = "Attacking " + npc.getName();
                    PlayerAssistPlugin.setCooldown(config.playStyle().getRandomTickInterval());
                    sleepUntil(Rs2Player::isInteracting, 1000);
//                    sleepUntil(() -> Microbot.getClient().getLocalPlayer().isInteracting()
//                            && Microbot.getClient().getLocalPlayer().getInteracting() instanceof NPC);

                    if (config.togglePrayer()) {
                        if (!config.toggleQuickPray()) {
                            AttackStyle attackStyle = AttackStyleMapper
                                    .mapToAttackStyle(Rs2NpcManager.getAttackStyle(npc.getId()));
                            if (attackStyle != null) {
                                switch (attackStyle) {
                                    case MAGE:
                                        Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MAGIC, true);
                                        break;
                                    case MELEE:
                                        Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_MELEE, true);
                                        break;
                                    case RANGED:
                                        Rs2Prayer.toggle(Rs2PrayerEnum.PROTECT_RANGE, true);
                                        break;
                                }
                            }
                        } else {
                            Rs2Prayer.toggleQuickPrayer(true);
                        }
                    }


>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    public void shutdown() {
        super.shutdown();
<<<<<<< HEAD
        configAttackableNpcs = null;
        clicked = false;


    }
}
=======
    }
}
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
