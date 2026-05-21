package milkucha.trmt.erosion;

import milkucha.trmt.TRMTPlugin;
import org.bukkit.Material;
import java.util.concurrent.ThreadLocalRandom;

public final class BlockThresholds {

    private BlockThresholds() {}

    public static float randomThreshold(Material material) {
        double min = TRMTPlugin.INSTANCE.getConfig().getDouble("erosion-speed.min", 15.0);
        double max = TRMTPlugin.INSTANCE.getConfig().getDouble("erosion-speed.max", 45.0);

        double threshold;
        if (max <= min) {
            threshold = min;
        } else {
            threshold = min + ThreadLocalRandom.current().nextDouble() * (max - min);
        }

        // Scale down the total durability threshold if the block is leaf or soft plant foliage
        if (material.name().endsWith("_LEAVES") || isTrampleablePlant(material)) {
            double plantFactor = TRMTPlugin.INSTANCE.getConfig().getDouble("modifiers.vegetation-durability-factor", 0.2);
            threshold *= plantFactor;
        }

        return (float) threshold;
    }

    private static boolean isTrampleablePlant(Material mat) {
        String name = mat.name();
        return name.equals("SHORT_GRASS") || name.equals("TALL_GRASS") || 
               name.equals("FERN") || name.equals("LARGE_FERN") || 
               name.contains("FLOWER") || name.endsWith("SAPLING") || 
               name.equals("DEAD_BUSH");
    }
}
