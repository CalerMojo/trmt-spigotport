package milkucha.trmt.erosion;

import org.bukkit.Material;

public class ErosionEntry {
    private final Material trackedMaterial;
    private float threshold;
    private float walkedOnCount;
    private long lastTouchedGameTime;
    private int erosionStage;

    public ErosionEntry(Material trackedMaterial, float threshold, float walkedOnCount, long lastTouchedGameTime) {
        this.trackedMaterial = trackedMaterial;
        this.threshold = threshold;
        this.walkedOnCount = walkedOnCount;
        this.lastTouchedGameTime = lastTouchedGameTime;
        this.erosionStage = 0;
    }

    public ErosionEntry(Material trackedMaterial, float threshold, float walkedOnCount, long lastTouchedGameTime, int erosionStage) {
        this.trackedMaterial = trackedMaterial;
        this.threshold = threshold;
        this.walkedOnCount = walkedOnCount;
        this.lastTouchedGameTime = lastTouchedGameTime;
        this.erosionStage = erosionStage;
    }

    public Material getTrackedMaterial() { return trackedMaterial; }
    public float getThreshold() { return threshold; }
    public float getWalkedOnCount() { return walkedOnCount; }
    public long getLastTouchedGameTime() { return lastTouchedGameTime; }
    public int getErosionStage() { return erosionStage; }

    public void recordStep(float amount, long currentGameTime) {
        this.walkedOnCount += amount;
        this.lastTouchedGameTime = currentGameTime;
    }

    public void advanceGrassStage(float newThreshold) {
        this.erosionStage++;
        this.walkedOnCount = 0f;
        this.threshold = newThreshold;
    }

    public boolean isEmpty() { return walkedOnCount <= 0; }
}