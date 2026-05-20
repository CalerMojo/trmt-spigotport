package milkucha.trmt.erosion;

import java.util.HashMap;
import java.util.Map;

public class ErosionSaveState {
    private Map<String, SavedEntry> serializedData = new HashMap<>();

    public static class SavedEntry {
        public String material;
        public float threshold;
        public float walkedOnCount;
        public long lastTouchedGameTime;
        public int erosionStage;

        public SavedEntry() {}

        public SavedEntry(ErosionEntry entry) {
            this.material = entry.getTrackedMaterial().name();
            this.threshold = entry.getThreshold();
            this.walkedOnCount = entry.getWalkedOnCount();
            this.lastTouchedGameTime = entry.getLastTouchedGameTime();
            this.erosionStage = entry.getErosionStage();
        }
    }

    public Map<String, SavedEntry> getSerializedData() { return serializedData; }
    public void setSerializedData(Map<String, SavedEntry> serializedData) { this.serializedData = serializedData; }
}