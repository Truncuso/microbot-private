package net.runelite.client.plugins.griffinplugins.griffincombat;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.plugins.griffinplugins.griffintrainer.GriffinCombatPlugin;

@ConfigGroup(GriffinCombatPlugin.CONFIG_GROUP)
public interface GriffinCombatConfig extends Config {
    @ConfigSection(
            name = "Skill Levels",
            description = "Skill Levels",
            position = 0,
            closedByDefault = false
    )
    String skillsSection = "skills";

    @ConfigItem(
            keyName = "attackLevel",
            name = "Attack",
            description = " Attack Level",
            position = 0,
            section = skillsSection
    )
    default int attackLevel() {
        return 0;
    }

    @ConfigItem(
            keyName = "strengthLevel",
            name = "Strength",
            description = " Strength Level",
            position = 1,
            section = skillsSection
    )
    default int strengthLevel() {
        return 0;
    }

    @ConfigItem(
            keyName = "defenseLevel",
            name = "Defense",
            description = " Defense Level",
            position = 2,
            section = skillsSection
    )
    default int defenseLevel() {
        return 0;
    }

    @ConfigSection(
            name = "Combat Settings",
            description = "Combat Settings",
            position = 1,
            closedByDefault = false
    )
    String combatSettingsSection = "combatSettings";

    @ConfigItem(
            keyName = "collectItems",
            name = "Loot Items",
            description = "Loot Items",
            position = 0,
            section = combatSettingsSection
    )
    default boolean collectItems() {
        return true;
    }

    @ConfigItem(
            keyName = "buryBones",
            name = "Bury Bones",
            description = "Bury Bones",
            position = 1,
            section = combatSettingsSection
    )
    default boolean buryBones() {
        return true;
    }

}
