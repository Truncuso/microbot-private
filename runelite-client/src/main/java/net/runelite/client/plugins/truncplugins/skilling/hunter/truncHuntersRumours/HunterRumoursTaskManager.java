import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import java.io.FileReader;

public class HunterRumoursTaskManager {
    private final Map<Integer, HunterTask> rumourTasks = new HashMap<>();

    public HunterRumoursTaskManager() {
        initializeRumourTasks();
    }

    private void initializeRumourTasks() {
        rumourTasks.put(1, new HunterTask("Crimson Swift", 1, "Bird Snare", "Feldip Hills"));
        rumourTasks.put(9, new HunterTask("Copper Longtail", 9, "Bird Snare", "Piscatoris Hunter Area"));
        rumourTasks.put(19, new HunterTask("Tropical Wagtail", 19, "Bird Snare", "Feldip Hills"));
        rumourTasks.put(53, new HunterTask("Grey Chinchompa", 53, "Box Trap", "Piscatoris Hunter Area"));
        rumourTasks.put(63, new HunterTask("Red Chinchompa", 63, "Box Trap", "Feldip Hills"));
        rumourTasks.put(47, new HunterTask("Orange Salamander", 47, "Net Trap", "Kharidian Desert"));
        rumourTasks.put(67, new HunterTask("Black Salamander", 67, "Net Trap", "Wilderness"));
    }

    public HunterTask getRumourTaskForLevel(int hunterLevel) {
        HunterTask selectedTask = null;
        for (Map.Entry<Integer, HunterTask> entry : rumourTasks.entrySet()) {
            if (hunterLevel >= entry.getKey()) {
                selectedTask = entry.getValue();  // Assigns the highest task available for the level
            }
        }
        return selectedTask;
    }
    public List<HunterTask> loadTasksFromJson(String path) {
        Gson gson = new Gson();
        return gson.fromJson(new FileReader(path), new TypeToken<List<HunterTask>>() {}.getType());
    }
}

class HunterTask {
    private final String creature;
    private final int requiredLevel;
    private final String trapType;
    private final String location;

    public HunterTask(String creature, int requiredLevel, String trapType, String location) {
        this.creature = creature;
        this.requiredLevel = requiredLevel;
        this.trapType = trapType;
        this.location = location;
    }

    public String getCreature() {
        return creature;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public String getTrapType() {
        return trapType;
    }

    public String getLocation() {
        return location;
    }
}
