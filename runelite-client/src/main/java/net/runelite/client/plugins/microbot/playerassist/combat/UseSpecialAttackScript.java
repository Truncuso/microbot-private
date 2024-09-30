package net.runelite.client.plugins.microbot.playerassist.combat;

<<<<<<< HEAD
import net.runelite.api.widgets.Widget;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.playerassist.PlayerAssistConfig;
import net.runelite.client.plugins.microbot.util.combat.Rs2Combat;
import net.runelite.client.plugins.microbot.util.widget.Rs2Widget;
=======
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.playerassist.PlayerAssistConfig;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6

import java.util.concurrent.TimeUnit;

public class UseSpecialAttackScript extends Script {

    public boolean run(PlayerAssistConfig config) {
        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                if (!config.useSpecialAttack()) return;
<<<<<<< HEAD
                Widget specialAttackOrb = Rs2Widget.getWidget(160, 35);
                if (specialAttackOrb == null || Microbot.getClientThread().runOnClientThread(specialAttackOrb::isHidden)) return;
                if (Microbot.getClient().getLocalPlayer().isInteracting()) {
                    Rs2Combat.setSpecState(true, 1000);
                }
            } catch(Exception ex) {
=======
                if (Rs2Player.isInteracting())
                    Microbot.getSpecialAttackConfigs().useSpecWeapon();
            } catch (Exception ex) {
>>>>>>> eaf3305b337d54b17a015219ff53601454d5a3b6
                System.out.println(ex.getMessage());
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }

    public void shutdown() {
        super.shutdown();
    }

}