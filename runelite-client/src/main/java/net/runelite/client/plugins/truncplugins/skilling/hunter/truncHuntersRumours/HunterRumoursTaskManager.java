package net.runelite.client.plugins.truncplugins.skilling.hunter.truncHuntersRumours;
import com.google.gson.Gson;


import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

public class HunterRumoursTaskManager {
    private List<TaskMaster> taskMasters;
    private Map<String, HunterCreature> creatures;

    public HunterRumoursTaskManager() {
        loadTaskMastersFromJson("path/to/hunterMasters.json");
        loadCreaturesFromJson("path/to/hunterCreatures.json");
    }

    private void loadTaskMastersFromJson(String path) {
        try (FileReader reader = new FileReader(path)) {
            Gson gson = new Gson();
            TaskMastersWrapper wrapper = gson.fromJson(reader, TaskMastersWrapper.class);
            taskMasters = wrapper.hunterMasters;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCreaturesFromJson(String path) {
        try (FileReader reader = new FileReader(path)) {
            Gson gson = new Gson();
            CreaturesWrapper wrapper = gson.fromJson(reader, CreaturesWrapper.class);
            creatures = wrapper.hunterCreatures.stream()
                .collect(Collectors.toMap(HunterCreature::getName, creature -> creature));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<HunterCreature> getTasksForLevel(int hunterLevel, String taskMasterName) {
        for (TaskMaster master : taskMasters) {
            if (master.getName().equals(taskMasterName) && hunterLevel >= master.getRequiredLevel()) {
                return master.getCreatures().stream()
                    .map(creatures::get)
                    .filter(creature -> creature != null && hunterLevel >= creature.getRequiredLevel())
                    .collect(Collectors.toList());
            }
        }
        return null;
    }

    public List<TaskMaster> getTaskMasters() {
        return taskMasters;
    }

    public Map<String, HunterCreature> getCreatures() {
        return creatures;
    }

    // Wrapper classes for JSON deserialization
    private static class TaskMastersWrapper {
        List<TaskMaster> hunterMasters;
    }

    private static class CreaturesWrapper {
        List<HunterCreature> hunterCreatures;
    }
}
