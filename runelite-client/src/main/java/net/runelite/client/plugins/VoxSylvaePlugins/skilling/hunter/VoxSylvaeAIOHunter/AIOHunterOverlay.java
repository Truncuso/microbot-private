package net.runelite.client.plugins.VoxSylvaePlugins.skilling.hunter.VoxSylvaeAIOHunter;

import net.runelite.client.plugins.hunter.HunterPlugin;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.fishing.barbarian.BarbarianFishingScript;
import net.runelite.client.plugins.microbot.util.antiban.Rs2Antiban;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;



import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.events.GameTick;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPanel;

import javax.inject.Inject;
import java.awt.*;
public class AIOHunterOverlay extends OverlayPanel{

    private final AIOHunterPlugin plugin;
    private final HuntersRumoursScript script;
    private final AIOHunterConfig config;
    @Inject
    public AIOHunterOverlay(AIOHunterPlugin plugin, HuntersRumoursScript  script,AIOHunterConfig config) {        
        super(plugin);
        this.plugin = plugin;
        this.config = config;
        this.script = script;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_SCENE);        
        setNaughty();
        
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        // Display information like traps set, animals caught, and antiban status
        //graphics.setColor(Color.WHITE);
        //graphics.drawString("Traps set: " + plugin.getTrapsSet(), 10, 10);
        //graphics.drawString("Animals caught: " + plugin.getAnimalsCaught(), 10, 25);

        try {
            panelComponent.setPreferredSize(new Dimension(250, 400));
            panelComponent.getChildren().add(TitleComponent.builder()
                    .text("\uD83E\uDD86 AIO Hunter Fisher \uD83E\uDD86")
                    .color(Color.ORANGE)
                    .build());

            panelComponent.getChildren().add(LineComponent.builder().build());
            // check if player is in the correct region(10038)
            int correctRegionId = 10038;
            HunterCreatureTarget configuredCreature = script.getHunterCreatureTarget();

            String region = Rs2Player.getWorldLocation() != null ? Rs2Player.getWorldLocation().getRegionID() == 10038 ? "In Region" : "Not in Region" : "Not in Region";
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Region: " + region)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder().build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Barbarian rod: " + (Rs2Inventory.hasItem("Barbarian rod") ? "Present" : "Not Present"))
                    .build());

            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Feathers: " + (Rs2Inventory.hasItem("feather") ? String.valueOf(Rs2Inventory.get("feather").quantity) : "Not Present"))
                    .build());
                String creatureName;
                String reatureName ;               
                String method;
                int level;
                String goal ;
                int goalAmount ;
                String goalType ;
        if (configuredCreature != null) {
                creatureName = configuredCreature.getName();               
                method = configuredCreature.getMethod();
                level = configuredCreature.getRequiredLevel();
                goal = configuredCreature.getGoalName();
                goalAmount = configuredCreature.getGoalAmount();
                goalType = configuredCreature.getGoalTypeName();
            }else{
                creatureName = "No creature selected";
                method = "No method selected";
                level = 0;
                goal = "No goal selected";
                goalAmount = 0;
                goalType = "No goal type selected";
            }

            panelComponent.getChildren().add(LineComponent.builder().build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Creature: " + creatureName)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Method: " + method)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Level: " + level)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder()
                    .left("Goal: " + goal).right(goalType + ": " + goalAmount).rightColor(Color.GREEN)
                    .build());
            
            panelComponent.getChildren().add(LineComponent.builder().build());
            panelComponent.getChildren().add(LineComponent.builder().build());
            Rs2Antiban.renderAntibanOverlayComponents(panelComponent);

            panelComponent.getChildren().add(LineComponent.builder()
                    .left(Microbot.status)
                    .right("Version:" + HuntersRumoursScript.version)
                    .build());
            panelComponent.getChildren().add(LineComponent.builder().build());
                    Rs2Antiban.renderAntibanOverlayComponents(panelComponent);

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return super.render(graphics);
        
    }
}