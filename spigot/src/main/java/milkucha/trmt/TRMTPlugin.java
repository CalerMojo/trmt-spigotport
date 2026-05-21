package milkucha.trmt;

import milkucha.trmt.erosion.ErosionMapManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class TRMTPlugin extends JavaPlugin implements Listener, CommandExecutor, TabCompleter {

    public static TRMTPlugin INSTANCE;
    public static Logger LOGGER;

    @Override
    public void onEnable() {
        INSTANCE = this;
        LOGGER = this.getLogger();

        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        
        this.getCommand("trmt").setExecutor(this);
        this.getCommand("trmt").setTabCompleter(this);

        ErosionMapManager.getInstance().loadState();
        LOGGER.info("[TRMT] Server-Side Plugin Initialized successfully.");
    }

    @Override
    public void onDisable() {
        ErosionMapManager.getInstance().saveState();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() && 
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Block standingOn = event.getTo().getBlock().getRelative(BlockFace.DOWN);
        Block runningThrough = event.getTo().getBlock();
        
        float stepWeight = event.getPlayer().isSprinting() ? 1.5f : 1.0f;
        long worldTime = event.getTo().getWorld().getFullTime();

        ErosionMapManager.getInstance().onStep(standingOn, stepWeight, worldTime);
        
        if (runningThrough.getType().name().endsWith("_LEAVES")) {
            ErosionMapManager.getInstance().onStep(runningThrough, stepWeight, worldTime);
        }
    }

    @EventHandler
    public void onChunkUnload(org.bukkit.event.world.ChunkUnloadEvent event) {
        ErosionMapManager.getInstance().pruneChunkMemory(event.getChunk());
    }

    @EventHandler
    public void onBlockPlaceAttempt(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || !event.getItem().getType().isBlock()) return;

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;

        Block blockBelow = clickedBlock.getRelative(event.getBlockFace());
        Block targetedErosionBlock = blockBelow.getRelative(0, -1, 0);

        if (ErosionMapManager.getInstance().isErodedBlock(targetedErosionBlock.getLocation())) {
            event.setCancelled(true);
        }
    }

    // --- COMMAND HANDLING ---
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        if (!sender.hasPermission("trmt.admin")) {
            sender.sendMessage(ChatColor.RED + "You lack trmt.admin permissions.");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "setspeed":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /trmt setspeed <minSteps> <maxSteps>");
                    return true;
                }
                try {
                    double min = Double.parseDouble(args[1]);
                    double max = Double.parseDouble(args[2]);

                    if (min < 0 || max < 0 || max < min) {
                        sender.sendMessage(ChatColor.RED + "Invalid values. Max must be positive and greater than min.");
                        return true;
                    }

                    getConfig().set("erosion-speed.min", min);
                    getConfig().set("erosion-speed.max", max);
                    saveConfig();

                    sender.sendMessage(ChatColor.GREEN + "[TRMT] Erosion speed thresholds updated to: " + min + " - " + max + " steps.");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Please provide valid numeric inputs.");
                }
                return true;

            // 🕒 NEW TIMEOUT COMMAND IMPLEMENTATION
            case "setdays":
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Usage: /trmt setdays <minecraftDays>");
                    return true;
                }
                try {
                    double days = Double.parseDouble(args[1]);

                    if (days < 0) {
                        sender.sendMessage(ChatColor.RED + "Days cannot be a negative value.");
                        return true;
                    }

                    getConfig().set("deerosion.inactivity-days", days);
                    saveConfig();

                    sender.sendMessage(ChatColor.GREEN + "[TRMT] Inactivity timeout before de-erosion set to: " + days + " Minecraft days.");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Please provide a valid numeric value for days.");
                }
                return true;

            case "reloadconfig":
                reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "[TRMT] Config reloaded.");
                return true;

            case "convert-to-vanilla":
                if (args.length > 1 && args[1].equalsIgnoreCase("confirm")) {
                    ErosionMapManager.getInstance().convertAllErodedToVanilla();
                    sender.sendMessage(ChatColor.GREEN + "[TRMT] Wiped step memory cache. Active paths reset.");
                } else {
                    sender.sendMessage(ChatColor.YELLOW + "[TRMT] WARNING: This wipes active step history. Existing modified blocks freeze as their current variant.");
                    sender.sendMessage(ChatColor.GOLD + "Run /trmt convert-to-vanilla confirm to proceed.");
                }
                return true;

            case "eroded-chunks":
                int totalActive = ErosionMapManager.getInstance().getTrackingCount();
                sender.sendMessage(ChatColor.GREEN + "[TRMT] Tracking " + totalActive + " active unique block coordinates in live memory.");
                return true;
        }
        return false;
    }

    // --- AUTOFILL / TAB COMPLETION ---
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("trmt.admin")) {
            return completions;
        }

        // 🕒 UPDATED AUTOFILL: Added "setdays" to the primary command suggestion engine
        if (args.length == 1) {
            List<String> subCommands = Arrays.asList("setspeed", "setdays", "reloadconfig", "convert-to-vanilla", "eroded-chunks");
            StringUtil.copyPartialMatches(args[0], subCommands, completions);
            Collections.sort(completions);
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("convert-to-vanilla")) {
            List<String> confirmations = Collections.singletonList("confirm");
            StringUtil.copyPartialMatches(args[1], confirmations, completions);
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("setspeed")) {
            return Collections.singletonList("<minSteps>");
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("setspeed")) {
            return Collections.singletonList("<maxSteps>");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("setdays")) {
            return Collections.singletonList("<minecraftDays>");
        }

        return completions;
    }
}
