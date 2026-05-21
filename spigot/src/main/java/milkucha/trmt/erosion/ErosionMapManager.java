package milkucha.trmt.erosion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import milkucha.trmt.TRMTPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ErosionMapManager {
    private static ErosionMapManager instance;
    private final Map<Location, ErosionEntry> erosionMap = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final File storageFile;

    private ErosionMapManager() {
        this.storageFile = new File(TRMTPlugin.INSTANCE.getDataFolder(), "data.json");
        startDeErosionTask();
    }

    public static ErosionMapManager getInstance() {
        if (instance == null) instance = new ErosionMapManager();
        return instance;
    }

    public void onStep(Block block, float amount, long currentGameTime) {
        Location loc = block.getLocation();
        Material mat = block.getType();

        // Check if the target block is a sturdy trail block or soft trampleable foliage
        boolean isErodible = (mat == Material.GRASS_BLOCK || mat == Material.DIRT || 
                              mat == Material.SAND || mat == Material.COARSE_DIRT || 
                              mat.name().endsWith("_LEAVES") || isTrampleablePlant(mat));

        if (!isErodible) return;

        ErosionEntry existing = erosionMap.get(loc);
        
        if (existing != null && existing.getTrackedMaterial() != mat) {
            erosionMap.remove(loc);
            existing = null;
        }

        if (existing == null) {
            float threshold = BlockThresholds.randomThreshold(mat);
            existing = new ErosionEntry(mat, threshold, 0f, currentGameTime);
            erosionMap.put(loc, existing);
        }

        existing.recordStep(amount, currentGameTime);

        if (existing.getWalkedOnCount() >= existing.getThreshold()) {
            advanceStage(block, existing, currentGameTime);
        }
    }

    // Helper classification check for soft ground-level undergrowth vegetation families
    private boolean isTrampleablePlant(Material mat) {
        String name = mat.name();
        return name.equals("SHORT_GRASS") || name.equals("TALL_GRASS") || 
               name.equals("FERN") || name.equals("LARGE_FERN") || 
               name.contains("FLOWER") || name.endsWith("SAPLING") || 
               name.equals("DEAD_BUSH");
    }

    private void advanceStage(Block block, ErosionEntry entry, long currentTime) {
        Material currentMat = block.getType();

        // 1. Solid Ground Paths Block Swaps
        if (currentMat == Material.GRASS_BLOCK) {
            entry.advanceGrassStage(BlockThresholds.randomThreshold(currentMat));
            if (entry.getErosionStage() == 3) {
                block.setType(Material.COARSE_DIRT, true);
            }
        } else if (currentMat == Material.COARSE_DIRT) {
            block.setType(Material.DIRT_PATH, true);
            erosionMap.remove(block.getLocation()); 
        } else if (currentMat == Material.SAND) {
            block.setType(Material.SMOOTH_SANDSTONE_SLAB, true);
            erosionMap.remove(block.getLocation());
        } 
        // 2. Leaf Block Thinning & Trampling Path
        else if (currentMat.name().endsWith("_LEAVES")) {
            entry.advanceGrassStage(BlockThresholds.randomThreshold(currentMat));
            if (entry.getErosionStage() >= 3) {
                block.setType(Material.AIR, true); // Erase the leaf block completely
                erosionMap.remove(block.getLocation());
            } else {
                block.setType(Material.MANGROVE_ROOTS, true); // Thinned branch proxy
            }
        }
        // 3. 🌿 NEW PLANT TRAMPLING ACTION: Destroys plants instantly when thresholds are fulfilled
        else if (isTrampleablePlant(currentMat)) {
            block.setType(Material.AIR, true);
            erosionMap.remove(block.getLocation());
        }
    }

    // --- AUTOMATED HEALING LOOP (DE-EROSION) ---
    private void startDeErosionTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (erosionMap.isEmpty()) return;

                long TICKS_PER_DAY = 24000L;
                double daysThreshold = TRMTPlugin.INSTANCE.getConfig().getDouble("deerosion.inactivity-days", 3.0);
                long ticksInactivityTimeout = (long) (daysThreshold * TICKS_PER_DAY);

                Iterator<Map.Entry<Location, ErosionEntry>> iterator = erosionMap.entrySet().iterator();

                while (iterator.hasNext()) {
                    Map.Entry<Location, ErosionEntry> mapEntry = iterator.next();
                    Location loc = mapEntry.getKey();
                    ErosionEntry entry = mapEntry.getValue();

                    if (!loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) continue;

                    long currentWorldTime = loc.getWorld().getFullTime();
                    long timeElapsed = currentWorldTime - entry.getLastTouchedGameTime();

                    if (timeElapsed >= ticksInactivityTimeout) {
                        if (ThreadLocalRandom.current().nextDouble() < 0.20) {
                            iterator.remove(); // Drop tracking index, freezing the current block state
                        }
                    }
                }
            }
        }.runTaskTimer(TRMTPlugin.INSTANCE, 6000L, 6000L);
    }

    public void pruneChunkMemory(org.bukkit.Chunk chunk) {
        if (erosionMap.isEmpty()) return;
        erosionMap.keySet().removeIf(loc -> 
            loc.getWorld().equals(chunk.getWorld()) && 
            (loc.getBlockX() >> 4) == chunk.getX() && 
            (loc.getBlockZ() >> 4) == chunk.getZ()
        );
    }

    public boolean isErodedBlock(Location loc) { return erosionMap.containsKey(loc); }
    public int getTrackingCount() { return erosionMap.size(); }
    public void convertAllErodedToVanilla() { erosionMap.clear(); }

    // --- AUTOMATED FILE IO ---
    public void loadState() {
        if (!storageFile.exists()) return;

        try (FileReader reader = new FileReader(storageFile)) {
            ErosionSaveState saveState = gson.fromJson(reader, ErosionSaveState.class);
            if (saveState == null || saveState.getSerializedData() == null) return;

            erosionMap.clear();
            for (Map.Entry<String, ErosionSaveState.SavedEntry> e : saveState.getSerializedData().entrySet()) {
                String[] parts = e.getKey().split(",");
                if (parts.length < 4) continue;

                World world = Bukkit.getWorld(parts[0]);
                if (world == null) continue; 

                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                Location loc = new Location(world, x, y, z);

                ErosionSaveState.SavedEntry saved = e.getValue();
                Material mat = Material.getMaterial(saved.material);
                if (mat == null) continue;

                ErosionEntry entry = new ErosionEntry(
                    mat, 
                    saved.threshold, 
                    saved.walkedOnCount, 
                    saved.lastTouchedGameTime, 
                    saved.erosionStage
                );
                erosionMap.put(loc, entry);
            }
            TRMTPlugin.LOGGER.info("[TRMT] Restored " + erosionMap.size() + " active tracking paths.");
        } catch (IOException | NumberFormatException ex) {
            ex.printStackTrace();
        }
    }

    public void saveState() {
        if (erosionMap.isEmpty()) {
            if (storageFile.exists()) storageFile.delete();
            return;
        }

        if (!TRMTPlugin.INSTANCE.getDataFolder().exists()) {
            TRMTPlugin.INSTANCE.getDataFolder().mkdirs();
        }

        ErosionSaveState saveState = new ErosionSaveState();
        Map<String, ErosionSaveState.SavedEntry> serialized = saveState.getSerializedData();

        for (Map.Entry<Location, ErosionEntry> entry : erosionMap.entrySet()) {
            Location loc = entry.getKey();
            if (loc.getWorld() == null) continue;

            String stringKey = loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
            serialized.put(stringKey, new ErosionSaveState.SavedEntry(entry.getValue()));
        }

        try (FileWriter writer = new FileWriter(storageFile)) {
            gson.toJson(saveState, writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
