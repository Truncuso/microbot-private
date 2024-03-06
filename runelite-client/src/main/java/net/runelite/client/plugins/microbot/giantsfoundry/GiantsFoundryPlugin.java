package net.runelite.client.plugins.microbot.giantsfoundry;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.util.mouse.VirtualMouse;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;

@PluginDescriptor(
        name = PluginDescriptor.Mocrosoft + "GiantsFoundry",
        description = "Microbot giants foundry plugin",
        tags = {"minigame", "microbot", "smithing"},
        enabledByDefault = false
)
@Slf4j
public class GiantsFoundryPlugin extends Plugin {

    @Inject
    private GiantsFoundryConfig config;

    @Provides
    GiantsFoundryConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(GiantsFoundryConfig.class);
    }

    @Inject
    private Client client;
    @Inject
    private ClientThread clientThread;
    @Inject
    private Notifier notifier;
    @Inject
    private ItemManager itemManager;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private GiantsFoundryOverlay giantsFoundryOverlay;

    private final GiantsFoundryScript giantsFoundryScript = new GiantsFoundryScript();

    @Override
    protected void startUp() throws AWTException {
        Microbot.pauseAllScripts = false;
        Microbot.setClient(client);
        Microbot.setClientThread(clientThread);
        Microbot.setNotifier(notifier);
        Microbot.setItemManager(itemManager);
        Microbot.setMouse(new VirtualMouse());
        if (overlayManager != null) {
            overlayManager.add(giantsFoundryOverlay);
        }
        giantsFoundryScript.run(config);
    }

    protected void shutDown() {
        giantsFoundryScript.shutdown();
        overlayManager.remove(giantsFoundryOverlay);
    }
}
