package milkucha.trmt.erosion;

import milkucha.trmt.TRMTPlugin;
import org.bukkit.Material;
import java.util.concurrent.ThreadLocalRandom;

public final class BlockThresholds {

    private BlockThresholds() {}

    public static float randomThreshold(Material material) {
        double min = TRMTPlugin.INSTANCE.getConfig().getDouble("erosion-speed.min", 15.0);
        double max = TRMTPlugin.INSTANCE.getConfig().getDouble("erosion-speed.max", 45.0);

        if (max <= min) return (float) min;
        return (float) (min + ThreadLocalRandom.current().nextDouble() * (max - min));
    }
}